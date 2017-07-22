package foo.runtime;

import foo.interpreter.*;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Name("system")
public class StdLib {
    public static final double pi = Math.PI;
    @Name("true")
    public static final boolean TRUE = true;
    @Name("false")
    public static final boolean FALSE = true;
    @Name("null")
    public static final Object NULL = null;
    @Name(";")
    public static final TreePVector EMPTY = TreePVector.empty();

    public static String str(Object x) {
        return String.valueOf(x);
    }

    @Description("Exponent")
    public static double exp(Number x) {
        return Math.exp(x.doubleValue());
    }

    @Name("+")
    public static Number plus(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() + y.doubleValue();
        }
        return x.longValue() + y.longValue();
    }

    @Name("-")
    public static Number minus(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() - y.doubleValue();
        }
        return x.longValue() - y.longValue();
    }

    @Name("*")
    public static Number multiply(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() * y.doubleValue();
        }
        return x.longValue() * y.longValue();
    }

    @Name("/")
    public static double divide(Number x, Number y) {
        return x.doubleValue() / y.doubleValue();
    }

    public static long div(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return Math.round(x.doubleValue() / y.doubleValue());
        }
        return x.longValue() / y.longValue();
    }

    public static long mod(Number x, Number y) {
        if (x instanceof Double || y instanceof Double) {
            return Math.round(x.doubleValue() % y.doubleValue());
        }
        return x.longValue() % y.longValue();
    }

    public static Iterable map(Iterable source, Function f) {
        return () -> new Iterator() {
            Iterator it = source.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Object next() {
                return f.apply(it.next());
            }
        };
    }

    @Name("==")
    public static boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    @Name("<")
    public static boolean less(Comparable x, Comparable y) {
        return x.compareTo(y) < 0;
    }

    @Name(">")
    public static boolean greater(Comparable x, Comparable y) {
        return x.compareTo(y) > 0;
    }

    @Name("<=")
    public static boolean le(Comparable x, Comparable y) {
        return x.compareTo(y) <= 0;
    }

    @Name(">=")
    public static boolean ge(Comparable x, Comparable y) {
        return x.compareTo(y) >= 0;
    }

    @Delayed
    public static boolean and(Supplier<Boolean> x, Supplier<Boolean> y) {
        return x.get() && y.get();
    }

    @Delayed
    public static boolean or(Supplier<Boolean> x, Supplier<Boolean> y) {
        return x.get() || y.get();
    }

    public static boolean xor(boolean x, boolean y) {
        return x ^ y;
    }

    public static boolean not(boolean b) {
        return !b;
    }

    @Delayed
    @Name("?")
    public static Object iff(Supplier<Boolean> condition, Supplier<?> option1, Supplier<?> option2) {
        return condition.get() ? option1.get() : option2.get();
    }

    public static Number neg(Number x) {
        return (x instanceof Long) ? -x.longValue() : -x.doubleValue();
    }

//    @Name("list")§
//    public static PVector<?> $(Object... args) {
//        return TreePVector.from(Arrays.asList(args));
//    }

    public static Object head(Iterable it) {
        return it.iterator().next();
    }

    public static List tail(List list) {
        return list.subList(1, list.size());
    }

    @Name("empty?")
    public static boolean empty(Iterable x) {
        return !x.iterator().hasNext();
    }

    @Name(":")
    public static PVector prepend(Object x, PVector list) {
        return list.plus(0, x);
    }

    public static PVector append(PVector list, Object x) {
        return list.plus(x);
    }

    public static void println(Object o) {
        System.out.println(o);
    }

    @Name("to-list")
    public static PVector tolist(Iterable source) {
        if (source instanceof PVector) {
            return (PVector) source;
        }
        PVector v = TreePVector.empty();
        for (Object x : source) {
            v = v.plus(x);
        }
        return v;
    }
}
