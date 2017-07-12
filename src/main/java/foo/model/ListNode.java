package foo.model;

import java.util.ArrayList;
import java.util.List;

public class ListNode extends Node {
    private final List<Node> items = new ArrayList<>();

    public List<Node> getItems() {
        return items;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitList(this);
    }
}
