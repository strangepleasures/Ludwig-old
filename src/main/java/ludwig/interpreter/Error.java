package ludwig.interpreter;

public class Error implements Signal {
    private static final ThreadLocal<Error> error = new ThreadLocal<>();

    private final String message;

    public Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error: " + message;
    }

    public static Error error(String message) {
        Error e = new Error(message);
        error.set(e);
        return e;
    }

    public static Error error() {
        Error e = error.get();
        error.remove();
        return e;
    }
}
