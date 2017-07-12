package foo.interpreter;

import org.junit.Test;

import static org.junit.Assert.*;

public class SystemPackageTest {

    private SystemPackage systemPackage = new SystemPackage();
    private Interpreter interpreter = new Interpreter();

    @Test
    public void testPlus() {
        assertEquals(5.0, interpreter.call(SystemPackage.PLUS, 2.0, 3.0));
    }

    @Test
    public void testMinus() {
        assertEquals(-1.0, interpreter.call(SystemPackage.MINUS, 2.0, 3.0));
    }

    @Test
    public void testIf() {
//        assertEquals(2.0, interpreter.call(SystemPackage.IF, true, 2.0, 3.0));
//        assertEquals(3.0, interpreter.call(SystemPackage.IF, false, 2.0, 3.0));
        assertNull(interpreter.call(SystemPackage.IF, false, 2.0));
    }
}