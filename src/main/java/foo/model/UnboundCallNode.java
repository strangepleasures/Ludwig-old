package foo.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class UnboundCallNode extends Node implements ListLike {
    private final List<Node> nodes = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitUnboundCall(this);
    }
}
