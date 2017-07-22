package foo.ide;

import foo.model.Node;
import lombok.Getter;

@Getter
public class CodeLine {
    private final Node node;
    private final String string;

    public CodeLine(Node node, String string) {
        this.node = node;
        this.string = string;
    }
}
