package ludwig.utils;

import ludwig.model.*;

import static ludwig.utils.NodeUtils.arguments;

class ArgumentsCount implements NodeVisitor<Integer> {
    @Override
    public Integer visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public Integer visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public Integer visitFunction(FunctionNode functionNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitVariable(VariableNode variableNode) {
        return 0;
    }

    @Override
    public Integer visitAssignment(AssignmentNode assignmentNode) {
        return 2;
    }

    @Override
    public Integer visitReference(ReferenceNode referenceNode) {
        return arguments(referenceNode.ref()).size();
    }

    @Override
    public Integer visitList(ListNode listNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitLiteral(LiteralNode literalNode) {
        return 0;
    }

    @Override
    public Integer visitLambda(LambdaNode lambdaNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitCall(CallNode callNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitReturn(ReturnNode returnNode) {
        return 1;
    }

    @Override
    public Integer visitIf(IfNode ifNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitElse(ElseNode elseNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitFor(ForNode forNode) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer visitFunctionReference(FunctionReferenceNode functionReference) {
        return 1;
    }

    @Override
    public Integer visitThrow(ThrowNode throwNode) {
        return 1;
    }

    @Override
    public Integer visitPlaceholder(PlaceholderNode placeholderNode) {
        return 0;
    }

    @Override
    public Integer visitBreak(BreakNode breakNode) {
        return 1;
    }

    @Override
    public Integer visitContinue(ContinueNode continueNode) {
        return 1;
    }

    @Override
    public Integer visitOverride(OverrideNode overrideNode) {
        return arguments(overrideNode).size();
    }

    @Override
    public Integer visitClass(ClassNode classNode) {
        return arguments(classNode).size();
    }
}
