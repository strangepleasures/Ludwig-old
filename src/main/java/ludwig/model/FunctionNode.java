package ludwig.model;


import java.util.ArrayList;
import java.util.List;

public class FunctionNode extends NamedNode<FunctionNode> implements Signature {
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

    @Override
    public List<String> arguments() {
        List<String> args = new ArrayList<>();
        for (Node node: children()) {
            if (node instanceof SeparatorNode) {
                break;
            }
            args.add(node.toString());
        }
        return args;
    }
}
