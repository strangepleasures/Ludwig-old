package ludwig.interpreter;

public interface Callable {
    Object call(Object... args);

    default boolean isLazy() {
        return false;
    }

    int argCount();
}
