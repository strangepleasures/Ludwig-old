package ludwig.utils;

import ludwig.model.*;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            if (child instanceof SeparatorNode) {
                break;
            }
            builder.append(' ');
            if (child instanceof PlaceholderNode) {
                builder.append(((PlaceholderNode) child).getParameter());
            } else {
                builder.append(child.toString());
            }
        }
        return builder.toString();
    }
}
