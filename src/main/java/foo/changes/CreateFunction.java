package foo.changes;

import lombok.Value;

@Value
public class CreateFunction extends Change {
    String id;
    String name;
    String parentId;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitCreateFunction(this);
    }
}
