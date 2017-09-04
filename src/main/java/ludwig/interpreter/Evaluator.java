package ludwig.interpreter;

import ludwig.model.*;
import ludwig.runtime.StdLib;
import org.pcollections.HashPMap;
import org.pcollections.TreePVector;

import static ludwig.utils.NodeUtils.isField;

class Evaluator implements NodeVisitor<Object> {
    private HashPMap<NamedNode, Object> locals;
    private boolean doElse;
    private Error error;

    Evaluator(HashPMap<NamedNode, Object> locals) {
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
        Node<?> head = referenceNode.ref();

        boolean isLazy = (head instanceof FunctionNode) && ((FunctionNode) head).isLazy();
        Object[] args = new Object[referenceNode.children().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = isLazy ? new Return(referenceNode.children().get(i), locals) : referenceNode.children().get(i).accept(this);
        }

        return untail(tail(head, args));
    }

    @Override
    public Object visitFunctionReference(FunctionReferenceNode functionReference) {
        Node node = ((ReferenceNode) functionReference.children().get(0)).ref();
        if (!(node instanceof Callable)) {
            return new CallableRef(node);
        }
        return node;
    }

    @Override
    public Object visitThrow(ThrowNode throwNode) {
        return Error.error(throwNode.children().get(0).accept(this).toString());
    }

    @Override
    public Object visitTry(TryNode tryNode) {
        error = null;
        Object result = null;
        for (Node node : tryNode.children()) {
            try {
                result = node.accept(this);
                if (result instanceof Signal) {
                    break;
                }
            } catch (Exception e) {
                error = new Error(e.getMessage());
                return null;
            }
        }
        if (result instanceof Error) {
            error = (Error) result;
            return null;
        } else {
            return result;
        }
    }

    @Override
    public Object visitPlaceholder(PlaceholderNode placeholderNode) {
        return null;
    }

    @Override
    public Object visitBreak(BreakNode breakNode) {
        return new Break((NamedNode) ((ReferenceNode) breakNode.children().get(0)).ref());
    }

    @Override
    public Object visitContinue(ContinueNode continueNode) {
        return new Continue((NamedNode) ((ReferenceNode) continueNode.children().get(0)).ref());
    }

    @Override
    public Object visitOverride(OverrideNode overrideNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitClass(ClassNode classNode) {
        return null;
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
            if (isField(lhs)) {
                Instance instance = (Instance) assignmentNode.children().get(0).children().get(0).accept(this);
                instance.set((VariableNode) lhs, value);
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

    Object tail(Node<?> head, Object[] args) {
        Node<?> impl = head;

        if (head instanceof ClassNode) {
            ClassType type = ClassType.of((ClassNode) head);
            Instance instance = new Instance(type);
            for (int i = 0; i < args.length; i++) {
                instance.set(type.fields().get(i), args[i]);
            }
            return instance;
        }

        if (args.length > 0 && args[0] instanceof Instance) {
            impl = (Node<?>) StdLib.type(args[0]).implementation(head);
        }

        if (isField(impl)) {
            return ((Instance) args[0]).get((VariableNode) impl);
        }
        if (impl instanceof NativeFunctionNode) {
            return ((NativeFunctionNode) impl).eval(args);
        }
        if (head instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) head;
            HashPMap<NamedNode, Object> savedLocals = locals;

            try {
                for (int i = 0; i < args.length; i++) {
                    locals = locals.plus((NamedNode) fn.children().get(i), args[i]);
                }

                Object result = null;

                if (impl instanceof OverrideNode) {
                    for (int i = 1; i < impl.children().size(); i++) {
                        result = impl.children().get(i).accept(this);
                        if (result instanceof Signal) {
                            break;
                        }
                    }
                } else {
                    for (int i = args.length; i < fn.children().size(); i++) {
                        result = fn.children().get(i).accept(this);
                        if (result instanceof Signal) {
                            break;
                        }
                    }

                }

                return result;
            } finally {
                locals = savedLocals;
            }
        }
        if (head instanceof OverrideNode) {
            FunctionNode fn = (FunctionNode) ((ReferenceNode) head.children().get(0)).ref();
            HashPMap<NamedNode, Object> savedLocals = locals;

            try {
                for (int i = 0; i < args.length; i++) {
                    locals = locals.plus((NamedNode) fn.children().get(i), args[i]);
                }

                Object result = null;

                if (impl instanceof OverrideNode) {
                    for (int i = 1; i < impl.children().size(); i++) {
                        result = impl.children().get(i).accept(this);
                        if (result instanceof Signal) {
                            break;
                        }
                    }
                }

                return result;
            } finally {
                locals = savedLocals;
            }
        }

        return locals.get(head);
    }
}
