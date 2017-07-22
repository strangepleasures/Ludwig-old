package foo.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import foo.changes.Change;
import foo.model.Node;
import org.reflections.Reflections;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChangeRepository {
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper(yamlFactory);

    static {
        mapper.setVisibility(mapper.getVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        new Reflections(Change.class.getPackage().getName())
            .getSubTypesOf(Change.class)
            .forEach(mapper::registerSubtypes);
        new Reflections(Node.class.getPackage().getName())
            .getSubTypesOf(Node.class)
            .forEach(mapper::registerSubtypes);
    }

    public static List<Change> fetch(URL url) throws IOException {
        YAMLParser parser = yamlFactory.createParser(url);
        List<Change> changes = new ArrayList<>();
        mapper.readValues(parser, Change.class).forEachRemaining(changes::add);
        return changes;
    }
}
