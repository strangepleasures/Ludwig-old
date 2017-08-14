package ludwig.changes;

public abstract class Insert<T extends Insert> extends Change<T> {
    String parent;
    String prev;
    String next;

    public String parent() {
        return parent;
    }

    public T parent(String parent) {
        this.parent = parent;
        return (T) this;
    }

    public String prev() {
        return prev;
    }

    public T prev(String prev) {
        this.prev = prev;
        return (T) this;
    }

    public String next() {
        return next;
    }

    public T next(String next) {
        this.next = next;
        return (T) this;
    }
}
