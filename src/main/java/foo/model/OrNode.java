package foo.model;

import lombok.Getter;

@Getter
public class OrNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOr(this);
    }
}
