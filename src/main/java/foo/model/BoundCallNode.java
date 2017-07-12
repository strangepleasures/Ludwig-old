package foo.model;

import java.util.HashMap;
import java.util.Map;

public class BoundCallNode extends Node {
    private FunctionNode function;

    private final Map<ParameterNode, Node> arguments = new HashMap<>();

    public FunctionNode getFunction() {
        return function;
    }

    public void setFunction(FunctionNode function) {
        this.function = function;
    }

    public Map<ParameterNode, Node> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBoundCall(this);
    }
}
