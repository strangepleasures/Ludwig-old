package ludwig.utils;

import ludwig.model.*;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class CodeFormatter implements NodeVisitor<Void> {
    @Getter
    private final List<CodeLine> lines = new ArrayList<>();
    private int indentation;

    @Override
    public Void visitList(ListNode node) {
        print("list");
        boolean inline = level(node) < 4;
        for (Node n: node.children()) {
            child(n, inline);
        }
        return null;
    }

    @Override
    public Void visitFunction(FunctionNode functionNode) {
        functionNode.children().forEach(n -> child(n, false));
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
        print(variableNode.getName());
        return null;
    }

    @Override
    public Void visitReference(ReferenceNode referenceNode) {
        print(referenceNode.ref().getName());
        boolean inline = level(referenceNode) < 4;
        referenceNode.children().forEach(node -> child(node, inline));
        return null;
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
        for (Node n: lambdaNode.children()) {
            if (body) {
                child(n, inline);
            } else {
                print(" " + n);
            }
            if (n instanceof SeparatorNode) {
                body = true;
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
        boolean inline = level(elseNode) < 4;
        elseNode.children().forEach(node -> child(node, inline));
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

    @Override
    public Void visitSeparator(SeparatorNode separatorNode) {
        print(": ");
        return null;
    }

    public void child(Node node, boolean inline) {
        indentation++;
        if (!inline) {
            lines.add(new CodeLine(node));
            for (int i = 1; i < indentation; i++) {
                print("    ");
            }
        } else if (indentation > 1) {
            print(" ");
        }
        node.accept(this);
        indentation--;
    }

    void print(String s) {
        CodeLine line = lines.get(lines.size() - 1);
        line.append(s);
    }

    private static int level(Node node) {
        return node.children().stream().map(CodeFormatter::level).max(Comparator.naturalOrder()).orElse(0) + 1;
    }

    @Override
    public String toString() {
        return lines.stream().map(CodeLine::toString).collect(Collectors.joining("\n"));
    }
}
