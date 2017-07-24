package foo.script;


import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Lexer {
    public static final Object BEGIN = new Object() {
        @Override
        public String toString() {
            return "(";
        }
    };
    public static final Object END = new Object() {
        @Override
        public String toString() {
            return ")";
        }
    };

    public static List<Object> read(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        List<Object> tokens = new ArrayList<>();
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
                        applyToken(builder, tokens, false);
                    }
                    break;
                case -1:
                case '\n':
                    if (!start) {
                        applyToken(builder, tokens, false);
                        start = true;
                        level = 0;
                    }
                    if (c == -1) {
                        for (; balance > 0; balance--) {
                            tokens.add(END);
                        }
                        return tokens;
                    }
                    break;
                default:
                    if (start) {
                        if (levelsStack.isEmpty()) {
                            if (level > 0) {
                                throw new RuntimeException("Invalid indentation");
                            }
                            levelsStack.addLast(0);
                        } else {
                            if (levelsStack.peekLast() < level) {
                                levelsStack.addLast(level);
                            } else {
                                while (!levelsStack.isEmpty() && levelsStack.peekLast() > level) {
                                    levelsStack.removeLast();
                                    tokens.add(END);
                                    balance--;
                                }
                                if (levelsStack.isEmpty() || levelsStack.peekLast() < level) {
                                    throw new RuntimeException("Invalid indentation");
                                }
                                tokens.add(END);
                                balance--;

                            }
                        }
                        tokens.add(BEGIN);
                        balance++;
                        start = false;
                    }

                    if (c == '"') {
                        readEscapedString(reader, builder);
                        applyToken(builder, tokens, true);
                    } else {
                        builder.append((char) c);
                    }
            }
        }
    }

    private static void applyToken(StringBuilder builder, List<Object> tokens, boolean string) {
        if (string) {
            tokens.add(new Str(StringEscapeUtils.unescapeJavaScript(builder.toString())));
        } else if (builder.length() > 0) {
            String token = builder.toString();
            if (token.equals(":")) {
                tokens.add(END);
                tokens.add(BEGIN);
            } else {
                tokens.add(parseToken(token, string));
            }
        }
        builder.setLength(0);
    }


    private static Object parseToken(String s, boolean string) {
        if (string) {
            return new Str(StringEscapeUtils.unescapeJavaScript(s));
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e2) {
                return s;
            }
        }
    }


    private static void readEscapedString(Reader reader, StringBuilder builder) throws IOException {
        boolean escaped = false;
        while (true) {
            int c = reader.read();
            if (c == -1) {
                throw new RuntimeException("Unterminated string");
            }

            if (escaped) {
                escaped = false;
            } else {
                if (c == '"') {
                    return;
                }
                escaped = c == '\\';
            }

            builder.append((char) c);
        }
    }
}
