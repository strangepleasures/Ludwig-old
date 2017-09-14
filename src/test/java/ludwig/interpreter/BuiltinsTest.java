package ludwig.interpreter;

import ludwig.model.FunctionNode;
import ludwig.model.PackageNode;
import ludwig.runtime.StdLib;
import ludwig.script.Parser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuiltinsTest {

    private PackageNode systemPackage = Builtins.of(StdLib.class);

    @Test
    public void testPlus() {
        assertEquals(5.0, Interpreter.call((FunctionNode) Parser.Companion.item(systemPackage, "+"), 2.0, 3.0));
    }

    @Test
    public void testMinus() {
        assertEquals(-1.0, Interpreter.call((FunctionNode) Parser.Companion.item(systemPackage, "-"), 2.0, 3.0));
    }
}