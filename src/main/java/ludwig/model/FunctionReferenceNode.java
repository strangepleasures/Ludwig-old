package ludwig.model;

public class FunctionReferenceNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFunctionReference(this);
    }

    @Override
    public String toString() {
        return "ref";
    }
}
