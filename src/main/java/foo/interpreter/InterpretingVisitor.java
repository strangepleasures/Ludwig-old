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
            FunctionNode functionNode = boundCallNode.getFunction();

            if (functionNode instanceof NativeFunctionNode) {
                Object[] args = boundCallNode.getFunction()
                    .getParameters()
                    .stream()
                    .map(param -> boundCallNode.getArguments().get(param).accept(this))
                    .toArray();
                return ((NativeFunctionNode) functionNode).call(args);
            }

            for (ParameterNode param : boundCallNode.getFunction().getParameters()) {
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

            if (result instanceof Return) {
                return ((Return) result).getValue();
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
        return locals = locals.plus(assignmentNode.getLhs(), assignmentNode.getRhs().accept(this));
    }
}
