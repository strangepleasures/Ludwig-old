package ludwig.interpreter;

@FunctionalInterface
public interface Delayed<T> {
    T get();
}
