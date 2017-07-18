package foo.changes;


import foo.model.Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsertNode extends Insert {
    Node node;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitInsertNode(this);
    }
}