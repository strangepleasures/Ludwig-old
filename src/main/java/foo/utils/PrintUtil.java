package foo.utils;

import foo.model.*;

public class PrintUtil {
    public static String toString(Node node) {
        PrintVisitor visitor = new PrintVisitor();
        node.accept(visitor);
        return visitor.toString();
    }

    private static class PrintVisitor implements NodeVisitor<Void> {
        private final StringBuilder out = new StringBuilder();
        private int indentation;
        private boolean inline;

        @Override
        public Void visitBoundCall(BoundCallNode boundCallNode) {
            indent();
            FunctionNode functionNode = (FunctionNode) boundCallNode.getChildren().get(0);
            out.append(functionNode.getName()).append("\n");
            indentation++;
            for (ParameterNode param : functionNode.getParameters()) {
                indent();
                out.append(param.getName()).append(": ");
                if (boundCallNode.getArguments().containsKey(param)) {
                    inline = true;
                    indentation++;
                    boundCallNode.getArguments().get(param).accept(this);
                    indentation--;
                } else {
                    out.append('\n');
                }
            }
            indentation--;
            return null;
        }

        @Override
        public Void visitFunction(FunctionNode functionNode) {
            indent();
            out.append("def ").append(functionNode.getName()).append(" [");
            boolean first = true;
            for (ParameterNode param : functionNode.getParameters()) {
                if (!first) {
                    out.append(' ');
                }
                first = false;
                out.append(param.getName());
            }
            out.append("]");
            printChildren(functionNode, false);
            return null;
        }

        @Override
        public Void visitLet(LetNode letNode) {
            indent();
            out.append("= ").append(letNode.getName());
            printChildren(letNode, true);
            return null;
        }

        @Override
        public Void visitList(ListNode listNode) {
            indent();
            out.append("list");
            printChildren(listNode, false);
            return null;
        }

        @Override
        public Void visitLiteral(LiteralNode literalNode) {
            indent();
            out.append(literalNode.getText()).append('\n');
            return null;
        }

        @Override
        public Void visitPackage(PackageNode packageNode) {
            indent();
            out.append("package ").append(packageNode.getName());
            printChildren(packageNode, false);
            return null;
        }

        @Override
        public Void visitParameter(ParameterNode parameterNode) {
            return null;
        }

        @Override
        public Void visitRef(RefNode refNode) {
            indent();
            out.append(((NamedNode)refNode.getChildren().get(0)).getName()).append('\n');
            return null;
        }

        @Override
        public Void visitUnboundCall(UnboundCallNode unboundCallNode) {
            indent();
            if (unboundCallNode.getChildren().get(0) instanceof RefNode) {
                out.append(((NamedNode) unboundCallNode.getChildren().get(0).getChildren().get(0)).getName());
                if (unboundCallNode.getChildren().size() == 1) {
                    out.append(" []\n");
                } else {
                    out.append('\n');
                    indentation++;
                    unboundCallNode.getChildren().stream().skip(1).forEach(this::print);
                    indentation--;
                }
            } else {
                // TODO: Support ?
            }
            return null;
        }

        @Override
        public Void visitLambda(LambdaNode lambdaNode) {
            indent();
            out.append("lambda [");
            boolean first = true;
            for (ParameterNode param : lambdaNode.getParameters()) {
                if (!first) {
                    out.append(' ');
                }
                first = false;
                out.append(param.getName());
            }
            out.append(']');
            printChildren(lambdaNode, false);
            return null;
        }

        @Override
        public Void visitReturn(ReturnNode returnNode) {
            indent();
            out.append("return");
            printChildren(returnNode, true);
            return null;
        }

        @Override
        public Void visitProject(ProjectNode projectNode) {
            indent();
            out.append("project ").append(projectNode.getName());
            printChildren(projectNode, false);
            return null;
        }

        @Override
        public Void visitIf(IfNode ifNode) {
            indent();
            out.append("if");
            printChildren(ifNode, true);
            return null;
        }

        @Override
        public Void visitAnd(AndNode andNode) {
            out.append("&");
            printChildren(andNode, false);
            return null;
        }

        @Override
        public Void visitOr(OrNode orNode) {
            indent();
            out.append("|");
            printChildren(orNode, false);
            return null;
        }

        @Override
        public Void visitAssignment(AssignmentNode assignmentNode) {
            indent();
            out.append(":= ").append(((NamedNode)assignmentNode.getChildren().get(0)).getName());
            printChildren(assignmentNode, true);
            return null;
        }

        @Override
        public Void visitElse(ElseNode elseNode) {
            indent();
            out.append("else");
            printChildren(elseNode, false);
            return null;
        }

        @Override
        public Void visitFor(ForNode forNode) {
            indent();
            out.append("for ").append(forNode.getName());
            printChildren(forNode, true);
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
                if (node.getChildren().isEmpty()) {
                    out.append('\n');
                } else {
                    print(node.getChildren().get(0));
                }
                indentation--;
                node.getChildren().stream().skip(1).forEach(this::print);
                indentation--;
            } else {
                out.append('\n');
                indentation++;
                node.getChildren().forEach(this::print);
                indentation--;
            }
        }

        private void indent() {
            if (!inline) {
                for (int i = 0; i < indentation; i++) {
                    out.append('\t');
                }
            }
            inline = false;
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }
}
