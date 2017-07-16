package foo.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OrNode extends Node implements ListLike {
    private final List<Node> nodes = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOr(this);
    }
}
