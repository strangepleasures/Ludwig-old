package ludwig.model;

import java.util.List;

public interface Signature {
    String getName();

    List<String> arguments();
}
