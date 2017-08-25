package ludwig.ide;

import javafx.scene.control.TextArea;

public class EditorUtils {
    public static int tokenIndex(String text, int pos) {
        if (pos >= text.length()) {
            return -1;
        }
        int index = -1;
        boolean isToken = false;
        for (int i = 0; i < Math.min(pos + 1, text.length()); i++) {
            boolean t;
            switch (text.charAt(i)) {
                case '\n':
                case '\t':
                case '\r':
                case ' ':
                case ':':
                    t = false;
                    break;
                default:
                    t = true;
                    break;
            }
            if (t && t != isToken) {
                index++;
            }
            isToken = t;
        }
        return index;
    }

    public static int tokenIndex(TextArea textArea) {
        return tokenIndex(textArea.getText(), textArea.getSelection().getStart());
    }
}
