package ludwig.model;

public abstract class NamedNode extends Node implements Named {
    private String name;

    @Override
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
