package ludwig.runtime;

import java.util.function.Consumer;

public class Concatenator implements Consumer<Object> {
    private final StringBuilder builder = new StringBuilder();

    @Override
    public void accept(Object o) {
        if (o instanceof String) {
            builder.append((String) o);
        } else if (o instanceof Double) {
            builder.append(((Double) o).doubleValue());
        } else if (o instanceof Long) {
            builder.append(((Long) o).longValue());
        } else {
            builder.append(o);
        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
