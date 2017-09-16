package ludwig.utils

import ludwig.model.*
import java.util.*

class PrettyPrinter private constructor() : NodeVisitor<Unit> {
    private val builder = StringBuilder()

    override fun toString(): String {
        if (builder.length == 0 || builder[builder.length - 1] != '\n') {
            builder.append('\n')
        }
        return builder.toString()
    }

    private var indentation: Int = 0

    override fun visitList(node: ListNode) {
        print("list")
        val inline = level(node) < 4
        for (n in node) {
            child(n, inline)
        }
    }

    override fun visitFunction(functionNode: FunctionNode) {
        var body = false
        for (child in functionNode) {
            if (child !is VariableNode) {
                body = true
            }
            if (body) {
                child(child, false)
            }
        }
    }


    override fun visitClass(classNode: ClassNode) {
    }

    override fun visitCatch(catchNode: CatchNode) {
        print("catch")
        catchNode.forEach { node -> child(node, false) }
    }


    override fun visitLiteral(literalNode: LiteralNode) {
        print(literalNode.text)
    }

    override fun visitPackage(packageNode: PackageNode) {
    }

    override fun visitVariable(variableNode: VariableNode) {
        print(variableNode.name)
    }

    override fun visitReference(referenceNode: ReferenceNode) {
        print(referenceNode.toString())
        val inline = canBeInlined(referenceNode)
        referenceNode.forEach { node -> child(node, inline) }
    }

    override fun visitFunctionReference(functionReference: FunctionReferenceNode) {
        print("ref ")
        if (!functionReference.isEmpty()) {
            print(functionReference[0].toString())
        }
    }

    override fun visitThrow(throwNode: ThrowNode) {
        print("throw ")
        throwNode[0].accept(this)
    }

    override fun visitTry(tryNode: TryNode) {
        print("try")
        tryNode.forEach { node -> child(node, false) }
    }

    override fun visitPlaceholder(placeholderNode: PlaceholderNode) {
        print(placeholderNode.toString())
    }

    override fun visitBreak(breakNode: BreakNode) {
        print("break ")
        breakNode[0].accept(this)
    }

    override fun visitContinue(continueNode: ContinueNode) {
        print("continue ")
        continueNode[0].accept(this)
    }

    override fun visitOverride(overrideNode: OverrideNode) {
        for (i in 1..overrideNode.size - 1) {
            child(overrideNode[i], false)
        }
    }

    override fun visitCall(callNode: CallNode) {
        print("call")
        child(callNode[0], true)
        val inline = level(callNode) < 4
        for (i in 1..callNode.size - 1) {
            child(callNode[i], inline)
        }
    }

    override fun visitLambda(lambdaNode: LambdaNode) {
        print("λ")

        var body = false
        val inline = level(lambdaNode) < 4
        for (n in lambdaNode) {
            if (!body && n !is VariableNode) {
                body = true
                print(" : ")
            }
            if (body) {
                child(n, inline)
            } else {
                print(" " + n)
            }
        }
    }

    override fun visitReturn(returnNode: ReturnNode) {
        print("return")
        returnNode.forEach { node -> child(node, true) }
    }

    override fun visitProject(projectNode: ProjectNode) {
    }

    override fun visitIf(ifNode: IfNode) {
        print("if")
        child(ifNode[0], true)
        for (i in 1 until ifNode.size) {
            child(ifNode[i], false)
        }
    }

    override fun visitAssignment(assignmentNode: AssignmentNode) {
        print("= ")
        child(assignmentNode[0], true)
        val inline = level(assignmentNode) < 4
        for (i in 1..assignmentNode.size - 1) {
            child(assignmentNode[i], inline)
        }
    }

    override fun visitElse(elseNode: ElseNode) {
        print("else")
        elseNode.forEach { node -> child(node, false) }
    }

    override fun visitFor(forNode: ForNode) {
        print("for")
        for (i in 0..forNode.size - 1) {
            child(forNode[i], i < 2)
        }
    }

    private fun child(node: Node, inline: Boolean) {
        indentation++
        if (!inline) {
            if (builder.length > 0) {
                builder.append('\n')
            }
            for (i in 1..indentation - 1) {
                print("    ")
            }
        } else {
            print(" ")
        }
        node.accept(this)
        indentation--
    }

    internal fun print(s: String?) {
        builder.append(s)
    }

    companion object {

        fun print(parent: Node): String {
            val printer = PrettyPrinter()
            parent.accept(printer)
            return printer.toString()
        }

        private fun canBeInlined(node: Node): Boolean {
            if (level(node) > 3) {
                return false
            }

            for (i in 0..node.size - 1 - 1) {
                if (NodeUtils.argumentsCount(node[i]) > 3) {
                    return false
                }
            }

            return true
        }

        private fun level(node: Node): Int {
            return node.stream().map<Int>({ level(it) }).max(Comparator.naturalOrder()).orElse(0) + 1
        }
    }

}
