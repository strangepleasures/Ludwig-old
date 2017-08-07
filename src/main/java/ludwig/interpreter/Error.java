package ludwig.interpreter;

public class Error implements Signal {
    private final String message;

    public Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
