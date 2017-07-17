package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LetNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLet(this);
    }
}
