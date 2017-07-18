package foo.interpreter;

import foo.model.ProjectNode;
import foo.runtime.StdLib;

import java.util.Arrays;

public class Runtime extends ProjectNode {
    public Runtime() {
        name("Runtime").id("Runtime");

        Arrays.asList(StdLib.class)
            .stream()
            .forEach(c -> add(new SystemPackage(c)));
    }
}
