package ludwig.runtime;

import ludwig.interpreter.Delayed;

import java.util.Iterator;

class ConsIterator<T> implements Iterator<T> {
    private Delayed<T> head;
    private Delayed<Iterable<T>> tail;
    private boolean first = true;
    private Iterator<T> it;

    ConsIterator(Delayed<T> head, Delayed<Iterable<T>> tail) {
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
