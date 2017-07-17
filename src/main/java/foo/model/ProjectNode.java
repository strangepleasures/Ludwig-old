package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }
}
