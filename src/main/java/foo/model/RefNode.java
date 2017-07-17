package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitRef(this);
    }
}
