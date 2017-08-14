package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.TreePVector;

class InterpretingVisitor implements NodeVisitor<Object> {
    private HashPMap<NamedNode, Object> locals;
    private boolean doElse;

    InterpretingVisitor(HashPMap<NamedNode, Object> locals) {
        this.locals = locals;
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
    public Object visitVariable(VariableNode variableNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitReference(ReferenceNode referenceNode) {
        Node<?> node = referenceNode.ref();

        if (node instanceof NativeFunctionNode) {
            NativeFunctionNode fn = (NativeFunctionNode) node;
            boolean delayed = fn.isLazy();

            Object[] args = new Object[fn.argCount()];
            for (int i = 0; i < args.length; i++) {
                args[i] = delayed ? new Return(referenceNode.children().get(i), locals) : referenceNode.children().get(i).accept(this);
            }
            return untail(fn.tail(args));
        }

        if (node instanceof FunctionNode) {
            boolean lazy = ((FunctionNode) node).isLazy();
            HashPMap<NamedNode, Object> savedLocals = locals;
            try {
                Object result = null;
                boolean params = true;
                OverrideNode overrideNode = null;
                for (int i = 0; i < node.children().size(); i++) {
                    Node child = node.children().get(i);

                    if (params) {
                        if (child instanceof SeparatorNode) {
                            params = false;
                            if (overrideNode != null) {
                                for (int j = 1; j < overrideNode.children().size(); j++) {
                                    result = overrideNode.children().get(j).accept(this);
                                    if (result instanceof Signal) {
                                        break;
                                    }
                                }
                                return untail(result);
                            }
                        } else {
                            Node arg = referenceNode.children().get(i);
                            Object argValue = lazy ? new Return(arg, locals) : arg.accept(this);
                            if (i == 0 && argValue instanceof Instance) {
                                Signature implementation = ((Instance) argValue).type().implementation((Signature) node);
                                if (implementation instanceof OverrideNode) {
                                    overrideNode = (OverrideNode) implementation;
                                }
                            }
                            locals = locals.plus((VariableNode) child, argValue);
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

        if (node instanceof FieldNode) {
            Instance instance = (Instance) referenceNode.children().get(0).accept(this);
            return instance.get((FieldNode) node);
        }

        Object value = locals.get(node);
        if (value instanceof Delayed) {
            value = ((Delayed) value).get();
        }
        return value;
    }

    @Override
    public Object visitFunctionReference(FunctionReferenceNode functionReference) {
        Node node = ((ReferenceNode) functionReference.children().get(0)).ref();
        if (!(node instanceof Callable)) {
            if (node instanceof FunctionNode) {
                return new CallableFunction((FunctionNode) node);
            }
            if (node instanceof FieldNode) {
                return new CallableField((FieldNode) node);
            }
        }
        return node;
    }

    @Override
    public Object visitThrow(ThrowNode throwNode) {
        return new Error(throwNode.children().get(0).accept(this).toString());
    }

    @Override
    public Object visitPlaceholder(PlaceholderNode placeholderNode) {
        return null;
    }

    @Override
    public Object visitBreak(BreakNode breakNode) {
        return new Break(((ReferenceNode) breakNode.children().get(0)).ref());
    }

    @Override
    public Object visitContinue(ContinueNode continueNode) {
        return new Continue(((ReferenceNode) continueNode.children().get(0)).ref());
    }

    @Override
    public Object visitField(FieldNode fieldNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitOverride(OverrideNode overrideNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitCall(CallNode callNode) {
        Object head = callNode.children().get(0).accept(this);
        if (head instanceof Delayed) {
            return untail(((Delayed) head).get());
        }

        Callable callable = (Callable) head;

        boolean delayed = callable.isLazy();

        Object[] args = callNode.children()
            .stream()
            .skip(1)
            .map(node -> delayed ? new Return(node, locals) : node.accept(this))
            .toArray();

        return untail(callable.tail(args));
    }

    @Override
    public Object visitLambda(LambdaNode lambdaNode) {
        return new Closure(locals, lambdaNode);
    }

    @Override
    public Object visitReturn(ReturnNode returnNode) {
        return new Return(returnNode.children().get(0), locals);
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
        Object value = assignmentNode.children().get(1).accept(this);

        Node lhs = assignmentNode.children().get(0);
        if (lhs instanceof ReferenceNode) {
            lhs = ((ReferenceNode) lhs).ref();
            if (lhs instanceof FieldNode) {
                Instance instance = (Instance) assignmentNode.children().get(0).children().get(0).accept(this);
                instance.set((FieldNode) lhs, value);
                return value;
            }
        }

        locals = locals.plus((NamedNode) lhs, value);
        return value;
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
            VariableNode v = (VariableNode) forNode.children().get(0);
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
