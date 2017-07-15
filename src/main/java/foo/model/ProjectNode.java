package foo.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectNode extends NamedNode {
    private final List<PackageNode> packages = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }
}
