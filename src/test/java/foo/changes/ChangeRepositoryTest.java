package foo.changes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import foo.repository.ChangeRepository;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class ChangeRepositoryTest {
    @Test
    public void fetch() throws Exception {
        URL url = ChangeRepositoryTest.class.getResource("/changes.yaml");
        List<Change> changes = ChangeRepository.fetch(url);

        assertEquals(5, changes.size());
    }

}