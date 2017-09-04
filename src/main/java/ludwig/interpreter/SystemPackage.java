package ludwig.interpreter;

import ludwig.model.PackageNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SystemPackage {
    public static PackageNode of(Class<?> clazz) {
        PackageNode p = new PackageNode();
        String packageName;
        if (clazz.isAnnotationPresent(Name.class)) {
            packageName = clazz.getAnnotation(Name.class).value();
        } else {
            packageName = clazz.getSimpleName().toLowerCase();
        }
        p.name(packageName).id(packageName);

        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                p.add(new NativeFunctionNode(method));
            }
        }
        return p;
    }
}

