package ludwig.interpreter;

import lombok.Value;
import ludwig.model.NamedNode;

@Value
public class Continue implements Signal {
    NamedNode loop;
}
