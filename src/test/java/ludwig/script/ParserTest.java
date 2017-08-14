package ludwig.script;

import ludwig.model.ProjectNode;
import ludwig.workspace.Workspace;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

@Ignore
public class ParserTest {

    @Test
    public void testParser() throws ParserException, IOException, LexerException {
        try(Reader reader = new InputStreamReader(ParserTest.class.getResourceAsStream("/all.foo"))) {
            Workspace workspace = new Workspace();
            ProjectNode project = new ProjectNode();
            project.name("System");
            project.id("System");
            workspace.getProjects().add(project);
            Parser.parse(reader, workspace, project);
            System.out.println(project);
        }

    }

}