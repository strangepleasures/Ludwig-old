package ludwig.interpreter;

public interface Accessor<T, R> {
    R get(T it);
    void set(T it, R value);
}
