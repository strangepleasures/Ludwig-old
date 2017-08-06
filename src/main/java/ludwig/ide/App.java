package ludwig.ide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ludwig.changes.Change;
import ludwig.repository.ChangeRepository;
import ludwig.repository.LocalChangeRepository;
import ludwig.workspace.Workspace;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class App extends Application {
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper(yamlFactory);
    private static final File SETTINGS_FILE = new File("./application.yaml");

    private Settings settings;
    private ChangeRepository repository;
    private Workspace workspace = new Workspace();

    public static void main(String[] args) {
        launch(args);
    }

    private ActionTarget actionTarget;

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadSettings();  // TODO: async
        loadWorkspace(); // TODO: async

        SplitPane splitPane = new SplitPane();

        EditorPane leftEditorPane = new EditorPane(this);
        EditorPane rightEditorPane = new EditorPane(this);
        leftEditorPane.setAnotherPane(rightEditorPane);
        rightEditorPane.setAnotherPane(leftEditorPane);
        splitPane.getItems().addAll(leftEditorPane, rightEditorPane);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(splitPane);

        Button addButton = new Button("", icon("add"));
        addButton.setOnAction(e -> {
            if (actionTarget != null) {
                actionTarget.add();
            }
        });
        Button deleteButton = new Button("", icon("delete"));
        deleteButton.setOnAction(e -> {
            if (actionTarget != null) {
                actionTarget.delete();
            }
        });

        ToolBar topToolBar = new ToolBar(
            addButton,
            deleteButton
        );
        borderPane.setTop(topToolBar);

        primaryStage.setScene(new Scene(borderPane, 1024, 768));
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
                if ("file".equals(settings.getProject().getProtocol())) {
                    repository = new LocalChangeRepository(new File(settings.getProject().getFile()));
                }
                List<Change> changes = repository.pull(null);
                workspace.apply(changes);
            } catch (IOException e) {
                // TODO:
            }
        }
    }

    public App setActionTarget(ActionTarget actionTarget) {
        this.actionTarget = actionTarget;
        return this;
    }

    private static ImageView icon(String name) {
        return new ImageView(new Image(App.class.getResourceAsStream("/icons/" + name + ".png")));
    }

    public Workspace getWorkspace() {
        return workspace;
    }
}
