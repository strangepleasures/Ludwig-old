package ludwig.model

interface NodeVisitor<T> {
    fun visitProject(projectNode: ProjectNode): T

    fun visitPackage(packageNode: PackageNode): T

    fun visitFunction(functionNode: FunctionNode): T

    fun visitVariable(variableNode: VariableNode): T

    fun visitAssignment(assignmentNode: AssignmentNode): T

    fun visitReference(referenceNode: ReferenceNode): T

    fun visitList(listNode: ListNode): T

    fun visitLiteral(literalNode: LiteralNode): T

    fun visitLambda(lambdaNode: LambdaNode): T

    fun visitCall(callNode: CallNode): T

    fun visitReturn(returnNode: ReturnNode): T

    fun visitIf(ifNode: IfNode): T

    fun visitElse(elseNode: ElseNode): T

    fun visitFor(forNode: ForNode): T

    fun visitFunctionReference(functionReference: FunctionReferenceNode): T

    fun visitThrow(throwNode: ThrowNode): T

    fun visitTry(tryNode: TryNode): T

    fun visitPlaceholder(placeholderNode: PlaceholderNode): T

    fun visitBreak(breakNode: BreakNode): T

    fun visitContinue(continueNode: ContinueNode): T

    fun visitOverride(overrideNode: OverrideNode): T

    fun visitClass(classNode: ClassNode): T

    fun visitCatch(catchNode: CatchNode): T
}
