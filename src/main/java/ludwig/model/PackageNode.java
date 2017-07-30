package ludwig.model;

public class PackageNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPackage(this);
    }

    public NamedNode item(String name) {
        return children().stream().map(n -> (NamedNode) n).filter(it -> it.getName().equals(name)).findFirst().orElse(null);
    }
}
