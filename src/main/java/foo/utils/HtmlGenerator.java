package foo.utils;

import foo.model.*;
import j2html.tags.ContainerTag;

import java.util.Comparator;

import static j2html.TagCreator.*;
import static org.apache.commons.lang.StringUtils.defaultString;

class HtmlGenerator implements NodeVisitor<ContainerTag> {
    private int indentation;
    private boolean inline;

    @Override
    public ContainerTag visitBoundCall(BoundCallNode boundCallNode) {
        return null;
    }

    @Override
    public ContainerTag visitFunction(FunctionNode functionNode) {
        return
            html(
                head(
                    title("Function " + functionNode.name()),
                    link().withRel("stylesheet").withHref("/css/function.css")
                ),
                body(
                    table(
                        tbody(
                            tr(
                                td("Name"),
                                td("Description")
                            ),
                            tr(
                                td(functionNode.name()),
                                td(defaultString(functionNode.getComment()))
                            ),
                            each(functionNode.parameters(), param -> tr(
                                td(param.name()),
                                td(defaultString(param.getComment()))
                            ))
                        )
                    ).withClass("signature-table"),


                    each(functionNode.children(), child -> child.accept(this))
                )
            );
    }

    @Override
    public ContainerTag visitLet(LetNode letNode) {
        return null;
    }

    @Override
    public ContainerTag visitList(ListNode listNode) {
        return null;
    }

    @Override
    public ContainerTag visitLiteral(LiteralNode literalNode) {
        return withNode(literalNode, span(literalNode.text()));
    }

    @Override
    public ContainerTag visitPackage(PackageNode packageNode) {
        return null;
    }

    @Override
    public ContainerTag visitParameter(ParameterNode parameterNode) {
        return null;
    }

    @Override
    public ContainerTag visitRef(RefNode refNode) {
        return withNode(refNode, a(refNode.ref().name())
            .withHref(refNode.ref().id()));
    }

    @Override
    public ContainerTag visitUnboundCall(UnboundCallNode node) {
        ContainerTag tag = withNode(node,
            node.children().get(0).accept(this)
        );

        boolean expand = level(node) > 3;
        if (!expand && indentation > 0) {
            tag = span("[").with(tag);
        }
        tag.with(expand ? br() : span(" "));
        indentation++;
        for (int i = 1; i < node.children().size(); i++) {
            if (i > 1) {
                tag.with(expand ? br() : span(" "));
            }
            tag.with(node.children().get(i).accept(this));
        }
        indentation--;
        if (!expand && indentation > 0) {
            tag.with(span("]"));
        }
        return tag;
    }

    @Override
    public ContainerTag visitLambda(LambdaNode lambdaNode) {
        return null;
    }

    @Override
    public ContainerTag visitReturn(ReturnNode returnNode) {
        return null;
    }

    @Override
    public ContainerTag visitProject(ProjectNode projectNode) {
        return null;
    }

    @Override
    public ContainerTag visitIf(IfNode ifNode) {
        return null;
    }

    @Override
    public ContainerTag visitAnd(AndNode andNode) {
        return null;
    }

    @Override
    public ContainerTag visitOr(OrNode orNode) {
        return null;
    }

    @Override
    public ContainerTag visitAssignment(AssignmentNode assignmentNode) {
        return null;
    }

    @Override
    public ContainerTag visitElse(ElseNode elseNode) {
        return null;
    }

    @Override
    public ContainerTag visitFor(ForNode forNode) {
        return null;
    }

    private ContainerTag withNode(Node node, ContainerTag tag) {
        String prefix = "";

        if (!inline && level(node) > 2) {
            for (int i = 0; i < indentation; i++) {
                prefix = prefix + "    ";
            }
        }
        inline = false;

        tag.attr("nodeId", node.id()).attr("nodeType", node.getClass().getSimpleName());
        if (prefix.length() > 0) {
            tag = span(prefix).with(tag);
        }

        return tag;
    }

    private static int level(Node node) {
        return node.children().stream().map(HtmlGenerator::level).max(Comparator.naturalOrder()).orElse(0) + 1;
    }

    private ContainerTag printChildren(ContainerTag tag, Node node, boolean inlineFirst) {
        if (inlineFirst) {
            tag.with(span(" "));
            inline = true;
            indentation += 2;
            if (node.children().isEmpty()) {
                tag.with(br());
            } else {
                node.children().get(0).accept(this);
            }
            indentation--;
            node.children().stream().skip(1).forEach(n -> n.accept(this));
            indentation--;
        } else {
            boolean expand = level(node) > 3;
            tag.with(expand ? br() : span(" ["));
            indentation++;
            for (int i = 0; i < node.children().size(); i++) {
                if (i > 0) {
                    tag.with(expand ? br() : span(" "));
                }
                tag.with(node.children().get(i).accept(this));
            }
            indentation--;
            if (!expand) {
                tag.with(span("]"));
            }
        }
        return tag;
    }

}
