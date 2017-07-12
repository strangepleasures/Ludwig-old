package foo.interpreter;

import foo.model.FunctionNode;
import foo.model.ParameterNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodNode extends FunctionNode {
    private final Method method;

    public MethodNode(Method method) {
        setName(method.getName());
        for (Parameter parameter: method.getParameters()) {
            ParameterNode param = new ParameterNode();
            param.setName(parameter.getName());
            getParameters().add(param);
        }
        this.method = method;
    }

    public Object eval(Object[] args) {
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
