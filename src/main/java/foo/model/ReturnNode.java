package foo.model;

import lombok.Data;

@Data
public class ReturnNode extends Node {
    private Node value;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitReturn(this);
    }
}
