package foo.script;

import foo.model.ProjectNode;
import foo.workspace.Workspace;
import org.junit.Test;

import java.io.*;

public class ParserTest {

    @Test
    public void testParser() throws ParserException, IOException, LexerException {
        try(Reader reader = new InputStreamReader(ParserTest.class.getResourceAsStream("/system.foo"))) {
            Workspace workspace = new Workspace();
            ProjectNode project = new ProjectNode();
            project.setName("System");
            project.id("System");
            workspace.getProjects().add(project);
            Parser.parse(reader, workspace, project);
            System.out.println(project);
        }

    }

}