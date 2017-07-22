package foo.ide;

import foo.model.*;

import java.util.List;

public class GetChildrenVisitor implements NodeVisitor<List<Node>> {
    @Override
    public List<Node> visitBoundCall(BoundCallNode boundCallNode) {
        return null;
    }

    @Override
    public List<Node> visitFunction(FunctionNode functionNode) {
        return null;
    }

    @Override
    public List<Node> visitLet(LetNode letNode) {
        return letNode.children();
    }

    @Override
    public List<Node> visitLiteral(LiteralNode literalNode) {
        return literalNode.children();
    }

    @Override
    public List<Node> visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public List<Node> visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public List<Node> visitRef(RefNode refNode) {
        return refNode.children();
    }

    @Override
    public List<Node> visitUnboundCall(UnboundCallNode unboundCallNode) {
        return null;
    }

    @Override
    public List<Node> visitLambda(LambdaNode lambdaNode) {
        return null;
    }

    @Override
    public List<Node> visitReturn(ReturnNode returnNode) {
        return returnNode.children().isEmpty() ? returnNode.children() : returnNode.children().get(0).children();
    }

    @Override
    public List<Node> visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public List<Node> visitIf(IfNode ifNode) {
        return ifNode.children();
    }

    @Override
    public List<Node> visitAnd(AndNode andNode) {
        return andNode.children();
    }

    @Override
    public List<Node> visitOr(OrNode orNode) {
        return orNode.children();
    }

    @Override
    public List<Node> visitAssignment(AssignmentNode assignmentNode) {
        return assignmentNode.children().get(0).children();
    }

    @Override
    public List<Node> visitElse(ElseNode elseNode) {
        return null;
    }

    @Override
    public List<Node> visitFor(ForNode forNode) {
        return null;
    }
}
