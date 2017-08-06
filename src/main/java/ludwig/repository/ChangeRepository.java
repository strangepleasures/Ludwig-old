package ludwig.repository;

import ludwig.changes.Change;

import java.io.IOException;
import java.util.List;

public interface ChangeRepository {
    void push(List<Change> changes) throws IOException;

    List<Change> pull(String sinceChangeId) throws IOException;
}
