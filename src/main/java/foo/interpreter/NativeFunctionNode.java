package foo.interpreter;

import foo.model.FunctionNode;
import foo.model.ParameterNode;

import java.lang.reflect.*;
import java.util.stream.Stream;

public class NativeFunctionNode extends FunctionNode implements Callable {
    private final Method method;
    private final Class[] paramTypes;

    public NativeFunctionNode(Method method) {
        this.method = method;
        this.paramTypes = method.getParameterTypes();

        String methodName = method.isAnnotationPresent(Name.class) ? method.getAnnotation(Name.class).value() : method.getName();
        name(methodName);

        String packageName;
        if (method.getDeclaringClass().isAnnotationPresent(Name.class)) {
            packageName = method.getDeclaringClass().getAnnotation(Name.class).value();
        } else {
            packageName = method.getDeclaringClass().getSimpleName().toLowerCase();
        }

        id(packageName + ":" + methodName);

        if (method.isAnnotationPresent(Description.class)) {
            setComment(method.getAnnotation(Description.class).value());
        }

        for (Parameter parameter : method.getParameters()) {
            ParameterNode param = new ParameterNode();
            String paramName = parameter.isAnnotationPresent(Name.class) ? parameter.getAnnotation(Name.class).value() : parameter.getName();
            param.name(paramName)
                .id(id() + ":" + paramName);
            if (parameter.isAnnotationPresent(Description.class)) {
                param.setComment(parameter.getAnnotation(Description.class).value());
            }
            parameters().add(param);
        }
    }

    @Override
    public Object call(Object[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                args[i] = cast(args[i], paramTypes[i]);
            }
            return method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object cast(Object o, Class<?> type) {
        if (o == null) {
            return null;
        }
        if (type.isInstance(o)) {
            return o;
        }

        if (type == Double.class || type == double.class) {
            ((Number) o).doubleValue();
        }

        if (o instanceof Callable) {
            Callable callable = (Callable) o;
            Method theMethod = Stream.of(type.getDeclaredMethods())
                .filter(m -> !m.isDefault())
                .findFirst()
                .get();
            return Proxy.newProxyInstance(Callable.class.getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
                if (method.equals(theMethod)) {
                    return callable.call(args);
                } else {
                    return method.invoke(callable, args);
                }
            });
        }

        return type.cast(o);
    }
}
