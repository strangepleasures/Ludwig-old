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
            if (i is ConsIterator<*>) {
                val ci = i as ConsIterator<T>
                first = true
                head = ci.head
                tail = ci.tail
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
            if (i is ConsIterator<*>) {
                val ci = i as ConsIterator<T>
                head = ci.head
                tail = ci.tail
                return head!!.get()
            }
            it = i
        }
        return it!!.next()
    }
}
