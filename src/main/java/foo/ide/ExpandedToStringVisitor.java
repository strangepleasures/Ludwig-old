package foo.ide;

import foo.model.*;

public class ExpandedToStringVisitor implements NodeVisitor<String> {
    public static ExpandedToStringVisitor INSTANCE = new ExpandedToStringVisitor();

    private ExpandedToStringVisitor() {

    }

    @Override
    public String visitBoundCall(BoundCallNode boundCallNode) {
        return boundCallNode.children().get(0).accept(this);
    }

    @Override
    public String visitFunction(FunctionNode functionNode) {
        return null;
    }

    @Override
    public String visitLet(LetNode letNode) {
        return "=";
    }

    @Override
    public String visitLiteral(LiteralNode literalNode) {
        return literalNode.text();
    }

    @Override
    public String visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public String visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public String visitRef(RefNode refNode) {
        return refNode.ref().getName();
    }

    @Override
    public String visitUnboundCall(UnboundCallNode unboundCallNode) {
        return unboundCallNode.children().get(0).accept(this);
    }

    @Override
    public String visitLambda(LambdaNode lambdaNode) {
        return "lambda";
    }

    @Override
    public String visitReturn(ReturnNode returnNode) {
        return "return";
    }

    @Override
    public String visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public String visitIf(IfNode ifNode) {
        return "if " + ifNode.children().get(0).accept(this);
    }

    @Override
    public String visitAssignment(AssignmentNode assignmentNode) {
        return "=";
    }

    @Override
    public String visitElse(ElseNode elseNode) {
        return "else";
    }

    @Override
    public String visitFor(ForNode forNode) {
        return "for " + forNode.getName() + " " + forNode.children().get(0).accept(this);
    }
}
