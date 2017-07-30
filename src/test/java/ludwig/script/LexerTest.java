package ludwig.script;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LexerTest {
    @Test
    public void read() throws Exception {
        test("123", "( 123 )");
        test("foo\n\tx", "( foo ( x ) )");
        test("+\n\t2\n\t3", "( + ( 2 ) ( 3 ) )");
        test("def abs x :\n\tcond < x 0\n\t\tneg x\n\t\tx", "( def abs x ) ( ( cond < x 0 ( neg x ) ( x ) ) )");
        test("if call predicate x\n\treturn false", "( if ( call predicate x ) ( return false ) )");
        test("def fact n :\n" +
            "\tcond < n 2\n" +
            "\t\t1\n" +
            "\t\t* n fact - n 1",
            "( def fact n ) ( ( cond < n 2 ( 1 ) ( * n fact - n 1 ) ) )");
    }

    private void test(String src, String tokens) throws IOException, LexerException {
        assertEquals(tokens, Lexer.read(new StringReader(src)).stream().collect(Collectors.joining(" ")));
        assertEquals(tokens, Lexer.read(new StringReader(src + "\n")).stream().collect(Collectors.joining(" ")));
    }
}