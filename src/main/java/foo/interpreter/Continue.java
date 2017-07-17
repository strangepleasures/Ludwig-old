package foo.interpreter;

import foo.model.NamedNode;
import lombok.Value;

@Value
public class Continue implements Signal {
    NamedNode loop;
}
