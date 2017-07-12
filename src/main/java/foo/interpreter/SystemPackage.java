package foo.interpreter;

import foo.model.FunctionNode;
import foo.model.PackageNode;

import java.util.Arrays;
import java.util.function.Function;

public class SystemPackage extends PackageNode {
    public static final FunctionNode PLUS = fn("+ left right", args -> (Double)args[0] +  (Double)args[1]);
    public static final FunctionNode MINUS = fn("- left right", args -> (Double)args[0] -  (Double)args[1]);
    public static final FunctionNode RETURN = fn("return value", args -> new Return(args.length > 0 ? args[0] : null));
    public static final FunctionNode IF = st("if condition then else", (interpreter, locals, nodes) -> {
        Object test = interpreter.eval(nodes[0], locals);
        if ((Boolean) test) {
            return interpreter.eval(nodes[1], locals);
        } else if (nodes.length > 2) {
            return interpreter.eval(nodes[2], locals);
        } else {
            return null;
        }
    });


    public SystemPackage() {
        setName("system");

        getItems().addAll(Arrays.asList(
            RETURN,
            IF,


            PLUS,
            MINUS
        ));
    }

    private static NativeFunctionNode fn(String signature, Function<Object[], Object> function) {
        return new NativeFunctionNode(signature, function);
    }

    private static NativeNode st(String signature, Statement statement) {
        return new NativeNode(signature, statement);
    }
}
