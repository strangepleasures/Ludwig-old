package foo.changes;

public interface ChangeVisitor<T> {
    T visitCreateProject(CreateProject createProject);

    T visitCreatePackage(CreatePackage createPackage);

    T visitCreateFunction(CreateFunction createFunction);

    T visitCreateParameter(CreateParameter createParameter);
}
