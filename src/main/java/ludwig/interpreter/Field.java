package ludwig.interpreter;

public class Field<R> implements Accessor<Instance, R> {
    @Override
    public R get(Instance it) {
        return it.get(this);
    }

    @Override
    public void set(Instance it, R value) {
        it.set(this, value);
    }
}
