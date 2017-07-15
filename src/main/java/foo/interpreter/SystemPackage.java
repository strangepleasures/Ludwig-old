package foo.interpreter;

import foo.model.PackageNode;

import java.lang.reflect.Method;

public class SystemPackage extends PackageNode {
    public SystemPackage() {
        setName("system");
        setId("system");

        for (Method method : StdLib.class.getDeclaredMethods()) {
            getItems().add(new NativeFunctionNode(method));
        }
    }
}

