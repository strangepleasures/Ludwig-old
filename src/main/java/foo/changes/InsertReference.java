package foo.changes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsertReference extends Insert {
    String id;
    String ref;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitInsertReference(this);
    }
}
