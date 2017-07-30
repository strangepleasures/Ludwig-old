package ludwig.model;

public interface NodeVisitor<T> {
    T visitBoundCall(BoundCallNode boundCallNode);

    T visitFunction(FunctionNode functionNode);

    T visitLet(LetNode letNode);

    T visitList(ListNode listNode);

    T visitLiteral(LiteralNode literalNode);

    T visitPackage(PackageNode packageNode);

    T visitParameter(ParameterNode parameterNode);

    T visitRef(RefNode refNode);

    T visitUnboundCall(UnboundCallNode unboundCallNode);

    T visitLambda(LambdaNode lambdaNode);

    T visitReturn(ReturnNode returnNode);

    T visitProject(ProjectNode projectNode);

    T visitIf(IfNode ifNode);

    T visitAssignment(AssignmentNode assignmentNode);

    T visitElse(ElseNode elseNode);

    T visitFor(ForNode forNode);
}
