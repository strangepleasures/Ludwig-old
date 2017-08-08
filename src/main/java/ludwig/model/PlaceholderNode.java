package ludwig.model;

public class PlaceholderNode extends Node<PlaceholderNode> {
    private final String parameter;

    public PlaceholderNode(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPlaceholder(this);
    }

    @Override
    public String toString() {
        return "<" + parameter + ">";
    }
}
