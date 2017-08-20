package ludwig.interpreter;

import ludwig.model.FunctionNode;
import ludwig.model.VariableNode;

import java.lang.reflect.*;

public class NativeFunctionNode extends FunctionNode implements Callable {
    private final Method method;
    private final Class[] paramTypes;
    private final boolean lazy;
    private static final Object[] EMPTY = {};

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
            comment(method.getAnnotation(Description.class).value());
        }

        for (Parameter parameter : method.getParameters()) {
            VariableNode param = new VariableNode();
            String paramName = parameter.isAnnotationPresent(Name.class) ? parameter.getAnnotation(Name.class).value() : parameter.getName();
            param.name(paramName)
                .id(id() + ":" + paramName);
            if (parameter.isAnnotationPresent(Description.class)) {
                param.comment(parameter.getAnnotation(Description.class).value());
            }
            add(param);
        }

        lazy = method.isAnnotationPresent(Lazy.class);
    }

    @Override
    public Object tail(Object[] args) {
        return new Evaluator(null).tail(this, args);
    }

    public Object eval(Object[] args) {
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
            return ((Number) o).doubleValue();
        }

        if (type == long.class) {
            return o;
        }

        if (o instanceof Callable) {
            Callable callable = (Callable) o;
            Method theMethod = functionalMethod(type);
            return Proxy.newProxyInstance(Callable.class.getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
                if (method.equals(theMethod)) {
                    if (args == null) {
                        args = EMPTY;
                    }
                    return callable.call(args);
                } else {
                    return method.invoke(callable, args);
                }
            });
        }

        return type.cast(o);
    }

    @Override
    public boolean isLazy() {
        return lazy;
    }

    @Override
    public int argCount() {
        return paramTypes.length;
    }

    private static Method functionalMethod(Class type) {
        if (type.isInterface()) {
            while (type != null) {
                for (Method m : type.getDeclaredMethods()) {
                    if (!m.isDefault()) {
                        return m;
                    }
                }
                type = type.getSuperclass();
            }
        }
        throw new RuntimeException("Cannot find a functional method in " + type);
    }
}
