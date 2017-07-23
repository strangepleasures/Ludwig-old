package foo.ide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import foo.changes.Change;
import foo.repository.ChangeRepository;
import foo.workspace.Workspace;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class App extends Application {
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper(yamlFactory);
    private static final File SETTINGS_FILE = new File("./application.yaml");

    private Settings settings;
    private Workspace workspace = new Workspace();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadSettings();  // TODO: async
        loadWorkspace(); // TODO: async

        SplitPane splitPane = new SplitPane();

        EditorPane leftEditorPane = new EditorPane(workspace, settings);
        EditorPane rightEditorPane = new EditorPane(workspace, settings);
        splitPane.getItems().addAll(leftEditorPane, rightEditorPane);

        primaryStage.setScene(new Scene(splitPane, 1024, 768));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        saveSettings(); // TODO: async
        super.stop();
    }

    private void loadSettings() {
        try {
            settings = mapper.readValue(SETTINGS_FILE, Settings.class);
        } catch (IOException e) {
            settings = new Settings();
        }
    }

    private void saveSettings() {
        SETTINGS_FILE.delete();
        try {
            mapper.writeValue(SETTINGS_FILE, settings);
        } catch (IOException e) {
        }
    }

    private void loadWorkspace() {
        if (settings.getProject() != null) {
            try {
                List<Change> changes = ChangeRepository.fetch(settings.getProject());
                workspace.apply(changes);
            } catch (IOException e) {
                // TODO:
            }
        }
    }
}
