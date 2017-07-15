package foo.interpreter;

import foo.model.FunctionNode;
import foo.model.ParameterNode;
import foo.utils.PrintUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class NativeFunctionNode extends FunctionNode implements Callable {
    private final Method method;

    public NativeFunctionNode(Method method) {
        setName(method.getName());
        setId("system:" + method.getName());
        for (Parameter parameter: method.getParameters()) {
            ParameterNode param = new ParameterNode();
            param.setName(parameter.getName());
            param.setId("system:" + method.getName() + ":" + parameter.getName());
            getParameters().add(param);
        }
        this.method = method;
    }

    @Override
    public Object call(Object[] args) {
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return PrintUtil.toString(this);
    }
}
