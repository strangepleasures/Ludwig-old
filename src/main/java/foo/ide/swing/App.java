package foo.ide.swing;

import foo.model.NamedNode;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FOO");

        final JSplitPane topLevelSplitPane = new JSplitPane();
        final JSplitPane leftSplitPane = new JSplitPane();
        topLevelSplitPane.setLeftComponent(leftSplitPane);

        JTree packageTree = new JTree();
        leftSplitPane.setLeftComponent(packageTree);

        JList<NamedNode> functionList = new JList<>();
        leftSplitPane.setRightComponent(functionList);

        JPanel panel = new JPanel();
        topLevelSplitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout());
        JTable signatureTable = new JTable();
        panel.add(signatureTable, BorderLayout.PAGE_START);
        JFXPanel jfxPanel = new JFXPanel();
        panel.add(jfxPanel, BorderLayout.CENTER);
        Platform.runLater(() -> {
            WebView webView = new WebView();
            jfxPanel.setScene(new Scene(webView));
        });
        frame.setContentPane(topLevelSplitPane);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}
