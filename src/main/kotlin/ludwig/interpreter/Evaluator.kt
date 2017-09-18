package ludwig.interpreter

import ludwig.model.*
import ludwig.runtime.StdLib
import ludwig.utils.isField
import org.pcollections.HashPMap
import org.pcollections.TreePVector

internal class Evaluator(private var locals: HashPMap<NamedNode, Any>) {
    private var doElse: Boolean = false

    fun eval(node: Node): Any? = when (node) {
        is LiteralNode -> visitLiteral(node)
        is SymbolNode -> visitReference(node)
        is AssignmentNode -> visitAssignment(node)
        is CallNode -> visitCall(node)
        is RefNode -> visitFunctionReference(node)
        is LambdaNode -> visitLambda(node)
        is IfNode -> visitIf(node)
        is ElseNode -> visitElse(node)
        is ForNode -> visitFor(node)
        is BreakNode -> visitBreak(node)
        is ContinueNode -> visitContinue(node)
        is ReturnNode -> visitReturn(node)
        is ListNode -> visitList(node)
        is ThrowNode -> visitThrow(node)
        is TryNode -> visitTry(node)
        is CatchNode -> visitCatch(node)
        else -> throw UnsupportedOperationException()
    }


    fun visitList(listNode: ListNode): Any? {
        var list = TreePVector.empty<Any?>()
        for (item in listNode) {
            list = list.plus(eval(item))
        }
        return list
    }

    fun visitLiteral(literalNode: LiteralNode): Any? {
        return literalNode.value
    }

    fun visitReference(symbolNode: SymbolNode): Any? {
        val head = symbolNode.ref

        val isLazy = head is FunctionNode && head.lazy
        val args = arrayOfNulls<Any>(symbolNode.size)
        for (i in args.indices) {
            args[i] = if (isLazy) Return<Any?>(symbolNode[i], locals) else eval(symbolNode[i])
        }

        return untail(tail(head, args))
    }

    fun visitFunctionReference(ref: RefNode): Any? {
        val node = (ref[0] as SymbolNode).ref
        return if (node !is Callable) {
            CallableRef(node)
        } else node
    }

    fun visitThrow(throwNode: ThrowNode): Any {
        return Error(eval(throwNode[0]).toString())
    }

    fun visitTry(tryNode: TryNode): Any? {
        var result: Any? = null
        for (node in tryNode) {
            try {
                result = eval(node)
                if (result is Signal) {
                    break
                }
            } catch (e: Exception) {
                Error(e.message)
            }

        }
        return if (result is Error) {
            null
        } else {
            result
        }
    }

    fun visitBreak(breakNode: BreakNode): Any {
        return Break()
    }

    fun visitContinue(continueNode: ContinueNode): Any {
        return Continue()
    }

    fun visitCatch(catchNode: CatchNode): Any? {
        if (Error.error() == null) {
            return null
        }
        val saved = Error.error()

        var result: Any? = null
        for (node in catchNode) {
            try {
                result = eval(node)
                if (result is Signal) {
                    break
                }
            } catch (e: Exception) {
                return null
            }

        }
        if (Error.error() == saved) {
            Error.reset()
        }
        return result
    }

    fun visitCall(callNode: CallNode): Any? {
        val head = eval(callNode[0])
        if (head is Delayed<*>) {
            return untail(head.get())
        }

        val callable = head as Callable

        val delayed = callable.isLazy

        val args = callNode
                .stream()
                .skip(1)
                .map { node -> if (delayed) Return<Any?>(node, locals) else eval(node) }
                .toArray()

        return untail(callable.tail(args))
    }

    fun visitLambda(lambdaNode: LambdaNode): Any? {
        return Closure(locals, lambdaNode)
    }

    fun visitReturn(returnNode: ReturnNode): Any? {
        return Return<Any?>(returnNode[0], locals)
    }

    fun visitIf(ifNode: IfNode): Any? {
        val test = eval(ifNode[0]) as Boolean
        if (test) {
            var result: Any? = null
            for (node in ifNode) {
                result = eval(node)
                if (result is Signal) {
                    break
                }
            }
            return result
        } else {
            doElse = true
        }
        return null
    }

    fun visitAssignment(assignmentNode: AssignmentNode): Any? {
        val value = eval(assignmentNode[1])

        var lhs = assignmentNode[0]
        if (lhs is SymbolNode) {
            lhs = lhs.ref
            if (isField(lhs)) {
                val instance = eval(assignmentNode[0][0]) as Instance
                instance[lhs as VariableNode] = value
                return value
            }
        }

        locals = locals.plus(lhs as NamedNode, value)
        return value
    }

    fun visitElse(elseNode: ElseNode): Any? {
        if (doElse) {
            doElse = false
            var result: Any? = null
            for (node in elseNode) {
                result = eval(node)
                if (result is Signal) {
                    break
                }
            }

            return result
        }
        return null
    }

    fun visitFor(forNode: ForNode): Any? {
        val v = forNode[0] as VariableNode
        loop@ for (x in eval(forNode[1]) as Iterable<*>) {
            locals = locals.plus(v, x)
            for (i in 2 until forNode.size) {
                val value = eval(forNode[i])
                if (value is Signal) {
                    if (value is Break) {
                        break@loop
                    } else if (value is Continue) {
                        continue@loop
                    }
                    return value
                }
            }
        }
        return null
    }

    private fun untail(result: Any?): Any? {
        var ret = result
        while (ret is Return<*>) {
            val tail = ret as Return<*>?
            val state = locals
            try {
                locals = tail!!.locals
                ret = eval(tail.node)
            } finally {
                locals = state
            }
        }
        return ret
    }

    fun tail(head: Node, args: Array<Any?>): Any? {
        var impl = head

        if (head is ClassNode) {
            val type = ClassType.of(head)
            val instance = Instance(type)
            for (i in args.indices) {
                instance[type.fields()[i]] = args[i]
            }
            return instance
        }

        if (args.size > 0 && args[0] is Instance) {
            impl = StdLib.type(args[0])!!.implementation(head)
        }

        if (isField(impl)) {
            return (args[0] as Instance).get<Any>(impl as VariableNode)
        }

        if (impl is FunctionNode) {
            val callable = Builtins.callable(impl)
            if (callable != null) {
                return callable.tail(args)
            }
        }

        if (head is FunctionNode) {
            val savedLocals = locals

            try {
                for (i in args.indices) {
                    locals = locals.plus(head[i] as NamedNode, args[i])
                }

                var result: Any? = null

                if (impl is OverrideNode) {
                    for (i in 1..impl.size - 1) {
                        result = eval(impl[i])
                        if (result is Signal) {
                            break
                        }
                    }
                } else {
                    for (i in args.size..head.size - 1) {
                        result = eval(head[i])
                        if (result is Signal) {
                            break
                        }
                    }

                }

                return result
            } finally {
                locals = savedLocals
            }
        }
        if (head is OverrideNode) {
            val fn = (head[0] as SymbolNode).ref as FunctionNode
            val savedLocals = locals

            try {
                for (i in args.indices) {
                    locals = locals.plus(fn[i] as NamedNode, args[i])
                }

                var result: Any? = null

                if (impl is OverrideNode) {
                    for (i in 1..impl.size - 1) {
                        result = eval(impl[i])
                        if (result is Signal) {
                            break
                        }
                    }
                }

                return result
            } finally {
                locals = savedLocals
            }
        }

        return locals[head]
    }
}
