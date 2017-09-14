package ludwig.script

import java.io.IOException
import java.io.Reader
import java.util.*

class Lexer private constructor(private val reader: Reader) {
    private val builder = StringBuilder()
    private val tokens = ArrayList<String>()
    private val levelsStack = ArrayDeque<Int>()
    private var balance = 0

    @Throws(IOException::class, LexerException::class)
    private fun read(): List<String> {

        var start = true
        var level = 0

        while (true) {
            val c = reader.read()
            when (c) {
                ' '.toInt(), '\t'.toInt() -> if (start) {
                    level++
                } else {
                    applyToken()
                }
                -1, '\n'.toInt() -> {
                    if (!start) {
                        applyToken()
                        start = true
                        level = 0
                    }
                    if (c == -1) {
                        while (balance > 0) {
                            tokens.add(")")
                            balance--
                        }
                        return tokens
                    }
                }
                else -> {
                    if (start) {
                        if (levelsStack.isEmpty()) {
                            if (level > 0) {
                                throw LexerException("Invalid indentation")
                            }
                            levelsStack.addLast(0)
                        } else {
                            if (levelsStack.peekLast() < level) {
                                levelsStack.addLast(level)
                            } else {
                                while (!levelsStack.isEmpty() && levelsStack.peekLast() > level) {
                                    levelsStack.removeLast()

                                }
                            }
                            for (i in balance downTo levelsStack.peekLast() + 1) {
                                tokens.add(")")
                                balance--
                            }
                        }
                        tokens.add("(")
                        balance++
                        start = false
                    }

                    if (c == '\''.toInt()) {
                        readEscapedString()
                        applyToken()
                    } else {
                        builder.append(c.toChar())
                    }
                }
            }
        }
    }

    private fun applyToken() {
        if (builder.length > 0) {
            val token = builder.toString()
            if (token == "call") {
                tokens.add("(")
                balance++
            }
            if (token == ":") {
                tokens.add(")")
                tokens.add("(")
            } else {
                tokens.add(token)
            }
        }
        builder.setLength(0)
    }


    @Throws(IOException::class, LexerException::class)
    private fun readEscapedString() {
        builder.append('\'')
        var escaped = false
        while (true) {
            val c = reader.read()
            if (c == -1) {
                throw LexerException("Unterminated string")
            }

            builder.append(c.toChar())

            if (escaped) {
                escaped = false
            } else {
                if (c == '\''.toInt()) {
                    return
                }
                escaped = c == '\\'.toInt()
            }
        }
    }

    companion object {


        @Throws(IOException::class, LexerException::class)
        fun read(reader: Reader): List<String> {
            return Lexer(reader).read()
        }

        fun isLiteral(token: String): Boolean {
            if (token.startsWith("\'")) {
                return true
            }
            try {
                java.lang.Double.parseDouble(token)
                return true
            } catch (ignore: NumberFormatException) {
                return false
            }

        }
    }
}
