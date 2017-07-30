package ludwig.interpreter;

public interface Callable {
    Object call(Object... args);

    default boolean isDelayed() {
        return false;
    }

    int argCount();
}
