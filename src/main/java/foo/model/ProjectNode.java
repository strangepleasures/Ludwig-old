package foo.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectNode extends NamedNode {
    private final List<PackageNode> packages = new ArrayList<>();

    public List<PackageNode> getPackages() {
        return packages;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }
}
