package foo.model;

public abstract class NamedNode extends Node {
    private String name;

    public String name() {
        return name;
    }

    public NamedNode name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
