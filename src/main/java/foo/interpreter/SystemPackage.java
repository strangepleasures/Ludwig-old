package foo.interpreter;

import foo.model.PackageNode;
import foo.runtime.StdLib;

import java.lang.reflect.Method;

public class SystemPackage extends PackageNode {
    public SystemPackage(Class<?> clazz) {
        String packageName;
        if (clazz.isAnnotationPresent(Name.class)) {
            packageName = clazz.getAnnotation(Name.class).value();
        } else {
            packageName = clazz.getSimpleName().toLowerCase();
        }
        setName(packageName);
        setId(packageName);

        for (Method method : StdLib.class.getDeclaredMethods()) {
            getItems().add(new NativeFunctionNode(method));
        }
    }
}

