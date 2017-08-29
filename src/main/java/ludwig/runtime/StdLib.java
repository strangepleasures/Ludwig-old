package ludwig.runtime;

import ludwig.interpreter.*;
import org.pcollections.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Name("system")
public class StdLib {


    public static ClassType type(Object o) {
        if (o == null) {
            return ClassType.byName("system:Null");
        }
        if (o instanceof Instance) {
            return ((Instance) o).type();
        }
        if (o instanceof String) {
            return ClassType.byName("system:String");
        }
        if (o instanceof PVector) {
            return ClassType.byName("system:List");
        }
        if (o instanceof PSet) {
            return ClassType.byName("system:Set");
        }
        if (o instanceof Iterable) {
            return ClassType.byName("system:Sequence");
        }
        if (o instanceof Boolean) {
            return ClassType.byName("system:Boolean");
        }
        if (o instanceof Long) {
            return ClassType.byName("system:Integer");
        }
        if (o instanceof Double) {
            return ClassType.byName("system:Real");
        }
        if (o instanceof Callable) {
            return ClassType.byName("system:Function");
        }
        if (o instanceof ClassType) {
            return ClassType.byName("system:Class");
        }
        return ClassType.byName("system:Any");
    }

    public static String str(Object x) {
        return String.valueOf(x);
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
        return list.plus((int)index, x);
    }

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

    public static Object get(Iterable seq, long n) {
        if (seq instanceof List) {
            return ((List) seq).get((int) n);
        }
        return StreamSupport.stream(seq.spliterator(), false).skip(n - 1).findFirst().get();
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

    public static String join(Iterable<?> it, String prefix, String delimiter, String suffix) {
        return StreamSupport.stream(it.spliterator(), false).map(String::valueOf).collect(Collectors.joining(delimiter, prefix, suffix));
    }

    @Name("arg-count")
    public long argCount(Callable callable) {
        return callable.argCount();
    }

}
