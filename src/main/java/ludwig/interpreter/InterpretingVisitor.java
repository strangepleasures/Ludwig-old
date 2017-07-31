package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.TreePVector;

import java.util.Map;

class InterpretingVisitor implements NodeVisitor<Object> {
    private HashPMap<NamedNode, Object> locals;
    private Map<NamedNode, Object> globals ;
    private boolean doElse;

    InterpretingVisitor(HashPMap<NamedNode, Object> locals, Map<NamedNode, Object> globals) {
        this.locals = locals;
        this.globals = globals;
    }

    @Override
    public Object visitBoundCall(BoundCallNode boundCallNode) {
        HashPMap<NamedNode, Object> savedLocals = locals;
        try {
            RefNode ref = (RefNode) boundCallNode.children().get(0);
            FunctionNode functionNode = (FunctionNode) ref.ref();


            if (functionNode instanceof NativeFunctionNode) {
                NativeFunctionNode fn = (NativeFunctionNode) functionNode;
                boolean delayed = fn.isLazy();

                Object[] args = functionNode
                    .parameters()
                    .stream()
                    .map(param -> boundCallNode.arguments().get(param))
                    .map(node -> delayed ? toClosure(node) : node.accept(this))
                    .toArray();

                return fn.call(args);
            }

            for (ParameterNode param : functionNode.parameters()) {
                locals = locals.plus(param, boundCallNode.arguments().get(param).accept(this));
            }

            Object result = null;
            for (Node node : functionNode.children()) {
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
        Object value = letNode.children().get(0).accept(this);
        locals = locals.plus(letNode, value);
        return value;
    }

    @Override
    public Object visitList(ListNode listNode) {
        TreePVector<Object> list = TreePVector.empty();
        for (Node item : listNode.children()) {
            list = list.plus(item.accept(this));
        }
        return list;
    }

    @Override
    public Object visitLiteral(LiteralNode literalNode) {
        return literalNode.value();
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
        NamedNode node = refNode.ref();

        if (locals.containsKey(node)) {
            return locals.get(node);
        }

        if (globals.containsKey(node)) {
            return globals.get(node);
        }

        if (node.getClass() == FunctionNode.class) {
            CallableFunction callableFunction = new CallableFunction((FunctionNode) node, globals);
            globals.put(node, callableFunction);
            return callableFunction;
        }

        if (node.getClass() == LetNode.class) {
            Object value = node.children().get(0).accept(this);
            globals.put(node, value);
            return value;
        }

        return node;
    }

    @Override
    public Object visitUnboundCall(UnboundCallNode unboundCallNode) {
        Callable callable = (Callable) unboundCallNode.children().get(0).accept(this);

        boolean delayed = callable.isLazy();

        Object[] args = unboundCallNode.children()
            .stream()
            .skip(1)
            .map(node -> delayed ? toClosure(node) : node.accept(this))
            .toArray();
        return callable.call(args);
    }

    @Override
    public Object visitLambda(LambdaNode lambdaNode) {
        return new Closure(locals, globals, lambdaNode);
    }

    @Override
    public Object visitReturn(ReturnNode returnNode) {
        return returnNode.children().isEmpty() ? Return.EMPTY : new Return(returnNode.children().get(0).accept(this));
    }

    @Override
    public Object visitProject(ProjectNode projectNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitIf(IfNode ifNode) {
        Boolean test = (Boolean) ifNode.children().get(0).accept(this);
        if (test) {
            Object result = null;
            for (Node node : ifNode.children()) {
                result = node.accept(this);
                if (result instanceof Signal) {
                    break;
                }
            }
            return result;
        } else {
            doElse = true;
        }
        return null;
    }

    @Override
    public Object visitAssignment(AssignmentNode assignmentNode) {
        RefNode ref = (RefNode) assignmentNode.children().get(0);
        return locals = locals.plus(ref.ref() , assignmentNode.children().get(1).accept(this));
    }

    @Override
    public Object visitElse(ElseNode elseNode) {
        if (doElse) {
            doElse = false;
            Object result = null;
            for (Node node : elseNode.children()) {
                result = node.accept(this);
                if (result instanceof Signal) {
                    break;
                }
            }

            return result;
        }
        return null;
    }

    @Override
    public Object visitFor(ForNode forNode) {
        for (Object var : (Iterable) forNode.children().get(0).accept(this)) {
            locals = locals.plus(forNode, var);
            for (int i = 1; i < forNode.children().size(); i++) {
                Object value = forNode.children().get(i).accept(this);
                if (value instanceof Signal) {
                    if (value instanceof Break) {
                        Break br = (Break) value;
                        if (br.getLoop() == forNode || br.getLoop() == null) {
                            break;
                        }
                    } else if (value instanceof Continue) {
                        Continue c = (Continue) value;
                        if (c.getLoop() == forNode || c.getLoop() == null) {
                            break;
                        }
                    }
                    return value;
                }
            }
        }
        return null;
    }

    private Closure toClosure(Node node) {
        LambdaNode lambdaNode = new LambdaNode();
        lambdaNode.children().add(node);
        return new Closure(locals, globals, lambdaNode);
    }
}
