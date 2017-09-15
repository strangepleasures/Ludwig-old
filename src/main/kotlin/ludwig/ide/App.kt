package ludwig.ide

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import ludwig.changes.Change
import ludwig.repository.ChangeRepository
import ludwig.repository.LocalChangeRepository
import ludwig.workspace.Environment
import java.io.File
import java.io.IOException
import java.util.function.Consumer

class App : Application() {

    private var settings: Settings? = null
    private var repository: ChangeRepository? = null
    private val environment = Environment()

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        loadSettings()  // TODO: async
        loadWorkspace() // TODO: async
        val borderPane = BorderPane()

        // do menu bar - make own class
        val menuBar = MenuBar()
        menuBar.isUseSystemMenuBar

        val fileMenu = Menu("File")
        val newMenuItem = MenuItem("New")
        val openMenuItem = MenuItem("Open")
        val exitMenuItem = MenuItem("Exit")

        exitMenuItem.setOnAction {
            /**not implemented */
            actionEvent ->
        }
        openMenuItem.setOnAction { actionEvent ->
            val dialog = FileChooser()
            dialog.title = "Open Project"
            val file = dialog.showOpenDialog(Stage())
            if (file != null) {
                try {
                    settings!!.project = file.toURI().toURL()
                    start(Stage())
                    primaryStage.close()
                } catch (e: Exception) {
                    //failed to open
                }

            }
        }
        exitMenuItem.setOnAction { actionEvent -> Platform.exit() }

        fileMenu.items.addAll(newMenuItem, openMenuItem, SeparatorMenuItem(), exitMenuItem)
        menuBar.menus.addAll(fileMenu)
        borderPane.top = menuBar

        val splitPane = SplitPane()
        val leftEditorPane = EditorPane(environment, settings!!)
        val rightEditorPane = EditorPane(environment, settings!!)
        leftEditorPane.anotherPane(rightEditorPane)
        rightEditorPane.anotherPane(leftEditorPane)
        splitPane.items.addAll(leftEditorPane, rightEditorPane)
        borderPane.center = splitPane

        val scene = Scene(borderPane, 1024.0, 768.0)
        primaryStage.scene = scene
        scene.stylesheets.add(App::class.java.getResource("/style.css").toExternalForm())
        primaryStage.show()
    }

    @Throws(Exception::class)
    override fun stop() {
        saveSettings() // TODO: async
        super.stop()
    }

    private fun loadSettings() {
        if (settings == null) {
            try {
                settings = mapper.readValue(SETTINGS_FILE, Settings::class.java)
            } catch (e: IOException) {
                settings = Settings()
            }

        }
    }

    private fun saveSettings() {
        SETTINGS_FILE.delete()
        try {
            mapper.writeValue(SETTINGS_FILE, settings)
        } catch (e: IOException) {
        }

    }

    private fun loadWorkspace() {
        if (settings!!.project != null) {
            try {
                if ("file" == settings!!.project!!.protocol) {
                    repository = LocalChangeRepository(File(settings!!.project!!.file))
                }
                val changes = repository!!.pull(null)
                environment.workspace().load(changes)

                environment.workspace().changeListeners().add(Consumer<Change> { change ->
                    try {
                        if (!environment.workspace().isLoading) {
                            repository!!.push(listOf(change))
                        }
                    } catch (e: IOException) {

                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        private val yamlFactory = YAMLFactory()
        private val mapper = ObjectMapper(yamlFactory)
        private val SETTINGS_FILE = File("./application.yaml")

        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java)
        }
    }
}
