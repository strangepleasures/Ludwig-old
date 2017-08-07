package ludwig.changes;

public abstract class Insert<T extends Insert> extends Change<T> {
    String parent;
    String prev;
    String next;

    public String getParent() {
        return parent;
    }

    public T setParent(String parent) {
        this.parent = parent;
        return (T) this;
    }

    public String getPrev() {
        return prev;
    }

    public T setPrev(String prev) {
        this.prev = prev;
        return (T) this;
    }

    public String getNext() {
        return next;
    }

    public T setNext(String next) {
        this.next = next;
        return (T) this;
    }
}
