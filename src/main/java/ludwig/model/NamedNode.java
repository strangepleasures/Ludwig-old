package ludwig.model;

public abstract class NamedNode<T extends NamedNode> extends Node<T> {
    private String name;

    public String name() {
        return name;
    }

    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    @Override
    public String toString() {
        return name;
    }
}
