package ludwig.interpreter

import ludwig.model.*
import ludwig.runtime.StdLib
import ludwig.utils.NodeUtils
import org.pcollections.HashPMap
import org.pcollections.TreePVector

internal class Evaluator(private var locals: HashPMap<NamedNode, Any>?) : NodeVisitor<Any?> {
    private var doElse: Boolean = false


    override fun visitFunction(functionNode: FunctionNode): Any? {
        throw UnsupportedOperationException()
    }

    override fun visitList(listNode: ListNode): Any? {
        var list = TreePVector.empty<Any?>()
        for (item in listNode) {
            list = list.plus(item.accept(this))
        }
        return list
    }

    override fun visitLiteral(literalNode: LiteralNode): Any? {
        return literalNode.value
    }

    override fun visitPackage(packageNode: PackageNode): Any {
        throw UnsupportedOperationException()
    }

    override fun visitVariable(variableNode: VariableNode): Any {
        throw UnsupportedOperationException()
    }

    override fun visitReference(referenceNode: ReferenceNode): Any? {
        val head = referenceNode.ref

        val isLazy = head is FunctionNode && head.lazy
        val args = arrayOfNulls<Any>(referenceNode.size)
        for (i in args.indices) {
            args[i] = if (isLazy) Return<Any?>(referenceNode[i], locals!!) else referenceNode[i].accept(this)
        }

        return untail(tail(head, args))
    }

    override fun visitFunctionReference(functionReference: FunctionReferenceNode): Any? {
        val node = (functionReference[0] as ReferenceNode).ref
        return if (node !is Callable) {
            CallableRef(node)
        } else node
    }

    override fun visitThrow(throwNode: ThrowNode): Any {
        return Error(throwNode[0].accept(this).toString())
    }

    override fun visitTry(tryNode: TryNode): Any? {
        var result: Any? = null
        for (node in tryNode) {
            try {
                result = node.accept(this)
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

    override fun visitPlaceholder(placeholderNode: PlaceholderNode): Any? {
        return null
    }

    override fun visitBreak(breakNode: BreakNode): Any {
        return Break()
    }

    override fun visitContinue(continueNode: ContinueNode): Any {
        return Continue()
    }

    override fun visitOverride(overrideNode: OverrideNode): Any {
        throw UnsupportedOperationException()
    }

    override fun visitClass(classNode: ClassNode): Any? {
        return null
    }

    override fun visitCatch(catchNode: CatchNode): Any? {
        if (Error.error() == null) {
            return null
        }
        val saved = Error.error()

        var result: Any? = null
        for (node in catchNode) {
            try {
                result = node.accept(this)
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

    override fun visitCall(callNode: CallNode): Any? {
        val head = callNode[0].accept(this)
        if (head is Delayed<*>) {
            return untail(head.get())
        }

        val callable = head as Callable

        val delayed = callable.isLazy

        val args = callNode
                .stream()
                .skip(1)
                .map { node -> if (delayed) Return<Any?>(node, locals!!) else node.accept(this) }
                .toArray()

        return untail(callable.tail(args))
    }

    override fun visitLambda(lambdaNode: LambdaNode): Any? {
        return Closure(locals!!, lambdaNode)
    }

    override fun visitReturn(returnNode: ReturnNode): Any? {
        return Return<Any?>(returnNode[0], locals!!)
    }

    override fun visitProject(projectNode: ProjectNode): Any? {
        throw UnsupportedOperationException()
    }

    override fun visitIf(ifNode: IfNode): Any? {
        val test = ifNode[0].accept(this) as Boolean
        if (test) {
            var result: Any? = null
            for (node in ifNode) {
                result = node.accept(this)
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

    override fun visitAssignment(assignmentNode: AssignmentNode): Any? {
        val value = assignmentNode[1].accept(this)

        var lhs = assignmentNode[0]
        if (lhs is ReferenceNode) {
            lhs = lhs.ref
            if (NodeUtils.isField(lhs)) {
                val instance = assignmentNode[0][0].accept(this) as Instance
                instance[lhs as VariableNode] = value
                return value
            }
        }

        locals = locals!!.plus(lhs as NamedNode, value)
        return value
    }

    override fun visitElse(elseNode: ElseNode): Any? {
        if (doElse) {
            doElse = false
            var result: Any? = null
            for (node in elseNode) {
                result = node.accept(this)
                if (result is Signal) {
                    break
                }
            }

            return result
        }
        return null
    }

    override fun visitFor(forNode: ForNode): Any? {
        loop@ for (`var` in forNode[1].accept(this) as Iterable<*>) {
            val v = forNode[0] as VariableNode
            locals = locals!!.plus(v, `var`)
            for (i in 1..forNode.size - 1) {
                val value = forNode[i].accept(this)
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
        var result = result
        while (result is Return<*>) {
            val tail = result as Return<*>?
            val state = locals
            try {
                locals = tail!!.locals
                result = tail.node.accept(this)
            } finally {
                locals = state
            }
        }
        return result
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

        if (NodeUtils.isField(impl)) {
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
                    locals = locals!!.plus(head[i] as NamedNode, args[i])
                }

                var result: Any? = null

                if (impl is OverrideNode) {
                    for (i in 1..impl.size - 1) {
                        result = impl[i].accept(this)
                        if (result is Signal) {
                            break
                        }
                    }
                } else {
                    for (i in args.size..head.size - 1) {
                        result = head[i].accept(this)
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
            val fn = (head[0] as ReferenceNode).ref as FunctionNode
            val savedLocals = locals

            try {
                for (i in args.indices) {
                    locals = locals!!.plus(fn[i] as NamedNode, args[i])
                }

                var result: Any? = null

                if (impl is OverrideNode) {
                    for (i in 1..impl.size - 1) {
                        result = impl[i].accept(this)
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

        return locals!![head]
    }
}
