package ludwig.model;

public class ReferenceNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

    @Override
    public String toString() {
        return "ref";
    }
}
