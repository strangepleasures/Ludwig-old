package foo.model;

import java.util.HashMap;
import java.util.Map;

public class BoundCallNode extends Node {
    private final Map<ParameterNode, Node> arguments = new HashMap<>();

    public Map<ParameterNode, Node> arguments() {
        return arguments;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBoundCall(this);
    }
}
