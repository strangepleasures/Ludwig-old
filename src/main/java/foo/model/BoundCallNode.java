package foo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class BoundCallNode extends Node {
    private final Map<ParameterNode, Node> arguments = new HashMap<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBoundCall(this);
    }
}
