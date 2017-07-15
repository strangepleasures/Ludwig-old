package foo.model;

import lombok.*;

@Getter
@Setter
public class LetNode extends NamedNode {
    private Node value;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLet(this);
    }
}
