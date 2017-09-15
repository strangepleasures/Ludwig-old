package ludwig.interpreter

import ludwig.model.FunctionNode
import ludwig.runtime.StdLib
import ludwig.script.Parser
import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltinsTest {

    private val systemPackage = Builtins.of(StdLib::class.java)

    @Test
    fun testPlus() {
        assertEquals(5.0, Interpreter.call(Parser.item(systemPackage, "+") as FunctionNode?, 2.0, 3.0))
    }

    @Test
    fun testMinus() {
        assertEquals(-1.0, Interpreter.call(Parser.item(systemPackage, "-") as FunctionNode?, 2.0, 3.0))
    }
}