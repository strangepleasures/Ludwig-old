package foo.ide;

import foo.model.*;

public class SingleLineFormatter implements NodeVisitor<String> {
    private final StringBuilder out = new StringBuilder();

    @Override
    public String visitBoundCall(BoundCallNode boundCallNode) {
        out.append('[');
        for (int i = 0; i < boundCallNode.children().size(); i++) {
            if (i > 0) {
                out.append(' ');
            }
            boundCallNode.children().get(i).accept(this);
        }
        FunctionNode fn = (FunctionNode) boundCallNode.children().get(0);

        for (ParameterNode p: fn.parameters()) {
            if (boundCallNode.arguments().containsKey(p)) {
                out.append(' ').append(p.getName()).append(": ");
                boundCallNode.arguments().get(p).accept(this);
            }
        }
        out.append(']');
        return null;
    }

    @Override
    public String visitFunction(FunctionNode functionNode) {
        return null;
    }

    @Override
    public String visitLet(LetNode letNode) {
        out.append("= ").append(letNode.getName()).append(' ');
        if (!letNode.children().isEmpty()) {
            letNode.children().get(0).accept(this);
        }
        return null;
    }


    @Override
    public String visitLiteral(LiteralNode literalNode) {
        out.append(literalNode.text());
        return null;
    }

    @Override
    public String visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public String visitParameter(ParameterNode parameterNode) {
        out.append(parameterNode.getName());
        return null;
    }

    @Override
    public String visitRef(RefNode refNode) {
        refNode.ref().accept(this);
        return null;
    }

    @Override
    public String visitUnboundCall(UnboundCallNode unboundCallNode) {
        out.append('[');
        for (int i = 0; i < unboundCallNode.children().size(); i++) {
            if (i > 0) {
                out.append(' ');
            }
            unboundCallNode.children().get(i).accept(this);
        }
        out.append(']');
        return null;
    }

    @Override
    public String visitLambda(LambdaNode lambdaNode) {
        out.append("[lambda [");
        for (int i = 0; i < lambdaNode.parameters().size(); i++) {
            if (i > 0) {
                out.append(' ');
            }
            lambdaNode.parameters().get(i).accept(this);
        }
        out.append(']');
        lambdaNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        out.append(']');
        return null;
    }

    @Override
    public String visitReturn(ReturnNode returnNode) {
        out.append("return");
        returnNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        return null;
    }

    @Override
    public String visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public String visitIf(IfNode ifNode) {
        out.append("[if");
        ifNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        out.append(']');
        return null;
    }

    @Override
    public String visitAnd(AndNode andNode) {
        out.append("[|");
        andNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        out.append(']');
        return null;
    }

    @Override
    public String visitOr(OrNode orNode) {
        out.append("[&");
        orNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        out.append(']');
        return null;
    }

    @Override
    public String visitAssignment(AssignmentNode assignmentNode) {
        out.append(":=");
        assignmentNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        return null;
    }

    @Override
    public String visitElse(ElseNode elseNode) {
        out.append("[else");
        elseNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        out.append(']');
        return null;
    }

    @Override
    public String visitFor(ForNode forNode) {
        out.append("[for ").append(forNode.getName());
        forNode.children().forEach(node -> {
            out.append(' ');
            node.accept(this);
        });
        out.append(']');
        return null;
    }

    @Override
    public String toString() {
        String s = out.toString();
        if (s.startsWith("[")) {
            s = s.substring(1, s.length() - 1);
        }
        return super.toString();
    }
}
