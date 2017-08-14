package ludwig.interpreter;

import ludwig.model.ProjectNode;
import ludwig.runtime.StdLib;

import java.util.Arrays;

public class Builtins extends ProjectNode {
    public Builtins() {
        name("Runtime").id("Runtime");

        Arrays.asList(StdLib.class)
            .stream()
            .forEach(c -> add(new SystemPackage(c)));

        setReadonly(true);
    }
}
