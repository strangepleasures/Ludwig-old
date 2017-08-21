package ludwig.utils;

import ludwig.model.*;

import static ludwig.utils.NodeUtils.arguments;
import static ludwig.utils.NodeUtils.declaration;

class HasParameters implements NodeVisitor<Boolean> {
    @Override
    public Boolean visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public Boolean visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public Boolean visitFunction(FunctionNode functionNode) {
        return true;
    }

    @Override
    public Boolean visitVariable(VariableNode variableNode) {
        return false;
    }

    @Override
    public Boolean visitAssignment(AssignmentNode assignmentNode) {
        return true;
    }

    @Override
    public Boolean visitReference(ReferenceNode referenceNode) {
        return !arguments(referenceNode.ref()).isEmpty();
    }

    @Override
    public Boolean visitList(ListNode listNode) {
        return true;
    }

    @Override
    public Boolean visitLiteral(LiteralNode literalNode) {
        return false;
    }

    @Override
    public Boolean visitLambda(LambdaNode lambdaNode) {
        return true;
    }

    @Override
    public Boolean visitCall(CallNode callNode) {
        return true;
    }

    @Override
    public Boolean visitReturn(ReturnNode returnNode) {
        return true;
    }

    @Override
    public Boolean visitIf(IfNode ifNode) {
        return true;
    }

    @Override
    public Boolean visitElse(ElseNode elseNode) {
        return true;
    }

    @Override
    public Boolean visitFor(ForNode forNode) {
        return true;
    }

    @Override
    public Boolean visitFunctionReference(FunctionReferenceNode functionReference) {
        return true;
    }

    @Override
    public Boolean visitThrow(ThrowNode throwNode) {
        return true;
    }

    @Override
    public Boolean visitPlaceholder(PlaceholderNode placeholderNode) {
        return false;
    }

    @Override
    public Boolean visitBreak(BreakNode breakNode) {
        return true;
    }

    @Override
    public Boolean visitContinue(ContinueNode continueNode) {
        return true;
    }

    @Override
    public Boolean visitOverride(OverrideNode overrideNode) {
        return true;
    }

    @Override
    public Boolean visitClass(ClassNode classNode) {
        return null;
    }
}
