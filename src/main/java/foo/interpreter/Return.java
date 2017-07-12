package foo.interpreter;

public class Return implements Signal {
    private final Object value;

    public Return(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
