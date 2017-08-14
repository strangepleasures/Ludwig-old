package ludwig.changes;


import ludwig.model.Node;
import lombok.Getter;
import lombok.Setter;

public class InsertNode extends Insert<InsertNode> {
    private Node node;

    public Node node() {
        return node;
    }

    public InsertNode node(Node node) {
        this.node = node;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitInsertNode(this);
    }
}