package ludwig.changes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Delete extends Change {
    private String id;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitDelete(this);
    }
}
