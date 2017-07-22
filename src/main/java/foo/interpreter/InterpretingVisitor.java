package foo.interpreter;

import foo.model.*;
import org.pcollections.HashPMap;

class InterpretingVisitor implements NodeVisitor<Object> {
    private HashPMap<NamedNode, Object> locals;
    private boolean doElse;

    InterpretingVisitor(HashPMap<NamedNode, Object> locals) {
        this.locals = locals;
    }

    @Override
    public Object visitBoundCall(BoundCallNode boundCallNode) {
        HashPMap<NamedNode, Object> savedLocals = locals;
        try {
            FunctionNode functionNode = (FunctionNode) boundCallNode.children().get(0);


            if (functionNode instanceof NativeFunctionNode) {
                NativeFunctionNode fn = (NativeFunctionNode) functionNode;
                boolean delayed = fn.isDelayed();

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

        if (node.getClass() == FunctionNode.class) {
            return new CallableFunction((FunctionNode) node);
        }

        return locals.getOrDefault(node, node);
    }

    @Override
    public Object visitUnboundCall(UnboundCallNode unboundCallNode) {
        Callable callable = (Callable) unboundCallNode.children().get(0).accept(this);

        boolean delayed = callable.isDelayed();

        Object[] args = unboundCallNode.children()
            .stream()
            .skip(1)
            .map(node -> delayed ? toClosure(node) : node.accept(this))
            .toArray();
        return callable.call(args);
    }

    @Override
    public Object visitLambda(LambdaNode lambdaNode) {
        return new Closure(locals, lambdaNode);
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
        return locals = locals.plus((NamedNode) assignmentNode.children().get(0), assignmentNode.children().get(1).accept(this));
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
        return new Closure(locals, lambdaNode);
    }
}
