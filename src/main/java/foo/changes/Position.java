package foo.changes;

import lombok.Value;

@Value
public class Position implements Destination {
    String parent;
    String prev;
    String next;
}
