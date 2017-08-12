package ludwig.model;

public class PackageNode extends NamedNode<PackageNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPackage(this);
    }

    @Override
    public boolean isOrdered() {
        return false;
    }
}
