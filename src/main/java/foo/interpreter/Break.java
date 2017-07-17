package foo.interpreter;

import foo.model.NamedNode;
import lombok.Value;

@Value
public class Break implements Signal {
    NamedNode loop;
}
