package foo.changes;

import java.util.UUID;

public abstract class Change {
    public abstract <T> T accept(ChangeVisitor<T> visitor);

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
