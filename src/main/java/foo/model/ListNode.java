package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitList(this);
    }
}
