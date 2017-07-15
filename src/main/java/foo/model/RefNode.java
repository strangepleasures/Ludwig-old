package foo.model;

import lombok.*;

@Getter
@Setter
public class RefNode extends Node {
    private NamedNode node;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitRef(this);
    }
}
