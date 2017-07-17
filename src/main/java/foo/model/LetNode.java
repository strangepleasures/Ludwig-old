package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LetNode extends NamedNode implements ValueHolder<Node> {
    private Node value;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLet(this);
    }
}
