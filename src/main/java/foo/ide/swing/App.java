package foo.ide.swing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import foo.changes.Change;
import foo.ide.Settings;
import foo.model.*;
import foo.repository.ChangeRepository;
import foo.utils.NodeUtils;
import foo.workspace.Workspace;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class App {
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper(yamlFactory);
    private static final File SETTINGS_FILE = new File("./application.yaml");
    private static volatile WebView webView;

    private static Settings settings;
    private static Workspace workspace = new Workspace();

    public static void main(String[] args) {
        loadSettings();  // TODO: async
        loadWorkspace(); // TODO: async

        JFrame frame = new JFrame("FOO");

        final JSplitPane topLevelSplitPane = new JSplitPane();
        final JSplitPane leftSplitPane = new JSplitPane();
        topLevelSplitPane.setLeftComponent(leftSplitPane);

        PackageTree packageTree = new PackageTree(workspace);
        leftSplitPane.setLeftComponent(packageTree);
        packageTree.setMinimumSize(new Dimension(100, 100));
        packageTree.setPreferredSize(new Dimension(200, 200));

        JList<NamedNode> functionList = new JList<>();
        leftSplitPane.setRightComponent(functionList);
        functionList.setMinimumSize(new Dimension(100, 100));
        functionList.setPreferredSize(new Dimension(200, 200));
        JPanel panel = new JPanel();
        topLevelSplitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout());
        JFXPanel jfxPanel = new JFXPanel();
        panel.add(jfxPanel, BorderLayout.CENTER);

        packageTree.addTreeSelectionListener(e -> {
            if (packageTree.getSelectionModel().getSelectionPath().getLastPathComponent() != null) {
                Object sel = packageTree.getSelectionModel().getSelectionPath().getLastPathComponent();
                if (sel instanceof PackageTreeNode) {
                    PackageTreeNode node = (PackageTreeNode) sel;
                    functionList.setModel(new FunctionListModel((PackageNode) node.getUserObject()));
                }
            }
        });

        functionList.addListSelectionListener(e -> {
            FunctionNode functionNode = (FunctionNode) functionList.getSelectedValue();
            String content = functionNode != null ? NodeUtils.toHtml(functionNode) : "Nothing to display";

            Platform.runLater(() -> webView.getEngine().loadContent(content));
        });

        Platform.runLater(() -> {
            webView = new WebView();
            jfxPanel.setScene(new Scene(webView));
        });

        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                saveSettings();
            }
        });

        frame.setContentPane(topLevelSplitPane);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

    }

    private static void loadSettings() {
        try {
            settings = mapper.readValue(SETTINGS_FILE, Settings.class);
        } catch (IOException e) {
            settings = new Settings();
        }
    }

    private static void saveSettings() {
        SETTINGS_FILE.delete();
        try {
            mapper.writeValue(SETTINGS_FILE, settings);
        } catch (IOException e) {
        }
    }

    private static void loadWorkspace() {
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
