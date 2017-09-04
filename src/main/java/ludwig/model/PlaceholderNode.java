package ludwig.model;

public class PlaceholderNode extends Node<PlaceholderNode> {
    public String parameter() {
        return parameter;
    }

    public PlaceholderNode parameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    private String parameter;



    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPlaceholder(this);
    }

    @Override
    public String toString() {
        return "<" + parameter + ">";
    }
}
