package foo.utils;

import foo.model.*;

public class PrintUtil {
    public static String toString(Node node) {
        PrintVisitor visitor = new PrintVisitor();
        node.accept(visitor);
        return visitor.toString();
    }

}
