package foo.model;

public interface NodeVisitor<T> {
    T visitCall(CallNode callNode);

    T visitFunction(FunctionNode functionNode);

    T visitLet(LetNode letNode);

    T visitList(ListNode listNode);

    T visitLiteral(LiteralNode literalNode);

    T visitPackage(PackageNode packageNode);

    T visitParameter(ParameterNode parameterNode);

    T visitRef(RefNode refNode);
}
