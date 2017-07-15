package foo.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListNode extends Node {
    private final List<Node> items = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitList(this);
    }
}
