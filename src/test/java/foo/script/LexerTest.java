package foo.script;

import org.junit.Test;

import java.io.*;
import java.util.stream.Collectors;

import static foo.script.Lexer.read;
import static org.junit.Assert.assertEquals;

public class LexerTest {
    @Test
    public void testRead() throws Exception {
        test("", "");
        test("", "\n");
        test("( 123 )", "123");
        test("( 123 )", "\n123\n");
        test("( \"aaa\" )", "\"aaa\" ");
        test("( list 1 2 ( 3 ) )", "list 1 2\n 3");
        test("( foo ( x ) ) ( bar )", "foo\n x\nbar");
        test("( list 1 2 3 ) ( foo ( x ) ( y z ) ) ( bar )", "list 1 2 3\nfoo\n x\n y     z\nbar");
    }

    private static BufferedReader $(String s) {
        return new BufferedReader(new StringReader(s));
    }


    private void test(String expect, String source) throws IOException {
        assertEquals(expect, read(new StringReader(source)).stream().map(Object::toString).collect(Collectors.joining(" ")));
    }
}