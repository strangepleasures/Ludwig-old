package foo.ide;

import foo.model.*;
import lombok.Getter;

import java.util.*;

public class CodeFormatter implements NodeVisitor<Void> {
    @Getter
    private final List<CodeLine> lines = new ArrayList<>();
    private int indentation;


    @Override
    public Void visitBoundCall(BoundCallNode boundCallNode) {
        FunctionNode fn = (FunctionNode) boundCallNode.children().get(0);
        print(fn.getName());
        boolean inline = level(fn) < 4;
        fn.parameters().forEach(param -> {
            if (boundCallNode.arguments().containsKey(param)) {
                child(boundCallNode.arguments().get(param), inline);
            }
        });

        return null;
    }

    @Override
    public Void visitFunction(FunctionNode functionNode) {
        functionNode.children().forEach(n -> child(n, false));
        return null;
    }

    @Override
    public Void visitLet(LetNode letNode) {
        print("= ");
        print(letNode.getName());
        child(letNode.children().get(0), true);
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
    public Void visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public Void visitRef(RefNode refNode) {
        print(refNode.ref().getName());
        return null;
    }

    @Override
    public Void visitUnboundCall(UnboundCallNode unboundCallNode) {
        print("!");
        child(unboundCallNode.children().get(0), true);
        boolean inline = level(unboundCallNode) < 4;
        for (int i = 1; i < unboundCallNode.children().size(); i++) {
            child(unboundCallNode.children().get(i), inline);
        }

        return null;
    }

    @Override
    public Void visitLambda(LambdaNode lambdaNode) {
        print("λ");
        lambdaNode.parameters().forEach(param -> print(" " + param.getName()));
        print(" :");
        boolean inline = level(lambdaNode) < 4;
        lambdaNode.children().forEach(node -> child(node, inline));
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
        boolean inline = level(ifNode) < 4;
        for (int i = 1; i < ifNode.children().size(); i++) {
            child(ifNode.children().get(i), inline);
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
        print("for ");
        print(forNode.getName());
        child(forNode.children().get(0), true);
        boolean inline = level(forNode) < 4;
        for (int i = 1; i < forNode.children().size(); i++) {
            child(forNode.children().get(i), inline);
        }
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

}
