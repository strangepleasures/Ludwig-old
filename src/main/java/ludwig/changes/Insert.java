package ludwig.changes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Insert extends Change {
    String parent;
    String prev;
    String next;
}
