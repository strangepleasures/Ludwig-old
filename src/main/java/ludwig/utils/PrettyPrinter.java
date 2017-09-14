package ludwig.utils;

import ludwig.model.*;

import java.util.Comparator;

public class PrettyPrinter implements NodeVisitor<Void> {
    private final StringBuilder builder = new StringBuilder();

    private PrettyPrinter() {
    }

    public static String print(Node parent) {
        PrettyPrinter printer = new PrettyPrinter();
        parent.accept(printer);
        return printer.toString();
    }

    @Override
    public String toString() {
        if (builder.length() == 0 || builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }
        return builder.toString();
    }

    private int indentation;

    @Override
    public Void visitList(ListNode node) {
        print("list");
        boolean inline = level(node) < 4;
        for (Node n : node.children()) {
            child(n, inline);
        }
        return null;
    }

    @Override
    public Void visitFunction(FunctionNode functionNode) {
        boolean body = false;
        for (Node child : functionNode.children()) {
            if (!(child instanceof VariableNode)) {
                body = true;
            }
            if (body) {
                child(child, false);
            }
        }
        return null;
    }


    @Override
    public Void visitClass(ClassNode classNode) {
        return null;
    }

    @Override
    public Void visitCatch(CatchNode catchNode) {
        print("catch");
        catchNode.children().forEach(node -> child(node, false));
        return null;
    }


    @Override
    public Void visitLiteral(LiteralNode literalNode) {
        print(literalNode.text());
        return null;
    }

    @Override
    public Void visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public Void visitVariable(VariableNode variableNode) {
        print(variableNode.name());
        return null;
    }

    @Override
    public Void visitReference(ReferenceNode referenceNode) {
        print(referenceNode.toString());
        boolean inline = canBeInlined(referenceNode);
        referenceNode.children().forEach(node -> child(node, inline));
        return null;
    }

    private static boolean canBeInlined(Node<?> node) {
        if (level(node) > 3) {
            return false;
        }

        for (int i = 0; i < node.children().size() - 1; i++) {
            if (NodeUtils.INSTANCE.argumentsCount(node.children().get(i)) > 3) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Void visitFunctionReference(FunctionReferenceNode functionReference) {
        print("ref ");
        if (!functionReference.children().isEmpty()) {
            print(functionReference.children().get(0).toString());
        }
        return null;
    }

    @Override
    public Void visitThrow(ThrowNode throwNode) {
        print("throw ");
        throwNode.children().get(0).accept(this);
        return null;
    }

    @Override
    public Void visitTry(TryNode tryNode) {
        print("try");
        tryNode.children().forEach(node -> child(node, false));
        return null;
    }

    @Override
    public Void visitPlaceholder(PlaceholderNode placeholderNode) {
        print(placeholderNode.toString());
        return null;
    }

    @Override
    public Void visitBreak(BreakNode breakNode) {
        print("break ");
        breakNode.children().get(0).accept(this);
        return null;
    }

    @Override
    public Void visitContinue(ContinueNode continueNode) {
        print("continue ");
        continueNode.children().get(0).accept(this);
        return null;
    }

    @Override
    public Void visitOverride(OverrideNode overrideNode) {
        for (int i = 1; i < overrideNode.children().size(); i++) {
            child(overrideNode.children().get(i), false);
        }
        return null;
    }

    @Override
    public Void visitCall(CallNode callNode) {
        print("call");
        child(callNode.children().get(0), true);
        boolean inline = level(callNode) < 4;
        for (int i = 1; i < callNode.children().size(); i++) {
            child(callNode.children().get(i), inline);
        }

        return null;
    }

    @Override
    public Void visitLambda(LambdaNode lambdaNode) {
        print("λ");

        boolean body = false;
        boolean inline = level(lambdaNode) < 4;
        for (Node n : lambdaNode.children()) {
            if (!body && !(n instanceof VariableNode)) {
                body = true;
                print(" : ");
            }
            if (body) {
                child(n, inline);
            } else {
                print(" " + n);
            }
        }
        return null;
    }

    @Override
    public Void visitReturn(ReturnNode returnNode) {
        print("return");
        returnNode.children().forEach(node -> child(node, true));
        return null;
    }

    @Override
    public Void visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public Void visitIf(IfNode ifNode) {
        print("if");
        child(ifNode.children().get(0), true);
        for (int i = 1; i < ifNode.children().size(); i++) {
            child(ifNode.children().get(i), false);
        }
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentNode assignmentNode) {
        print("= ");
        child(assignmentNode.children().get(0), true);
        boolean inline = level(assignmentNode) < 4;
        for (int i = 1; i < assignmentNode.children().size(); i++) {
            child(assignmentNode.children().get(i), inline);
        }
        return null;
    }

    @Override
    public Void visitElse(ElseNode elseNode) {
        print("else");
        elseNode.children().forEach(node -> child(node, false));
        return null;
    }

    @Override
    public Void visitFor(ForNode forNode) {
        print("for");
        for (int i = 0; i < forNode.children().size(); i++) {
            child(forNode.children().get(i), i < 2);
        }
        return null;
    }

    private void child(Node node, boolean inline) {
        indentation++;
        if (!inline) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            for (int i = 1; i < indentation; i++) {
                print("    ");
            }
        } else {
            print(" ");
        }
        node.accept(this);
        indentation--;
    }

    void print(String s) {
        builder.append(s);
    }

    private static int level(Node<?> node) {
        return node.children().stream().map(PrettyPrinter::level).max(Comparator.naturalOrder()).orElse(0) + 1;
    }

}
