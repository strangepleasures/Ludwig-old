package ludwig.script

import org.junit.Test

import java.io.IOException
import java.io.StringReader
import java.util.stream.Collectors

import org.junit.Assert.assertEquals

class LexerTest {
    @Test
    @Throws(Exception::class)
    fun read() {
        test("123", "( 123 )")
        test("foo\n\tx", "( foo ( x ) )")
        test("+\n\t2\n\t3", "( + ( 2 ) ( 3 ) )")
        test("def abs x :\n\tcond < x 0\n\t\tneg x\n\t\tx", "( def abs x ) ( ( cond < x 0 ( neg x ) ( x ) ) )")
        test("if call predicate x\n\treturn false", "( if ( call predicate x ) ( return false ) )")
        test("def fact n :\n" +
                "\tcond < n 2\n" +
                "\t\t1\n" +
                "\t\t* n fact - n 1",
                "( def fact n ) ( ( cond < n 2 ( 1 ) ( * n fact - n 1 ) ) )")

        test("def each seq consumer :\n" +
                "\tfor x seq\n" +
                "\t\tcall consumer x",
                "( def each seq consumer ) ( ( for x seq ( ( call consumer x ) ) ) )")
    }

    @Throws(IOException::class, LexerException::class)
    private fun test(src: String, tokens: String) {
        assertEquals(tokens, Lexer.read(StringReader(src)).stream().collect(Collectors.joining(" ")))
        assertEquals(tokens, Lexer.read(StringReader(src + "\n")).stream().collect(Collectors.joining(" ")))
    }
}