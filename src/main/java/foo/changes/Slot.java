package foo.changes;

import lombok.Value;

@Value
public class Slot implements Destination {
    String parent;
}
