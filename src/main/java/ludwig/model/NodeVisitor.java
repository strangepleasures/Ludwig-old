package ludwig.model;

public interface NodeVisitor<T> {
    T visitProject(ProjectNode projectNode);

    T visitPackage(PackageNode packageNode);

    T visitFunction(FunctionNode functionNode);

    T visitParameter(ParameterNode parameterNode);

    T visitSeparator(SeparatorNode separatorNode);

    T visitVariableDeclaration(VariableDeclarationNode variableDeclarationNode);

    T visitAssignment(AssignmentNode assignmentNode);

    T visitVariable(VariableNode variableNode);

    T visitList(ListNode listNode);

    T visitLiteral(LiteralNode literalNode);

    T visitLambda(LambdaNode lambdaNode);

    T visitBoundCall(BoundCallNode boundCallNode);

    T visitUnboundCall(UnboundCallNode unboundCallNode);

    T visitReturn(ReturnNode returnNode);

    T visitIf(IfNode ifNode);

    T visitElse(ElseNode elseNode);

    T visitFor(ForNode forNode);

    T visitReference(ReferenceNode referenceNode);
}
