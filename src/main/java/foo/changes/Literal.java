package foo.changes;

import lombok.Value;

@Value
public class Literal extends Change {
    String id;
    String value;
    Destination destination;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}
