package ludwig.script

import ludwig.model.ProjectNode
import ludwig.workspace.Workspace
import org.junit.Ignore
import org.junit.Test
import java.io.IOException
import java.io.InputStreamReader

@Ignore
class ParserTest {

    @Test
    @Throws(ParserException::class, IOException::class, LexerException::class)
    fun testParser() {
        InputStreamReader(ParserTest::class.java.getResourceAsStream("/all.foo")).use { reader ->
            val workspace = Workspace()
            val project = ProjectNode()
            project.name("System")
            project.id("System")
            workspace.projects.add(project)
            Parser.parse(reader, workspace, project)
            println(project)
        }

    }

}