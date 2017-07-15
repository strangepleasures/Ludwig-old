package foo.model;

import lombok.Data;

@Data
public class RefNode extends Node {
    private NamedNode node;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitRef(this);
    }
}
