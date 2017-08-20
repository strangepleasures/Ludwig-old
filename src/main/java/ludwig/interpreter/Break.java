package ludwig.interpreter;

import lombok.Value;
import ludwig.model.NamedNode;

@Value
public class Break implements Signal {
    NamedNode loop;
}
