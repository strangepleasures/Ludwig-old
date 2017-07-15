package foo.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectNode extends NamedNode {
    private final List<PackageNode> packages = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }
}
