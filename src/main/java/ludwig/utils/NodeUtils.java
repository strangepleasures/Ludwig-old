package ludwig.utils;

import ludwig.model.*;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.*;

public class NodeUtils {

    public static Object parseLiteral(String s) {
        switch (s) {
            case "true":
                return true;
            case "false":
                return false;
            case "null":
                return null;
            default:
                if (s.startsWith("'")) {
                    return StringEscapeUtils.unescapeJavaScript(s.substring(1, s.length() - 1));
                }
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e1) {
                    return Double.parseDouble(s);
                }
        }
    }

    public static String formatLiteral(Object o) {
        if (o instanceof String) {
            return '\'' + StringEscapeUtils.escapeJavaScript(o.toString()) + '\'';
        }
        return String.valueOf(o);
    }

    public static List<Node> expandNode(Node node) {
        List<Node> nodes = new ArrayList<>();
        expandNode(node, true, nodes);
        return nodes;
    }

    private static void expandNode(Node<?> node, boolean onlyChildren, List<Node> nodes) {
        if (!onlyChildren) {
            nodes.add(node);
        }
        for (Node child : node.children()) {
            expandNode(child, false, nodes);
        }
    }

    public static String signature(Node<?> node) {
        if (node instanceof OverrideNode) {
            return signature(((ReferenceNode)node.children().get(0)).ref());
        }
        StringBuilder builder = new StringBuilder(node.toString());
        for (Node<?> child: node.children()) {
            if (child instanceof PlaceholderNode) {
                builder.append(' ');
                builder.append(((PlaceholderNode) child).getParameter());
            } else if (child instanceof VariableNode) {
                builder.append(' ');
                builder.append(child.toString());
            } else {
                break;
            }
        }
        return builder.toString();
    }

    public static boolean isReadonly(Node<?> node) {
        return node == null || node.parentOfType(ProjectNode.class).isReadonly();
    }

    private static void collectLocals(Node<?> root, Node<?> stop, String filter, List<Node> locals) {
        if (root == stop) {
            return;
        }
        if (root instanceof VariableNode && ((VariableNode) root).name().startsWith(filter)) {
            locals.add(root);
        }
        root.children().forEach(child -> collectLocals(child, stop, filter, locals));
    }

    public static List<Node> collectLocals(Node<?> root, Node<?> stop, String filter) {
        List<Node> locals = new ArrayList<>();
        collectLocals(root, stop, filter, locals);
        locals.sort(Comparator.comparing(Object::toString));
        return locals;
    }

    public static boolean isField(Node node) {
        return (node instanceof VariableNode) && (node.parent() instanceof ClassNode);
    }
}
