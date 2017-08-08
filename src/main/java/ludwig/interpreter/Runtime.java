package ludwig.interpreter;

import ludwig.model.ProjectNode;
import ludwig.runtime.StdLib;

import java.util.Arrays;

public class Runtime extends ProjectNode {
    public Runtime() {
        setName("Runtime").id("Runtime");

        Arrays.asList(StdLib.class)
            .stream()
            .forEach(c -> add(new SystemPackage(c)));

        setReadonly(true);
    }
}
