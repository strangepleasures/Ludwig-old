package foo.interpreter;

import foo.model.*;
import org.pcollections.HashPMap;
import org.pcollections.TreePVector;

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
            FunctionNode functionNode = (FunctionNode) boundCallNode.getChildren().get(0);

            if (functionNode instanceof NativeFunctionNode) {
                Object[] args = functionNode
                    .getParameters()
                    .stream()
                    .map(param -> boundCallNode.getArguments().get(param).accept(this))
                    .toArray();
                return ((NativeFunctionNode) functionNode).call(args);
            }

            for (ParameterNode param : functionNode.getParameters()) {
                locals = locals.plus(param, boundCallNode.getArguments().get(param).accept(this));
            }

            Object result = null;
            for (Node node : functionNode.getChildren()) {
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
        Object value = letNode.getChildren().get(0).accept(this);
        locals = locals.plus(letNode, value);
        return value;
    }

    @Override
    public Object visitList(ListNode listNode) {
        TreePVector<Object> list = TreePVector.empty();
        for (Node item : listNode.getChildren()) {
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
        NamedNode node = (NamedNode) refNode.getChildren().get(0);

        if (node.getClass() == FunctionNode.class) {
            return new CallableFunction((FunctionNode) node);
        }

        return locals.getOrDefault(node, node);
    }

    @Override
    public Object visitUnboundCall(UnboundCallNode unboundCallNode) {
        Callable callable = (Callable) unboundCallNode.getChildren().get(0).accept(this);
        Object[] args = unboundCallNode.getChildren()
            .stream()
            .skip(1)
            .map(arg -> arg.accept(this))
            .toArray();
        return callable.call(args);
    }

    @Override
    public Object visitLambda(LambdaNode lambdaNode) {
        return new Closure(locals, lambdaNode);
    }

    @Override
    public Object visitReturn(ReturnNode returnNode) {
        return returnNode.getChildren().isEmpty() ? Return.EMPTY : new Return(returnNode.getChildren().get(0).accept(this));
    }

    @Override
    public Object visitProject(ProjectNode projectNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitIf(IfNode ifNode) {
        Boolean test = (Boolean) ifNode.getChildren().get(0).accept(this);
        if (test) {
            Object result = null;
            for (Node node : ifNode.getChildren()) {
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
    public Object visitAnd(AndNode andNode) {
        for (Node node: andNode.getChildren()) {
            if (!(Boolean) node.accept(this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object visitOr(OrNode orNode) {
        for (Node node: orNode.getChildren()) {
            if ((Boolean) node.accept(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object visitAssignment(AssignmentNode assignmentNode) {
        return locals = locals.plus((NamedNode) assignmentNode.getChildren().get(0), assignmentNode.getChildren().get(1).accept(this));
    }

    @Override
    public Object visitElse(ElseNode elseNode) {
        if (doElse) {
            doElse = false;
            Object result = null;
            for (Node node : elseNode.getChildren()) {
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
        for (Object var: (Iterable) forNode.getChildren().get(0).accept(this)) {
            locals = locals.plus(forNode, var);
            for (int i = 1; i < forNode.getChildren().size(); i++) {
                Object value = forNode.getChildren().get(i).accept(this);
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
}
