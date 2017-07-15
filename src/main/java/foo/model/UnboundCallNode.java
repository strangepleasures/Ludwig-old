package foo.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UnboundCallNode extends Node {
    private Node function;
    private final List<Node> arguments = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitUnboundCall(this);
    }
}
