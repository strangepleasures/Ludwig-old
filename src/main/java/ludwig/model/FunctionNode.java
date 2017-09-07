package ludwig.model;


public class FunctionNode extends NamedNode<FunctionNode>  {
    private boolean lazy;
    private Visibilities visibility = Visibilities.PUBLIC;

    public boolean lazy() {
        return lazy;
    }

    public FunctionNode lazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public Visibilities visibility() {
        return visibility;
    }

    public FunctionNode visibility(Visibilities visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }

    public String signature() {
        StringBuilder builder = new StringBuilder(name());
        for (Node node: children()) {
            if (!(node instanceof VariableNode)) {
                break;
            }
            builder.append(' ').append(node);
        }
        return builder.toString();
    }
}
