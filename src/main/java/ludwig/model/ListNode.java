package ludwig.model;

public class ListNode extends Node<ListNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitList(this);
    }

    @Override
    public String toString() {
        return "list";
    }
}
