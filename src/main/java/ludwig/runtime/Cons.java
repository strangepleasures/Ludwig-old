package ludwig.runtime;

import ludwig.interpreter.Delayed;

import java.util.Iterator;

class Cons<T> implements Iterable<T> {
    private final Delayed<T> head;
    private final Delayed<Iterable<T>> tail;

    Cons(Delayed<T> head, Delayed<Iterable<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public Iterator<T> iterator() {
        return new ConsIterator<>(head, tail);
    }

    Delayed<T> getHead() {
        return head;
    }

    Delayed<Iterable<T>> getTail() {
        return tail;
    }
}
