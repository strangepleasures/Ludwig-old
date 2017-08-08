package ludwig.interpreter;

import lombok.Value;
import ludwig.model.FunctionNode;

import java.util.function.*;

@Value
public class Property<T, R>  implements Accessor<T, R> {
    private final Function<? super T, ? extends R> getter;
    private final BiConsumer<? super T, ? super R> setter;

    @Override
    public R get(T it) {
        return getter.apply(it);
    }

    @Override
    public void set(T it, R value) {
        setter.accept(it, value);
    }
}
