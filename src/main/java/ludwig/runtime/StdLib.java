package ludwig.runtime;

import ludwig.interpreter.*;
import org.pcollections.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Name("system")
public class StdLib {
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
            return ((Cons<E>) seq).tail.get();
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

    public static PVector plus(PVector list, int index, Object x) {
        return list.plus(index, x);
    }

    public static void print(Object o) {
        System.out.print(o);
    }

    @Name("to-list")
    public static <E> PVector<E> tolist(Iterable<E> source) {
        if (source instanceof PVector) {
            return (PVector<E>) source;
        }
        PVector<E> v = TreePVector.empty();
        for (E x : source) {
            v = v.plus(x);
        }
        return v;
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

    public static long size(Iterable<?> it) {
        if (it instanceof Collection) {
            return ((Collection) it).size();
        }
        int result = 0;
        for (Object x : it) {
            result++;
        }
        return result;
    }

    public static Callable bind(Callable callable, Object o) {
        return new Callable() {
            @Override
            public Object tail(Object... args) {
                Object[] a = new Object[args.length + 1];
                a[0] = isLazy() ? (Delayed) () -> o : o;
                System.arraycopy(args, 0, a, 1, args.length);
                return callable.tail(a);
            }

            @Override
            public int argCount() {
                return callable.argCount() - 1;
            }

            @Override
            public boolean isLazy() {
                return callable.isLazy() && argCount() > 0;
            }
        };
    }

    private static class Cons<T> implements Iterable<T> {
        private final Delayed<T> head;
        private final Delayed<Iterable<T>> tail;

        private Cons(Delayed<T> head, Delayed<Iterable<T>> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public Iterator<T> iterator() {
            return new ConsIterator<>(head, tail);
        }
    }

    private static class ConsIterator<T> implements Iterator<T> {
        private Delayed<T> head;
        private Delayed<Iterable<T>> tail;
        private boolean first = true;
        private Iterator<T> it;

        private ConsIterator(Delayed<T> head, Delayed<Iterable<T>> tail) {
            this.head = head;
            this.tail = tail;
        }


        @Override
        public boolean hasNext() {
            if (first) {
                return true;
            }
            if (it == null) {
                Iterator<T> i = tail.get().iterator();
                if (i instanceof ConsIterator) {
                    ConsIterator<T> ci = (ConsIterator<T>) i;
                    first = true;
                    head = ci.head;
                    tail = ci.tail;
                    return true;
                }
                it = i;
            }
            return it.hasNext();
        }

        @Override
        public T next() {
            if (first) {
                first = false;
                return head.get();
            }
            if (it == null) {
                Iterator<T> i = tail.get().iterator();
                if (i instanceof ConsIterator) {
                    ConsIterator<T> ci = (ConsIterator<T>) i;
                    head = ci.head;
                    tail = ci.tail;
                    return head.get();
                }
                it = i;
            }
            return it.next();
        }
    }
}
