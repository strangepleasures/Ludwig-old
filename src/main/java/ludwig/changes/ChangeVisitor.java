package ludwig.changes;

public interface ChangeVisitor<T> {

    T visitInsertNode(InsertNode insert);

    T visitInsertReference(InsertReference insert);
}