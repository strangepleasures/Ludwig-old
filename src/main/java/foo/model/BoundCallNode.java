package foo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class BoundCallNode extends Node {
    @JsonIgnore
    private final Map<ParameterNode, Node> arguments = new HashMap<>();

    public Map<ParameterNode, Node> arguments() {
        return arguments;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBoundCall(this);
    }
}
