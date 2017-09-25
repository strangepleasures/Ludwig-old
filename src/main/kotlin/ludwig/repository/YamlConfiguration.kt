package ludwig.repository

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import ludwig.changes.Change
import org.reflections.Reflections

internal object YamlConfiguration {
    val YAML_FACTORY = YAMLFactory()
    val OBJECT_MAPPER = ObjectMapper(YAML_FACTORY)

    init {
        OBJECT_MAPPER.setVisibility(OBJECT_MAPPER.visibilityChecker
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))

        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

        Reflections(Change::class.java.`package`.name)
                .getSubTypesOf(Change::class.java)
                .forEach({ OBJECT_MAPPER.registerSubtypes(it) })
    }
}
