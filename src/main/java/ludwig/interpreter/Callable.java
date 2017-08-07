package ludwig.interpreter;

public interface Callable {
    default Object call(Object... args) {
        Object result = tail(args);

        if (result instanceof Delayed) {
            return ((Delayed) result).get();
        }

        return result;
    }

    Object tail(Object... args);

    default boolean isLazy() {
        return false;
    }

    int argCount();
}
