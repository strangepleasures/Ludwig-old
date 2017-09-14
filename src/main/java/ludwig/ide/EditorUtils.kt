package ludwig.ide

import javafx.scene.control.TextArea

object EditorUtils {
    fun tokenIndex(text: String, pos: Int): Int {
        if (pos >= text.length) {
            return -1
        }
        var index = -1
        var isToken = false
        for (i in 0 until Math.min(pos + 1, text.length)) {
            val t: Boolean
            when (text[i]) {
                '\n', '\t', '\r', ' ', ':' -> t = false
                else -> t = true
            }
            if (t && t != isToken) {
                index++
            }
            isToken = t
        }
        return index
    }

    fun tokenIndex(textArea: TextArea): Int {
        return tokenIndex(textArea.text, textArea.selection.start)
    }
}
