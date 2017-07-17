package foo.model;

import foo.utils.LiteralParser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiteralNode extends Node {
    private final String text;
    private final Object value;

    public LiteralNode(String text) {
        this(text, LiteralParser.parse(text));
    }

    private LiteralNode(String text, Object value) {
        this.text = text;
        this.value = LiteralParser.parse(text);
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

    public static LiteralNode ofValue(Object value) {
        return new LiteralNode(LiteralParser.toString(value), value);
    }
}
