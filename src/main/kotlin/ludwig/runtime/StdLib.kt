package ludwig.runtime

import ludwig.interpreter.*
import ludwig.model.Visibilities
import org.pcollections.OrderedPSet
import org.pcollections.POrderedSet
import org.pcollections.PSet
import org.pcollections.PVector

@Name("system")
object StdLib {


    fun type(o: Any?): ClassType? {
        when (o) {
            null -> return ClassType.byName("system:Null")
            is Instance -> return o.type()
            is String -> return ClassType.byName("system:String")
            is PVector<*> -> return ClassType.byName("system:List")
            is PSet<*> -> return ClassType.byName("system:Set")
            is Iterable<*> -> return ClassType.byName("system:Sequence")
            is Boolean -> return ClassType.byName("system:Boolean")
            is Long -> return ClassType.byName("system:Integer")
            is Double -> return ClassType.byName("system:Real")
            is Callable -> return ClassType.byName("system:Function")
            is ClassType -> return ClassType.byName("system:Class")
            else -> return if (o is ErrorInfo) {
                ClassType.byName("system:Error")
            } else ClassType.byName("system:Any")
        }
    }


    @Description("Exponent")
    fun exp(x: Double): Double {
        return Math.exp(x)
    }

    fun sin(x: Double): Double {
        return Math.sin(x)
    }

    fun cos(x: Double): Double {
        return Math.cos(x)
    }

    fun tan(x: Double): Double {
        return Math.tan(x)
    }


    @Name("+")
    fun plus(x: Number, y: Number): Number {
        return if (x is Double || y is Double) {
            x.toDouble() + y.toDouble()
        } else x.toLong() + y.toLong()
    }

    @Name("-")
    fun minus(x: Number, y: Number): Number {
        return if (x is Double || y is Double) {
            x.toDouble() - y.toDouble()
        } else x.toLong() - y.toLong()
    }

    @Name("*")
    fun multiply(x: Number, y: Number): Number {
        return if (x is Double || y is Double) {
            x.toDouble() * y.toDouble()
        } else x.toLong() * y.toLong()
    }

    @Name("/")
    fun divide(x: Number, y: Number): Double {
        return x.toDouble() / y.toDouble()
    }

    fun div(x: Number, y: Number): Long {
        return if (x is Double || y is Double) {
            Math.round(x.toDouble() / y.toDouble())
        } else x.toLong() / y.toLong()
    }

    fun mod(x: Number, y: Number): Long {
        return if (x is Double || y is Double) {
            Math.round(x.toDouble() % y.toDouble())
        } else x.toLong() % y.toLong()
    }

    @Name("<=")
    fun ordered(x: Any?, y: Any?): Boolean {
        if (x == y) {
            return true
        }

        return if (x is Double || y is Double) {
            (x as Number).toDouble() <= (y as Number).toDouble()
        } else x is Comparable<*> && x.javaClass.isInstance(y) && (x as Comparable<Any>).compareTo(y!!) <= 0

    }

    @Name("is-empty")
    fun isEmpty(seq: Iterable<*>): Boolean {
        return !seq.iterator().hasNext()
    }

    fun head(seq: Iterable<*>): Any? {
        return seq.iterator().next()
    }

    fun <E> tail(seq: Iterable<E>): Iterable<E> {
        if (seq is List<*>) {
            val list = seq as List<E>
            return list.subList(1, list.size)
        }
        if (seq is Cons<*>) {
            return (seq as Cons<E>).tail.get()
        } else {
            return Iterable {
                val i = seq.iterator()
                if (i.hasNext()) {
                    i.next()
                }
                i
            }
        }
    }

    fun insert(list: PVector<Any?>, index: Long, x: Any?): PVector<*> {
        return list.plus(index.toInt(), x)
    }

    @SideEffects
    fun print(o: Any) {
        print(o)
    }

    @Name("to-set")
    fun <E> toSet(source: Iterable<E>): POrderedSet<E> {
        if (source is POrderedSet<*>) {
            return source as POrderedSet<E>
        }
        var result: POrderedSet<E> = OrderedPSet.empty()
        for (i in source) {
            result = result.plus(i)
        }
        return result
    }

    @Lazy
    fun <T> cons(head: Delayed<T>, tail: Delayed<Iterable<T>>): Iterable<T> {
        return Cons(head, tail)
    }

    @Lazy
    @Name("lazy-seq")
    fun <T> lazySequence(seq: Delayed<Iterable<T>>): Iterable<T> {
        return object : Iterable<T> {
            internal var inner: Iterable<T>? = null

            override fun iterator(): Iterator<T> {
                if (inner == null) {
                    inner = seq.get()
                }
                return inner!!.iterator()
            }
        }
    }

    @Name("arg-count")
    fun argCount(callable: Callable): Long {
        return callable.argCount().toLong()
    }

    fun StringBuilder(): Concatenator {
        return Concatenator()
    }

    @Name("build-string")
    fun buildString(concatenator: Concatenator): String {
        return concatenator.toString()
    }

    fun <E> out(obj: E, dest: (E) -> Unit): (E) -> Unit {
        dest(obj)
        return dest
    }

    fun error(): ErrorInfo {
        return ErrorInfo(Error.error())
    }

    @Name("list-get")
    @Visibility(Visibilities.PRIVATE)
    fun <T> listGet(list: List<T>, index: Long): T {
        return list[index.toInt()]
    }
}
