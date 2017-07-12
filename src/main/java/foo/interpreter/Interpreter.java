package foo.interpreter;

import foo.model.*;
import org.pcollections.*;

public class Interpreter {
    public Object eval(Node node, HashPMap<NamedNode, Object> locals) {
        return node.accept(new InterpretingVisitor(locals));
    }

    public Object call(FunctionNode functionNode, Object... args) {
        BoundCallNode boundCallNode = new BoundCallNode();
        boundCallNode.setFunction(functionNode);

        for (int i = 0; i < args.length; i++) {
            boundCallNode.getArguments().put(functionNode.getParameters().get(i), LiteralNode.ofValue(args[i]));
        }

        return eval(boundCallNode, HashTreePMap.empty());
    }

    private class InterpretingVisitor implements NodeVisitor<Object> {
        private HashPMap<NamedNode, Object> locals;

        private InterpretingVisitor(HashPMap<NamedNode, Object> locals) {
            this.locals = locals;
        }

        @Override
        public Object visitBoundCall(BoundCallNode boundCallNode) {
            HashPMap<NamedNode, Object> savedLocals = locals;
            try {
                FunctionNode functionNode = boundCallNode.getFunction();

                if (functionNode instanceof NativeFunctionNode) {
                    Object[] args = boundCallNode.getFunction()
                        .getParameters()
                        .stream()
                        .map(param -> boundCallNode.getArguments().get(param).accept(this))
                        .toArray();
                    return ((NativeFunctionNode) functionNode).eval(args);
                }

                for (ParameterNode param: boundCallNode.getFunction().getParameters()) {
                    locals = locals.plus(param, boundCallNode.getArguments().get(param).accept(this));
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

        @Override
        public Object visitUnboundCall(UnboundCallNode unboundCallNode) {
            HashPMap<NamedNode, Object> savedLocals = locals;
            try {
                Object callable = unboundCallNode.getFunction().accept(this);

                if (callable instanceof NativeFunctionNode) {
                    Object[] args = unboundCallNode.getArguments()
                        .stream()
                        .map(arg -> arg.accept(this))
                        .toArray();

                    return ((NativeFunctionNode) callable).eval(args);
                }

                // TODO: refactor
                if (callable instanceof FunctionNode) {
                    FunctionNode functionNode = (FunctionNode) callable;
                    int index = 0;
                    for (ParameterNode param : functionNode.getParameters()) {
                        locals = locals.plus(param, unboundCallNode.getArguments().get(index++).accept(this));
                    }

                    Object result = null;
                    for (Node node : functionNode.getBody()) {
                        result = node.accept(this);
                        if (result instanceof Signal) {
                            break;
                        }
                    }

                    if (result instanceof Return) {
                        return ((Return) result).getValue();
                    }

                    return result;
                }

                // TODO: refactor
                if (callable instanceof Closure) {
                    Closure closure = (Closure) callable;
                    locals = closure.getLocals();
                    int index = 0;
                    for (ParameterNode param : closure.getParameters()) {
                        locals = locals.plus(param, unboundCallNode.getArguments().get(index++).accept(this));
                    }

                    Object result = null;
                    for (Node node : closure.getBody()) {
                        result = node.accept(this);
                        if (result instanceof Signal) {
                            break;
                        }
                    }

                    if (result instanceof Return) {
                        return ((Return) result).getValue();
                    }

                    return result;
                }

                throw new RuntimeException();
            } finally {
                locals = savedLocals;
            }

        }

        @Override
        public Object visitLambda(LambdaNode lambdaNode) {
            return new Closure(locals, lambdaNode.getParameters(), lambdaNode.getBody());
        }
    }
}
