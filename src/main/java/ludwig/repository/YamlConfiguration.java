package ludwig.repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ludwig.changes.Change;
import ludwig.model.Node;
import org.reflections.Reflections;

class YamlConfiguration {
    static final YAMLFactory YAML_FACTORY = new YAMLFactory();
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(YAML_FACTORY);

    static {
        OBJECT_MAPPER.setVisibility(OBJECT_MAPPER.getVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        new Reflections(Change.class.getPackage().getName())
            .getSubTypesOf(Change.class)
            .forEach(OBJECT_MAPPER::registerSubtypes);
        new Reflections(Node.class.getPackage().getName())
            .getSubTypesOf(Node.class)
            .forEach(OBJECT_MAPPER::registerSubtypes);
    }
}
