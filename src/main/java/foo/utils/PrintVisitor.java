package foo.utils;

import foo.model.*;

import java.util.Comparator;

class PrintVisitor implements NodeVisitor<Void> {
    private final StringBuilder out = new StringBuilder();
    private int indentation;
    private boolean inline;

    @Override
    public Void visitBoundCall(BoundCallNode node) {
        indent(node);
        FunctionNode functionNode = (FunctionNode) node.children().get(0);
        out.append(functionNode.getName());
        indentation++;
        for (ParameterNode param : functionNode.parameters()) {
            indent(param);
            out.append(param.getName()).append(": ");
            if (node.arguments().containsKey(param)) {
                inline = true;
                indentation++;
                node.arguments().get(param).accept(this);
                indentation--;
            } else {
                out.append('\n');
            }
        }
        indentation--;
        return null;
    }

    @Override
    public Void visitFunction(FunctionNode node) {
        indent(node);
        out.append("def ").append(node.getName()).append(" [");
        boolean first = true;
        for (ParameterNode param : node.parameters()) {
            if (!first) {
                out.append(' ');
            }
            first = false;
            out.append(param.getName());
        }
        out.append("]");
        printChildren(node, false);
        return null;
    }

    @Override
    public Void visitLet(LetNode node) {
        indent(node);
        out.append("= ").append(node.getName());
        printChildren(node, true);
        return null;
    }

    @Override
    public Void visitLiteral(LiteralNode node) {
        indent(node);
        out.append(node.text());
        return null;
    }

    @Override
    public Void visitPackage(PackageNode node) {
        indent(node);
        out.append("package ").append(node.getName());
        printChildren(node, false);
        return null;
    }

    @Override
    public Void visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public Void visitRef(RefNode node) {
        indent(node);
        out.append(node.ref().getName());
        return null;
    }

    @Override
    public Void visitUnboundCall(UnboundCallNode node) {
        indent(node);
        if (node.children().get(0) instanceof RefNode) {
            out.append(((RefNode) node.children().get(0)).ref().getName());
            boolean expand = level(node) > 3;
                out.append(expand ? "\n" : " [");
                indentation++;
                for (int i = 1; i < node.children().size(); i++) {
                    if (i > 1) {
                        out.append(expand ? '\n' : ' ');
                    }
                    print(node.children().get(i));
                }
                indentation--;
                if (!expand) {
                    out.append("]");
                }

        } else {
            // TODO: Support ?
        }
        return null;
    }

    @Override
    public Void visitLambda(LambdaNode node) {
        indent(node);
        out.append("lambda [");
        boolean first = true;
        for (ParameterNode param : node.parameters()) {
            if (!first) {
                out.append(' ');
            }
            first = false;
            out.append(param.getName());
        }
        out.append(']');
        printChildren(node, false);
        return null;
    }

    @Override
    public Void visitReturn(ReturnNode node) {
        indent(node);
        out.append("return");
        printChildren(node, true);
        return null;
    }

    @Override
    public Void visitProject(ProjectNode node) {
        indent(node);
        out.append("project ").append(node.getName());
        printChildren(node, false);
        return null;
    }

    @Override
    public Void visitIf(IfNode node) {
        indent(node);
        out.append("if");
        printChildren(node, true);
        return null;
    }

    @Override
    public Void visitAnd(AndNode andNode) {
        out.append("&");
        printChildren(andNode, false);
        return null;
    }

    @Override
    public Void visitOr(OrNode node) {
        indent(node);
        out.append("|");
        printChildren(node, false);
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentNode node) {
        indent(node);
        out.append(":= ").append(((NamedNode)node.children().get(0)).getName());
        printChildren(node, true);
        return null;
    }

    @Override
    public Void visitElse(ElseNode node) {
        indent(node);
        out.append("else");
        printChildren(node, false);
        return null;
    }

    @Override
    public Void visitFor(ForNode node) {
        indent(node);
        out.append("for ").append(node.getName());
        printChildren(node, true);
        return null;
    }

    private void print(Node node) {
        node.accept(this);
    }

    private void printChildren(Node node, boolean inlineFirst) {
        if (inlineFirst) {
            out.append(' ');
            inline = true;
            indentation += 2;
            if (node.children().isEmpty()) {
                out.append('\n');
            } else {
                print(node.children().get(0));
            }
            indentation--;
            node.children().stream().skip(1).forEach(this::print);
            indentation--;
        } else {
            boolean expand = level(node) > 3;
            out.append(expand ? "\n" : " [");
            indentation++;
            for (int i = 0; i < node.children().size(); i++) {
                if (i > 0) {
                    out.append(expand ? '\n' : ' ');
                }
                print(node.children().get(i));
            }
            indentation--;
            if (!expand) {
                out.append(']');
            }
        }
    }

    private void indent(Node node) {
        if (!inline && level(node) > 2) {
            for (int i = 0; i < indentation; i++) {
                out.append('\t');
            }
        }
        inline = false;
    }

    private static int level(Node node) {
        return node.children().stream().map(PrintVisitor::level).max(Comparator.naturalOrder()).orElse(0) + 1;
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
