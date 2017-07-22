package foo.utils;

import foo.model.Node;
import lombok.Getter;

public class CodeLine {
    @Getter
    private Node node;
    private StringBuilder builder = new StringBuilder();

    public CodeLine(Node node) {
        this.node = node;
    }

    public void append(String s) {
        builder.append(s);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
