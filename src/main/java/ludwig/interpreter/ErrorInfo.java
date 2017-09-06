package ludwig.interpreter;

public class ErrorInfo {
    private final Error error;

    public ErrorInfo(Error error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return error.toString();
    }
}
