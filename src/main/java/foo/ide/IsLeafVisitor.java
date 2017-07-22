package foo.ide;

import foo.model.*;

class IsLeafVisitor implements NodeVisitor<Boolean> {
    static IsLeafVisitor INSTANCE = new IsLeafVisitor();

    private IsLeafVisitor() {}

    @Override
    public Boolean visitBoundCall(BoundCallNode boundCallNode) {
        return false;
    }

    @Override
    public Boolean visitFunction(FunctionNode functionNode) {
        return false;
    }

    @Override
    public Boolean visitLet(LetNode letNode) {
        return letNode.children().isEmpty() || letNode.children().get(0).accept(this);
    }

    @Override
    public Boolean visitLiteral(LiteralNode literalNode) {
        return true;
    }

    @Override
    public Boolean visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public Boolean visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public Boolean visitRef(RefNode refNode) {
        return true;
    }

    @Override
    public Boolean visitUnboundCall(UnboundCallNode unboundCallNode) {
        return false;
    }

    @Override
    public Boolean visitLambda(LambdaNode lambdaNode) {
        return false;
    }

    @Override
    public Boolean visitReturn(ReturnNode returnNode) {
        return returnNode.children().isEmpty() || returnNode.children().get(0).accept(this);
    }

    @Override
    public Boolean visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public Boolean visitIf(IfNode ifNode) {
        return false;
    }

    @Override
    public Boolean visitAnd(AndNode andNode) {
        return false;
    }

    @Override
    public Boolean visitOr(OrNode orNode) {
        return false;
    }

    @Override
    public Boolean visitAssignment(AssignmentNode assignmentNode) {
        return assignmentNode.children().isEmpty() || assignmentNode.children().get(0).accept(this);
    }

    @Override
    public Boolean visitElse(ElseNode elseNode) {
        return false;
    }

    @Override
    public Boolean visitFor(ForNode forNode) {
        return false;
    }
}
