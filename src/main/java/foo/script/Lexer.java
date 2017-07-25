package foo.script;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Lexer {
    public static List<String> read(Reader reader) throws IOException, LexerException {
        StringBuilder builder = new StringBuilder();
        List<String> tokens = new ArrayList<>();
        Deque<Integer> levelsStack = new ArrayDeque<>();
        boolean start = true;
        int level = 0;
        int balance = 0;

        while (true) {
            int c = reader.read();
            switch (c) {
                case ' ':
                    if (start) {
                        level++;
                    } else {
                        applyToken(builder, tokens);
                    }
                    break;
                case -1:
                case '\n':
                    if (!start) {
                        applyToken(builder, tokens);
                        start = true;
                        level = 0;
                    }
                    if (c == -1) {
                        for (; balance > 0; balance--) {
                            tokens.add(")");
                        }
                        return tokens;
                    }
                    break;
                default:
                    if (start) {
                        if (levelsStack.isEmpty()) {
                            if (level > 0) {
                                throw new LexerException("Invalid indentation");
                            }
                            levelsStack.addLast(0);
                        } else {
                            if (levelsStack.peekLast() < level) {
                                levelsStack.addLast(level);
                            } else {
                                while (!levelsStack.isEmpty() && levelsStack.peekLast() > level) {
                                    levelsStack.removeLast();
                                    tokens.add(")");
                                    balance--;
                                }
                                if (levelsStack.isEmpty() || levelsStack.peekLast() < level) {
                                    throw new LexerException("Invalid indentation");
                                }
                                tokens.add(")");
                                balance--;

                            }
                        }
                        tokens.add("(");
                        balance++;
                        start = false;
                    }

                    if (c == '"') {
                        readEscapedString(reader, builder);
                        applyToken(builder, tokens);
                    } else {
                        builder.append((char) c);
                    }
            }
        }
    }

    public static boolean isLiteral(String token) {
        if (token.startsWith("\"")) {
            return true;
        }
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException ignore) {
            return false;
        }
    }

    private static void applyToken(StringBuilder builder, List<String> tokens) {
        if (builder.length() > 0) {
            String token = builder.toString();
            if (token.equals(":")) {
                tokens.add(")");
                tokens.add("(");
            } else {
                tokens.add(token);
            }
        }
        builder.setLength(0);
    }


    private static void readEscapedString(Reader reader, StringBuilder builder) throws IOException, LexerException {
        builder.append('"');
        boolean escaped = false;
        while (true) {
            int c = reader.read();
            if (c == -1) {
                throw new LexerException("Unterminated string");
            }

            builder.append((char) c);

            if (escaped) {
                escaped = false;
            } else {
                if (c == '"') {
                    return;
                }
                escaped = c == '\\';
            }
        }
    }
}
