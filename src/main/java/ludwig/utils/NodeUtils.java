package ludwig.utils;

import ludwig.model.Node;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

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
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e1) {
                    try {
                        return Double.parseDouble(s);
                    } catch (NumberFormatException e2) {
                        return StringEscapeUtils.unescapeJavaScript(s);
                    }
                }
        }
    }

    public static String formatLiteral(Object o) {
        if (o instanceof String) {
            return StringEscapeUtils.escapeJavaScript(o.toString());
        }
        return String.valueOf(o);
    }

    public static List<Node> expandNode(Node node) {
        List<Node> nodes = new ArrayList<>();
        expandNode(node, true, nodes);
        return nodes;
    }

    private static void expandNode(Node node, boolean onlyChildren, List<Node> nodes) {
        if (!onlyChildren) {
            nodes.add(node);
        }
        for (Node child: node.children()) {
            expandNode(child, false, nodes);
        }
    }
}
