package foo.interpreter;

import foo.model.ProjectNode;
import foo.runtime.StdLib;

import java.util.Arrays;

public class Runtime extends ProjectNode {
    public Runtime() {
        setName("Runtime");
        setId("Runtime");

        Arrays.asList(StdLib.class)
            .stream()
            .forEach(c -> getPackages().add(new SystemPackage(c)));
    }
}
