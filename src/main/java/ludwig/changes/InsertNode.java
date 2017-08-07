package ludwig.changes;


import ludwig.model.Node;
import lombok.Getter;
import lombok.Setter;

public class InsertNode extends Insert {
    Node node;

    public Node getNode() {
        return node;
    }

    public InsertNode setNode(Node node) {
        this.node = node;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitInsertNode(this);
    }
}