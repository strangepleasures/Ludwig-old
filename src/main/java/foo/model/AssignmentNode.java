package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentNode extends Node {
    NamedNode lhs;
    Node rhs;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }
}
