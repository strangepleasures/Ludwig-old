package foo.utils;

import foo.model.Node;
import org.apache.commons.lang.StringEscapeUtils;

public class NodeUtils {
    public static String toString(Node node) {
        CodeFormatter visitor = new CodeFormatter();
        node.accept(visitor);
        return visitor.toString();
    }

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

    public static String toHtml(Node node) {
        return node.accept(new HtmlGenerator()).render();
    }
}
