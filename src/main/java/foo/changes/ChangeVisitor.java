package foo.changes;

public interface ChangeVisitor<T> {
    T visitProject(Project project);

    T visitPackage(Package aPackage);

    T visitFunction(Function function);

    T visitParameter(Parameter parameter);

    T visitBoundCall(BoundCall boundCall);

    T visitReference(Reference reference);

    T visitLiteral(Literal literal);

    T visitUnboundCall(UnboundCall unboundCall);

    T visitReturn(Return aReturn);

    T visitLambda(Lambda lambda);

    T visitAnd(And and);

    T visitOr(Or or);
}
