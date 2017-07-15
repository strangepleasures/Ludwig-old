package foo.changes;

import lombok.Value;

@Value
public class Binding implements Destination {
    String parent;
    String parameter;
}
