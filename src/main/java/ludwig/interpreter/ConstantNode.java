package ludwig.interpreter;

import ludwig.model.*;

import java.lang.reflect.Field;

public class ConstantNode extends AssignmentNode {
    public ConstantNode(Field field) {
        String name = field.isAnnotationPresent(Name.class) ? field.getAnnotation(Name.class).value() : field.getName();
        VariableNode lhs = new VariableNode();
        lhs.setName(name);
        add(lhs);

        String packageName;
        if (field.getDeclaringClass().isAnnotationPresent(Name.class)) {
            packageName = field.getDeclaringClass().getAnnotation(Name.class).value();
        } else {
            packageName = field.getDeclaringClass().getSimpleName().toLowerCase();
        }

        lhs.id(packageName + ":" + name);
        id(lhs.id() + ":=");

        try {
            add(LiteralNode.ofValue(field.get(null)));
        } catch (IllegalAccessException e) {
        }
    }

}
