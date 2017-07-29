package foo.interpreter;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class Var implements Callable {
    private Object value;

    @Override
    public Object call(Object... args) {
        return value;
    }

    @Override
    public int argCount() {
        return 0;
    }
}
