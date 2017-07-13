package foo.changes;

public interface ChangeVisitor<T> {
    T visitCreateProject(CreateProjectChange createProjectChange);

    T visitCreatePackage(CreatePackageChange createPackageChange);
}
