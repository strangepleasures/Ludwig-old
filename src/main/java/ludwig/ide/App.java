package ludwig.ide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import ludwig.changes.Change;
import ludwig.repository.ChangeRepository;
import ludwig.repository.LocalChangeRepository;
import ludwig.workspace.Environment;
import ludwig.workspace.Workspace;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class App extends Application {
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper(yamlFactory);
    private static final File SETTINGS_FILE = new File("./application.yaml");

    private Settings settings;
    private ChangeRepository repository;
    @Getter
    private Environment environment = new Environment();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadSettings();  // TODO: async
        loadWorkspace(); // TODO: async
        BorderPane borderPane = new BorderPane();

        // do menu bar - make own class
        MenuBar menuBar = new MenuBar();
        menuBar.isUseSystemMenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem newMenuItem = new MenuItem("New");
        MenuItem openMenuItem = new MenuItem("Open");
        MenuItem exitMenuItem = new MenuItem("Exit");

        exitMenuItem.setOnAction(actionEvent -> {/**not implemented**/});
        openMenuItem.setOnAction(actionEvent -> {
                FileChooser dialog = new FileChooser();
                dialog.setTitle("Open Project");
                File file = dialog.showOpenDialog(new Stage());
                if(file != null){
                    try {
                        settings.setProject(file.toURI().toURL());
                        start(new Stage());
                        primaryStage.close();
                    } catch (Exception e) {
                        //failed to open
                    }
                }
            }
        );
        exitMenuItem.setOnAction(actionEvent -> Platform.exit());

        fileMenu.getItems().addAll(newMenuItem, openMenuItem, new SeparatorMenuItem(), exitMenuItem);
        menuBar.getMenus().addAll(fileMenu);
        borderPane.setTop(menuBar);

        SplitPane splitPane = new SplitPane();
        EditorPane leftEditorPane = new EditorPane(environment, settings);
        EditorPane rightEditorPane = new EditorPane(environment, settings);
        leftEditorPane.setAnotherPane(rightEditorPane);
        rightEditorPane.setAnotherPane(leftEditorPane);
        splitPane.getItems().addAll(leftEditorPane, rightEditorPane);
        borderPane.setCenter(splitPane);

        primaryStage.setScene(new Scene(borderPane, 1024, 768));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        saveSettings(); // TODO: async
        super.stop();
    }

    private void loadSettings() {
        if(settings == null) {
            try {
                settings = mapper.readValue(SETTINGS_FILE, Settings.class);
            } catch (IOException e) {
                settings = new Settings();
            }
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
                environment.getWorkspace().load(changes);

                environment.getWorkspace().changeListeners().add(change -> {
                    try {
                        if (!environment.getWorkspace().isLoading()) {
                            repository.push(Collections.singletonList(change));
                        }
                    } catch (IOException e) {

                    }
                });
            } catch (IOException e) {
                // TODO:
            }
        }
    }
}
