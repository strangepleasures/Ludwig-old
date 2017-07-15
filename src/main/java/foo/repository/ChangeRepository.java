package foo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.*;
import foo.changes.*;
import foo.changes.Package;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChangeRepository {
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper(yamlFactory);

    static {
        mapper.registerSubtypes(
            Project.class,
            Package.class,
            Function.class,
            Parameter.class,
            BoundCall.class,
            Reference.class,

            Position.class,
            Binding.class);
    }

    public static List<Change> fetch(URL url) throws IOException {
        YAMLParser parser = yamlFactory.createParser(url);
        List<Change> changes = new ArrayList<>();
        mapper.readValues(parser, Change.class).forEachRemaining(changes::add);
        return changes;
    }
}
