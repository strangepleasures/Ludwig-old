package ludwig.runtime;

import ludwig.interpreter.*;
import ludwig.interpreter.Error;
import ludwig.model.Visibilities;
import org.pcollections.*;

import java.util.*;
import java.util.function.Consumer;

@Name("system")
public class StdLib {


    public static ClassType type(Object o) {
        if (o == null) {
            return ClassType.Companion.byName("system:Null");
        }
        if (o instanceof Instance) {
            return ((Instance) o).type();
        }
        if (o instanceof String) {
            return ClassType.Companion.byName("system:String");
        }
        if (o instanceof PVector) {
            return ClassType.Companion.byName("system:List");
        }
        if (o instanceof PSet) {
            return ClassType.Companion.byName("system:Set");
        }
        if (o instanceof Iterable) {
            return ClassType.Companion.byName("system:Sequence");
        }
        if (o instanceof Boolean) {
            return ClassType.Companion.byName("system:Boolean");
        }
        if (o instanceof Long) {
            return ClassType.Companion.byName("system:Integer");
        }
        if (o instanceof Double) {
            return ClassType.Companion.byName("system:Real");
        }
        if (o instanceof Callable) {
            return ClassType.Companion.byName("system:Function");
        }
        if (o instanceof ClassType) {
            return ClassType.Companion.byName("system:Class");
        }
        if (o instanceof ErrorInfo) {
            return ClassType.Companion.byName("system:Error");
        }
        return ClassType.Companion.byName("system:Any");
    }


    @Description("Exponent")
    public static double exp(double x) {
        return Math.exp(x);
    }

    public static double sin(double x) {
        return Math.sin(x);
    }

    public static double cos(double x) {
        return Math.cos(x);
    }

    public static double tan(double x) {
        return Math.tan(x);
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

    @Name("<=")
    public static boolean ordered(Object x, Object y) {
        if (Objects.equals(x, y)) {
            return true;
        }

        if (x instanceof Double || y instanceof Double) {
            return ((Number) x).doubleValue() <= ((Number) y).doubleValue();
        }

        return x instanceof Comparable && x.getClass().isInstance(y) && ((Comparable) x).compareTo(y) <= 0;
    }

    @Name("is-empty")
    public static boolean isEmpty(Iterable seq) {
        return !seq.iterator().hasNext();
    }

    public static Object head(Iterable seq) {
        return seq.iterator().next();
    }

    public static <E> Iterable<E> tail(Iterable<E> seq) {
        if (seq instanceof List) {
            List<E> list = (List<E>) seq;
            return list.subList(1, list.size());
        }
        if (seq instanceof Cons) {
            return ((Cons<E>) seq).getTail().get();
        } else {
            return () -> {
                Iterator<E> i = seq.iterator();
                if (i.hasNext()) {
                    i.next();
                }
                return i;
            };
        }
    }

    public static PVector insert(PVector list, long index, Object x) {
        return list.plus((int) index, x);
    }

    @SideEffects
    public static void print(Object o) {
        System.out.print(o);
    }

    @Name("to-set")
    public static <E> POrderedSet<E> toSet(Iterable<E> source) {
        if (source instanceof POrderedSet) {
            return (POrderedSet<E>) source;
        }
        POrderedSet<E> result = OrderedPSet.empty();
        for (E i : source) {
            result = result.plus(i);
        }
        return result;
    }

    @Lazy
    public static <T> Iterable<T> cons(Delayed<T> head, Delayed<Iterable<T>> tail) {
        return new Cons<>(head, tail);
    }

    @Lazy
    @Name("lazy-seq")
    public static <T> Iterable<T> lazySequence(Delayed<Iterable<T>> seq) {
        return new Iterable<T>() {
            Iterable<T> inner;

            @Override
            public Iterator<T> iterator() {
                if (inner == null) {
                    inner = seq.get();
                }
                return inner.iterator();
            }
        };
    }

    @Name("arg-count")
    public long argCount(Callable callable) {
        return callable.argCount();
    }

    public static Concatenator StringBuilder() {
        return new Concatenator();
    }

    @Name("build-string")
    public static String buildString(Concatenator concatenator) {
        return concatenator.toString();
    }

    public static <E, C extends Consumer<? super E>> C out(E obj, C dest) {
        dest.accept(obj);
        return dest;
    }

    public static ErrorInfo error() {
        return new ErrorInfo(Error.Companion.error());
    }

    @Name("list-get")
    @Visibility(Visibilities.PRIVATE)
    public static <T> T listGet(List<T> list, long index) {
        return list.get((int)index);
    }
}
