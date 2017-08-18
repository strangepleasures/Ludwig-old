package ludwig.model;

public interface NodeVisitor<T> {
    T visitProject(ProjectNode projectNode);

    T visitPackage(PackageNode packageNode);

    T visitFunction(FunctionNode functionNode);

    T visitVariable(VariableNode variableNode);

    T visitAssignment(AssignmentNode assignmentNode);

    T visitReference(ReferenceNode referenceNode);

    T visitList(ListNode listNode);

    T visitLiteral(LiteralNode literalNode);

    T visitLambda(LambdaNode lambdaNode);

    T visitCall(CallNode callNode);

    T visitReturn(ReturnNode returnNode);

    T visitIf(IfNode ifNode);

    T visitElse(ElseNode elseNode);

    T visitFor(ForNode forNode);

    T visitFunctionReference(FunctionReferenceNode functionReference);

    T visitThrow(ThrowNode throwNode);

    T visitPlaceholder(PlaceholderNode placeholderNode);

    T visitBreak(BreakNode breakNode);

    T visitContinue(ContinueNode continueNode);

    T visitOverride(OverrideNode overrideNode);

    T visitClass(ClassNode classNode);
}
