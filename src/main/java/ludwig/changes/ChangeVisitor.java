package ludwig.changes;

public interface ChangeVisitor<T> {

    T visitInsertNode(InsertNode insert);

    T visitInsertReference(InsertReference insert);

    T visitDelete(Delete delete);

    T visitComment(Comment comment);
}