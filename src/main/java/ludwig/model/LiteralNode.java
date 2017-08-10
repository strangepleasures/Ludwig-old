package ludwig.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ludwig.utils.NodeUtils;

public class LiteralNode extends Node<LiteralNode> {
    private String text;
    @JsonIgnore
    private Object value;

    public LiteralNode() {
    }

    public LiteralNode(String text) {
        this(text, NodeUtils.parseLiteral(text));
    }

    private LiteralNode(String text, Object value) {
        this.text = text;
        this.value = value;
    }

    public String text() {
        return text;
    }

    public Object value() {
        if (value == null) {
            value = NodeUtils.parseLiteral(text);
        }
        return value;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

    public static LiteralNode ofValue(Object value) {
        return new LiteralNode(NodeUtils.formatLiteral(value), value);
    }

    @Override
    public String toString() {
        return text;
    }
}
