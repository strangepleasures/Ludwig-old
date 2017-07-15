package foo.model;

public interface ValueHolder<T> {
    T getValue();

    void setValue(T value);
}
