package foo.interpreter;

import foo.model.*;
import org.pcollections.*;

import java.util.Objects;

public class Interpreter {
    public Object eval(Node node, HashPMap<NamedNode, Object> locals) {
        return node.accept(new InterpretingVisitor(locals));
    }

    public Object call(FunctionNode functionNode, Object... args) {
        CallNode callNode = new CallNode();
        callNode.setFunction(functionNode);

        for (int i = 0; i < args.length; i++) {
            callNode.getArguments().put(functionNode.getParameters().get(i), LiteralNode.ofValue(args[i]));
        }

        return eval(callNode, HashTreePMap.empty());
    }

    private class InterpretingVisitor implements NodeVisitor<Object> {
        private HashPMap<NamedNode, Object> locals;

        private InterpretingVisitor(HashPMap<NamedNode, Object> locals) {
            this.locals = locals;
        }

        @Override
        public Object visitCall(CallNode callNode) {
            HashPMap<NamedNode, Object> savedLocals = locals;
            try {
                FunctionNode functionNode = callNode.getFunction();
                if (functionNode instanceof NativeNode) {
                    Node[] args = callNode.getFunction()
                        .getParameters()
                        .stream()
                        .map(param -> callNode.getArguments().get(param))
                        .filter(Objects::nonNull) // TODO: reimplement
                        .toArray(Node[]::new);
                    return ((NativeNode) functionNode).eval(Interpreter.this, locals, args);
                }

                for (ParameterNode param: callNode.getFunction().getParameters()) {
                    locals = locals.plus(param, callNode.getArguments().get(param).accept(this));
                }

                Object result = null;
                for (Node node: functionNode.getBody()) {
                    result = node.accept(this);
                    if (result instanceof Signal) {
                        break;
                    }
                }

                if (result instanceof Return) {
                    return ((Return) result).getValue();
                }

                return result;
            } finally {
                locals = savedLocals;
            }
        }

        @Override
        public Object visitFunction(FunctionNode functionNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object visitLet(LetNode letNode) {
            Object value = letNode.getValue().accept(this);
            locals = locals.plus(letNode, value);
            return value;
        }

        @Override
        public Object visitList(ListNode listNode) {
            TreePVector<Object> list = TreePVector.empty();
            for (Node item: listNode.getItems()) {
                list = list.plus(item.accept(this));
            }
            return list;
        }

        @Override
        public Object visitLiteral(LiteralNode literalNode) {
            return literalNode.getValue();
        }

        @Override
        public Object visitPackage(PackageNode packageNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object visitParameter(ParameterNode parameterNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object visitRef(RefNode refNode) {
            NamedNode node = refNode.getNode();
            return locals.getOrDefault(node, node);
        }
    }
}
