package foo.ide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import foo.changes.Change;
import foo.model.NamedNode;
import foo.repository.ChangeRepository;
import foo.workspace.Workspace;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
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
    private WorkspaceTreeModel workspaceTreeModel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadSettings();  // TODO: async
        loadWorkspace(); // TODO: async

        TreeView<NamedNode> packageTree = new TreeView<>(workspaceTreeModel.getWorkspaceRoot());
        packageTree.setShowRoot(false);
        primaryStage.setScene(new Scene(packageTree, 640, 480));
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
                workspaceTreeModel = new WorkspaceTreeModel(workspace);
            } catch (IOException e) {
                // TODO:
            }
        }
    }
}
