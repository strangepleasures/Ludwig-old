package ludwig.utils

import ludwig.model.*

import ludwig.utils.NodeUtils.arguments

internal class ArgumentsCount : NodeVisitor<Int?> {
    override fun visitProject(projectNode: ProjectNode): Int? {
        return null
    }

    override fun visitPackage(packageNode: PackageNode): Int? {
        return null
    }

    override fun visitFunction(functionNode: FunctionNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitVariable(variableNode: VariableNode): Int? {
        return 0
    }

    override fun visitAssignment(assignmentNode: AssignmentNode): Int? {
        return 2
    }

    override fun visitReference(referenceNode: ReferenceNode): Int? {
        return arguments(referenceNode.ref).size
    }

    override fun visitList(listNode: ListNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitLiteral(literalNode: LiteralNode): Int? {
        return 0
    }

    override fun visitLambda(lambdaNode: LambdaNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitCall(callNode: CallNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitReturn(returnNode: ReturnNode): Int? {
        return 1
    }

    override fun visitIf(ifNode: IfNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitElse(elseNode: ElseNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitFor(forNode: ForNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitFunctionReference(functionReference: FunctionReferenceNode): Int? {
        return 1
    }

    override fun visitThrow(throwNode: ThrowNode): Int? {
        return 1
    }

    override fun visitTry(tryNode: TryNode): Int? {
        return Integer.MAX_VALUE
    }

    override fun visitPlaceholder(placeholderNode: PlaceholderNode): Int? {
        return 0
    }

    override fun visitBreak(breakNode: BreakNode): Int? {
        return 1
    }

    override fun visitContinue(continueNode: ContinueNode): Int? {
        return 1
    }

    override fun visitOverride(overrideNode: OverrideNode): Int? {
        return arguments(overrideNode).size
    }

    override fun visitClass(classNode: ClassNode): Int? {
        return arguments(classNode).size
    }

    override fun visitCatch(catchNode: CatchNode): Int? {
        return Integer.MAX_VALUE
    }
}
