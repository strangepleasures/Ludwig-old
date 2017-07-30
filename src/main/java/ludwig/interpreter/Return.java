package ludwig.interpreter;

public class Return implements Signal {
    public static final Return EMPTY = new Return(null);

    private final Object value;

    public Return(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
