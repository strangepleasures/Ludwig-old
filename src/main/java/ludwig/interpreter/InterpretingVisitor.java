package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.TreePVector;

import java.util.Map;

class InterpretingVisitor implements NodeVisitor<Object> {
    private HashPMap<NamedNode, Object> locals;
    private Map<NamedNode, Object> globals;
    private boolean doElse;

    InterpretingVisitor(HashPMap<NamedNode, Object> locals, Map<NamedNode, Object> globals) {
        this.locals = locals;
        this.globals = globals;
    }


    @Override
    public Object visitFunction(FunctionNode functionNode) {
        throw new UnsupportedOperationException();
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
    public Object visitVariable(VariableNode variableNode) {
        Node node = variableNode.ref();

        if (node instanceof NativeFunctionNode) {
            NativeFunctionNode fn = (NativeFunctionNode) node;
            boolean delayed = fn.isLazy();

            Object[] args = new Object[fn.argCount()];
            for (int i = 0; i < args.length; i++) {
                args[i] = delayed ? new Return(variableNode.children().get(i), locals, globals) : variableNode.children().get(i).accept(this);
            }
            return untail(fn.tail(args));
        }

        if (node instanceof FunctionNode) {
            HashPMap<NamedNode, Object> savedLocals = locals;
            try {
                FunctionNode fn = (FunctionNode) node;
                Object result = null;
                boolean params = true;
                for (int i = 0; i < fn.children().size(); i++) {
                    Node child = fn.children().get(i);
                    if (params) {
                        if (child instanceof SeparatorNode) {
                            params = false;
                        } else {
                            locals = locals.plus((ParameterNode) child, variableNode.children().get(i).accept(this));
                        }
                    } else {
                        result = child.accept(this);
                        if (result instanceof Signal) {
                            break;
                        }
                    }
                }
                return untail(result);
            } finally {
                locals = savedLocals;
            }
        }


        if (locals.containsKey(node)) {
            return locals.get(node);
        }

        if (globals.containsKey(node)) {
            return globals.get(node);
        }

        if (node instanceof AssignmentNode) {
            Object value = node.children().get(1).accept(this);
            globals.put((NamedNode) node.children().get(0), value);
            return value;
        }

        return node;
    }

    @Override
    public Object visitReference(ReferenceNode referenceNode) {
        FunctionNode node = (FunctionNode) referenceNode.children().get(0).accept(this);
        if (!(node instanceof Callable)) {
            CallableFunction callableFunction = (CallableFunction) globals.get(node);
            if (callableFunction == null) {
                callableFunction = new CallableFunction(node, globals);
                globals.put(node, callableFunction);
            }
            return callableFunction;
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
                .map(node -> delayed ? new Return(node, locals, globals) : node.accept(this))
                .toArray();

        return untail(callable.tail(args));
    }

    @Override
    public Object visitLambda(LambdaNode lambdaNode) {
        return new Closure(locals, globals, lambdaNode);
    }

    @Override
    public Object visitReturn(ReturnNode returnNode) {
        return new Return(returnNode.children().get(0), locals, globals);
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
        Node lhs = assignmentNode.children().get(0);
        if (lhs instanceof ReferenceNode) {
            lhs = ((VariableNode) lhs).ref();
        }
        return locals = locals.plus((NamedNode) lhs, assignmentNode.children().get(1).accept(this));
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
        for (Object var : (Iterable) forNode.children().get(1).accept(this)) {
            ParameterNode v = (ParameterNode) forNode.children().get(0);
            locals = locals.plus(v, var);
            for (int i = 1; i < forNode.children().size(); i++) {
                Object value = forNode.children().get(i).accept(this);
                if (value instanceof Signal) {
                    if (value instanceof Break) {
                        Break br = (Break) value;
                        if (br.getLoop() == v || br.getLoop() == null) {
                            break;
                        }
                    } else if (value instanceof Continue) {
                        Continue c = (Continue) value;
                        if (c.getLoop() == v || c.getLoop() == null) {
                            break;
                        }
                    }
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public Object visitSeparator(SeparatorNode separatorNode) {
        return null;
    }

    private Object untail(Object result) {
        while (result instanceof Return) {
            Return tail = (Return) result;
            HashPMap<NamedNode, Object> state = locals;
            try {
                locals = tail.getLocals();
                result = tail.getNode().accept(this);
            } finally {
                locals = state;
            }
        }
        return result;
    }
}
