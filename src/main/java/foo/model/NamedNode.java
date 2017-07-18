package foo.model;

public abstract class NamedNode extends Node {
    private String name;

    public String getName() {
        return name;
    }

    public NamedNode setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
