package ludwig.interpreter;

import ludwig.model.NamedNode;
import lombok.Value;

@Value
public class Continue implements Signal {
    NamedNode loop;
}
