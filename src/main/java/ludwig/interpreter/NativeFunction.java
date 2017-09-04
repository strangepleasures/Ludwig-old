package ludwig.interpreter;

import java.lang.reflect.*;

public class NativeFunction implements Callable {
    private final Method method;
    private final Class[] paramTypes;
    private final boolean lazy;
    private static final Object[] EMPTY = {};

    public NativeFunction(Method method) {
        this.method = method;
        this.paramTypes = method.getParameterTypes();

        lazy = method.isAnnotationPresent(Lazy.class);
    }

    @Override
    public Object tail(Object[] args) {
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
