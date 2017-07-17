package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class NamedNode extends Node {
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
