package foo.interpreter;

import foo.model.*;
import org.pcollections.HashPMap;
import org.pcollections.TreePVector;

class InterpretingVisitor implements NodeVisitor<Object> {
    private HashPMap<NamedNode, Object> locals;

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
            for (Node node : functionNode.getItems()) {
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
        for (Node item : listNode.getItems()) {
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

        if (node.getClass() == FunctionNode.class) {
            return new CallableFunction((FunctionNode) node);
        }

        return locals.getOrDefault(node, node);
    }

    @Override
    public Object visitUnboundCall(UnboundCallNode unboundCallNode) {
        Callable callable = (Callable) unboundCallNode.getFunction().accept(this);
        Object[] args = unboundCallNode.getItems()
            .stream()
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
        return returnNode.getValue() != null ? new Return(returnNode.getValue().accept(this)) : Return.EMPTY;
    }

    @Override
    public Object visitProject(ProjectNode projectNode) {
        throw new UnsupportedOperationException();
    }
}
