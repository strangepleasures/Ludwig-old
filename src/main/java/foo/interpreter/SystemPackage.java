package foo.interpreter;

import foo.model.PackageNode;

import java.lang.reflect.*;

public class SystemPackage extends PackageNode {
    public SystemPackage(Class<?> clazz) {
        String packageName;
        if (clazz.isAnnotationPresent(Name.class)) {
            packageName = clazz.getAnnotation(Name.class).value();
        } else {
            packageName = clazz.getSimpleName().toLowerCase();
        }
        setName(packageName).id(packageName);

        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                children().add(new NativeFunctionNode(method));
            }
        }

        for (Field field: clazz.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                children().add(new NativeLetNode(field));
            }
        }
    }
}

