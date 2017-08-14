package ludwig.interpreter;

import ludwig.model.Signature;

import java.util.HashMap;
import java.util.Map;

public class Type {
    private final Map<Signature, Signature> overrides = new HashMap<>();

    public Signature implementation(Signature signature) {
        return overrides.getOrDefault(signature, signature);
    }
}
