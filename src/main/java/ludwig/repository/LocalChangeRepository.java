package ludwig.repository;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import ludwig.changes.Change;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocalChangeRepository implements ChangeRepository {
    private final File file;

    public LocalChangeRepository(File file) {
        this.file = file;
    }

    @Override
    public void push(List<Change> changes) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(file, true);
             JsonGenerator generator = YamlConfiguration.YAML_FACTORY.createGenerator(fos, JsonEncoding.UTF8)) {
            for (Change change : changes) {
                YamlConfiguration.OBJECT_MAPPER.writeValue(generator, change);
            }
        }
    }

    @Override
    public List<Change> pull(String sinceChangeId) throws IOException {
        try (YAMLParser parser = YamlConfiguration.YAML_FACTORY.createParser(file);
             MappingIterator<Change> it = YamlConfiguration.OBJECT_MAPPER.readValues(parser, Change.class);) {
            List<Change> changes = new ArrayList<>();
            boolean accept = sinceChangeId == null;
            while (it.hasNext()) {
                Change change = it.nextValue();
                if (accept) {
                    changes.add(change);
                }
                accept = accept || change.getChangeId().equals(sinceChangeId);
            }
            return changes;
        }
    }
}
