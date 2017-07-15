package foo.runtime;

import foo.interpreter.Callable;
import foo.interpreter.Name;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Iterator;

public class StdLib {
    public static String str(Object x) {
        return String.valueOf(x);
    }

    public static double exp(Number x) {
        return Math.exp(x.doubleValue());
    }

    public static Number plus(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() + y.doubleValue();
        }
        return x.longValue() + y.longValue();
    }

    public static Number minus(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() - y.doubleValue();
        }
        return x.longValue() - y.longValue();
    }

    public static Iterable map(Iterable source, Callable f) {
        return () -> new Iterator() {
            Iterator it = source.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Object next() {
                return f.call(it.next());
            }
        };
    }

    @Name("to-list")
    public static PVector tolist(Iterable source) {
        if (source instanceof PVector) {
            return (PVector) source;
        }
        PVector v = TreePVector.empty();
        for (Object x: source) {
            v = v.plus(x);
        }
        return v;
    }
}
