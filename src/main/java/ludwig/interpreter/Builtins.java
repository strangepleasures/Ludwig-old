package ludwig.interpreter;

import ludwig.model.*;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class Builtins {
    private static final Map<FunctionNode, Callable> callables = new HashMap<>();

    public static Callable callable(FunctionNode fn) {
        return callables.get(fn);
    }

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
                FunctionNode fn = function(method);
                callables.put(fn, new NativeFunction(method));
                p.add(fn);
            }
        }
        return p;
    }

    private static FunctionNode function(Method method) {
        FunctionNode f = new FunctionNode();

        f.lazy(method.isAnnotationPresent(Lazy.class));

        String methodName = method.isAnnotationPresent(Name.class) ? method.getAnnotation(Name.class).value() : method.getName();
        f.name(methodName);

        String packageName;
        if (method.getDeclaringClass().isAnnotationPresent(Name.class)) {
            packageName = method.getDeclaringClass().getAnnotation(Name.class).value();
        } else {
            packageName = method.getDeclaringClass().getSimpleName().toLowerCase();
        }

        f.id(packageName + ":" + methodName);

        if (method.isAnnotationPresent(Description.class)) {
            f.comment(method.getAnnotation(Description.class).value());
        }

        if (method.isAnnotationPresent(Visibility.class)) {
            f.visibility(method.getAnnotation(Visibility.class).value());
        }

        for (Parameter parameter : method.getParameters()) {
            VariableNode param = new VariableNode();
            String paramName = parameter.isAnnotationPresent(Name.class) ? parameter.getAnnotation(Name.class).value() : parameter.getName();
            param.name(paramName)
                .id(f.id() + ":" + paramName);
            if (parameter.isAnnotationPresent(Description.class)) {
                param.comment(parameter.getAnnotation(Description.class).value());
            }
            f.add(param);
        }
        f.add(new PlaceholderNode().parameter("Built-in function").id(f.id() + ":body"));

        return f;
    }
}

