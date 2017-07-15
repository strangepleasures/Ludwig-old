package foo.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BoundCallNode extends Node {
    private FunctionNode function;

    private final Map<ParameterNode, Node> arguments = new HashMap<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBoundCall(this);
    }
}
