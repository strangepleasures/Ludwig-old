package ludwig.runtime

import ludwig.interpreter.Delayed

internal class Cons<T>(val head: Delayed<T>, val tail: Delayed<Iterable<T>>) : Iterable<T> {

    override fun iterator(): Iterator<T> {
        return ConsIterator(head, tail)
    }
}
