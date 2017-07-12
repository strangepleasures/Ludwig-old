package foo.model;

import java.util.ArrayList;
import java.util.List;

public class UnboundCallNode extends Node {
    private Node function;
    private final List<Node> arguments = new ArrayList<>();

    public Node getFunction() {
        return function;
    }

    public void setFunction(Node function) {
        this.function = function;
    }

    public List<Node> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitUnboundCall(this);
    }
}
