package ludwig.ide;

import javafx.scene.control.TextArea;

public class EditorUtils {
    public static int tokenIndex(TextArea textArea) {
        String text = textArea.getText();
        int pos = textArea.getSelection().getStart();
        int index = -1;
        boolean isToken = false;
        for (int i = 0; i < Math.min(pos + 1, text.length()); i++) {
            boolean t;
            switch (text.charAt(i)) {
                case '\n':
                case '\t':
                case '\r':
                case ' ':
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
}
