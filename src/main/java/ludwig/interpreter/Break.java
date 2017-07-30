package ludwig.interpreter;

import ludwig.model.NamedNode;
import lombok.Value;

@Value
public class Break implements Signal {
    NamedNode loop;
}
