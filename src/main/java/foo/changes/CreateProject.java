package foo.changes;

import lombok.Value;

@Value
public class CreateProject extends Change {
    String id;
    String name;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitCreateProject(this);
    }
}
