package ludwig.interpreter;

public class Error implements Signal {
    private static final ThreadLocal<Error> error = new ThreadLocal<>();

    private final String message;

    public Error(String message) {
        this.message = message;
        error.set(this);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error: " + message;
    }

    public static Error error() {
        return error.get();
    }

    public static void reset() {
        error.remove();
    }
}
