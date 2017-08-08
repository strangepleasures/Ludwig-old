package ludwig.model;


public class FunctionNode extends NamedNode<FunctionNode> {
    private boolean lazy;

    public boolean isLazy() {
        return lazy;
    }

    public FunctionNode setLazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }

    public String signature() {
        StringBuilder builder = new StringBuilder(getName());
        for (Node node: children()) {
            if (node instanceof SeparatorNode) {
                break;
            }
            builder.append(' ').append(node);
        }
        return builder.toString();
    }
}
