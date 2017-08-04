package ludwig.model;

public class PackageNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPackage(this);
    }

    public Named item(String name) {
        return children().stream().map(n -> (Named) n).filter(it -> it.getName().equals(name)).findFirst().orElse(null);
    }
}
