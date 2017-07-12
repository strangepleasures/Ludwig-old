package foo.utils;

import org.apache.commons.lang.StringEscapeUtils;

public class LiteralParser {
    public static Object parse(String s) {
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

    public static String toString(Object o) {
        if (o instanceof String) {
            return StringEscapeUtils.escapeJavaScript(o.toString());
        }
        return String.valueOf(o);
    }
}
