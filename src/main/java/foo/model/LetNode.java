package foo.model;

import lombok.Data;

@Data
public class LetNode extends NamedNode {
    private Node value;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLet(this);
    }
}
