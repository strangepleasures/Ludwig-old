package ludwig.interpreter;

import ludwig.model.LetNode;
import ludwig.model.LiteralNode;

import java.lang.reflect.Field;

public class NativeLetNode extends LetNode {
    public NativeLetNode(Field field) {
        String name = field.isAnnotationPresent(Name.class) ? field.getAnnotation(Name.class).value() : field.getName();
        setName(name);

        String packageName;
        if (field.getDeclaringClass().isAnnotationPresent(Name.class)) {
            packageName = field.getDeclaringClass().getAnnotation(Name.class).value();
        } else {
            packageName = field.getDeclaringClass().getSimpleName().toLowerCase();
        }

        id(packageName + ":" + name);

        try {
            add(LiteralNode.ofValue(field.get(null)));
        } catch (IllegalAccessException e) {
        }
    }

}
