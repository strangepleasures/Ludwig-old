package foo.ide;

import foo.model.*;

import java.util.ArrayList;
import java.util.List;

public class CodeFormatter implements NodeVisitor<Void> {
    private final List<CodeLine> lines = new ArrayList<>();
    private boolean indentation;
    private boolean inline;

    @Override
    public Void visitBoundCall(BoundCallNode boundCallNode) {
        return null;
    }

    @Override
    public Void visitFunction(FunctionNode functionNode) {
        return null;
    }

    @Override
    public Void visitLet(LetNode letNode) {
        return null;
    }

    @Override
    public Void visitLiteral(LiteralNode literalNode) {
        return null;
    }

    @Override
    public Void visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public Void visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public Void visitRef(RefNode refNode) {
        return null;
    }

    @Override
    public Void visitUnboundCall(UnboundCallNode unboundCallNode) {
        return null;
    }

    @Override
    public Void visitLambda(LambdaNode lambdaNode) {
        return null;
    }

    @Override
    public Void visitReturn(ReturnNode returnNode) {
        return null;
    }

    @Override
    public Void visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public Void visitIf(IfNode ifNode) {
        return null;
    }

    @Override
    public Void visitAnd(AndNode andNode) {
        return null;
    }

    @Override
    public Void visitOr(OrNode orNode) {
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentNode assignmentNode) {
        return null;
    }

    @Override
    public Void visitElse(ElseNode elseNode) {
        return null;
    }

    @Override
    public Void visitFor(ForNode forNode) {
        return null;
    }
}
