package foo.model;

import lombok.Getter;

@Getter
public class AndNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAnd(this);
    }
}
