package foo.interpreter;

import foo.model.*;
import org.pcollections.HashPMap;

public class NativeNode extends FunctionNode {
    private final Statement statement;

    protected NativeNode(String signature, Statement statement) {
        String[] parsedSignature = signature.split(" ");
        setName(parsedSignature[0]);
        for (int i = 1; i < parsedSignature.length; i++) {
            ParameterNode param = new ParameterNode();
            param.setName(parsedSignature[i]);
            getParameters().add(param);
        }
        this.statement = statement;
    }

    public Object eval(Interpreter interpreter, HashPMap<NamedNode, Object> locals, Node[] arguments) {
        return statement.eval(interpreter, locals, arguments);
    }
}
