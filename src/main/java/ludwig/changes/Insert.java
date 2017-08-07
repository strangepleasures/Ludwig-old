package ludwig.changes;

public abstract class Insert extends Change {
    String parent;
    String prev;
    String next;

    public String getParent() {
        return parent;
    }

    public Insert setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getPrev() {
        return prev;
    }

    public Insert setPrev(String prev) {
        this.prev = prev;
        return this;
    }

    public String getNext() {
        return next;
    }

    public Insert setNext(String next) {
        this.next = next;
        return this;
    }
}
