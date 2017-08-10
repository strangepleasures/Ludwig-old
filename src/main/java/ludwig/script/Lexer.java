package ludwig.script;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Lexer {
    private final Reader reader;
    private final StringBuilder builder = new StringBuilder();
    private final List<String> tokens = new ArrayList<>();
    private final Deque<Integer> levelsStack = new ArrayDeque<>();
    private int balance = 0;

    private Lexer(Reader reader) {
        this.reader = reader;
    }



    public static List<String> read(Reader reader) throws IOException, LexerException {
        return new Lexer(reader).read();
    }

    private List<String> read() throws IOException, LexerException {

        boolean start = true;
        int level = 0;

        while (true) {
            int c = reader.read();
            switch (c) {
                case ' ':
                case '\t':
                    if (start) {
                        level++;
                    } else {
                        applyToken();
                    }
                    break;
                case -1:
                case '\n':
                    if (!start) {
                        applyToken();
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

                                }
                            }
                            for (int i = balance; i > levelsStack.peekLast(); i--) {
                                tokens.add(")");
                                balance--;
                            }
                        }
                        tokens.add("(");
                        balance++;
                        start = false;
                    }

                    if (c == '\'') {
                        readEscapedString();
                        applyToken();
                    } else {
                        builder.append((char) c);
                    }
            }
        }
    }

    public static boolean isLiteral(String token) {
        if (token.startsWith("\'")) {
            return true;
        }
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException ignore) {
            return false;
        }
    }

    private void applyToken() {
        if (builder.length() > 0) {
            String token = builder.toString();
            if (token.equals("call")) {
                tokens.add("(");
                balance++;
            }
            if (token.equals(":")) {
                tokens.add(")");
                tokens.add("(");
            } else {
                tokens.add(token);
            }
        }
        builder.setLength(0);
    }


    private void readEscapedString() throws IOException, LexerException {
        builder.append('\'');
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
                if (c == '\'') {
                    return;
                }
                escaped = c == '\\';
            }
        }
    }
}
