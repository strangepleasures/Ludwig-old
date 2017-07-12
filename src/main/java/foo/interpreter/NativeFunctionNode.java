package foo.interpreter;

import java.util.function.Function;

public class NativeFunctionNode extends NativeNode {
    public NativeFunctionNode(String signature, Function<Object[], Object> function) {
        super(signature, ((interpreter, locals, nodes) -> {
            Object[] args = new Object[nodes.length];
            for (int i = 0; i < args.length; i++) {
                args[i] = interpreter.eval(nodes[i], locals);
            }
            return function.apply(args);
        }));
    }
}
