package ludwig.runtime

import ludwig.interpreter.Delayed

internal class ConsIterator<T>(private var head: Delayed<T>?, private var tail: Delayed<Iterable<T>>?) : Iterator<T> {
    private var first = true
    private var it: Iterator<T>? = null

    override fun hasNext(): Boolean {
        if (first) {
            return true
        }
        if (it == null) {
            val i = tail!!.get().iterator()
            if (i is ConsIterator<T>) {
                first = true
                head = i.head
                tail = i.tail
                return true
            }
            it = i
        }
        return it!!.hasNext()
    }

    override fun next(): T {
        if (first) {
            first = false
            return head!!.get()
        }
        if (it == null) {
            val i = tail!!.get().iterator()
            if (i is ConsIterator<T>) {
                head = i.head
                tail = i.tail
                return head!!.get()
            }
            it = i
        }
        return it!!.next()
    }
}
