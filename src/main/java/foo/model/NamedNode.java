package foo.model;

import lombok.Data;

@Data
public abstract class NamedNode extends Node {
    private String name;
}
