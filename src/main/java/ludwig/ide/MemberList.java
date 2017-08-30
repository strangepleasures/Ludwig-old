package ludwig.ide;

import com.sun.javafx.collections.ObservableListWrapper;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.scene.control.*;
import ludwig.changes.*;
import ludwig.interpreter.*;
import ludwig.model.*;
import ludwig.script.Lexer;
import ludwig.script.LexerException;
import ludwig.utils.NodeUtils;
import ludwig.workspace.Environment;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static ludwig.utils.NodeUtils.isReadonly;


public class MemberList extends ListView<Node<?>> {
    private final Environment environment;
    private PackageNode packageNode;

    public MemberList(Environment environment) {
        this.environment = environment;

        setCellFactory(listView -> new SignatureListCell());
        setContextMenu(ContextMenuFactory.menu(new Actions()));
    }

    public void setPackage(PackageNode packageNode) {
        this.packageNode = packageNode;
        getItems().clear();

        if (packageNode != null) {
            setItems(new ObservableListWrapper<>(packageNode.children()
                .stream()
                .filter(item -> !(item instanceof PackageNode))
                .map(item -> (Node<?>) item)
                .sorted(Comparator.comparing(NodeUtils::signature))
                .collect(Collectors.toList())));

            if (!getItems().isEmpty()) {
                getSelectionModel().select(0);
            }
        }
    }

    public class Actions {
        public void addFunction() {
            if (isReadonly(packageNode)) {
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add a function");
            dialog.setHeaderText("Enter function signature");

            dialog.showAndWait().ifPresent(signature -> {
                List<String> parts = Collections.emptyList();
                try {
                    parts = Lexer.read(new StringReader(signature))
                        .stream()
                        .filter(s -> !s.equals("(") && !s.equals(")"))
                        .collect(Collectors.toList());
                } catch (IOException | LexerException t) {
                }
                if (!parts.isEmpty()) {
                    List<Change> changes = new ArrayList<>();

                    InsertNode insertFn = new InsertNode()
                        .node(new FunctionNode().name(parts.get(0)).id(Change.newId()))
                        .parent(packageNode.id());

                    changes.add(insertFn);

                    String prev = null;
                    for (int i = 1; i < parts.size(); i++) {
                        String id = Change.newId();
                        changes.add(new InsertNode()
                            .node(new VariableNode().name(parts.get(i)).id(id))
                            .parent(insertFn.node().id())
                            .prev(prev));
                        prev = id;
                    }

                    environment.getWorkspace().apply(changes);

                    FunctionNode fn = environment.getWorkspace().node(insertFn.node().id());
                    getSelectionModel().select(fn);
                }
            });
        }


        public void override() {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Override");
            dialog.setHeaderText("");

            AutoCompletionTextFieldBinding<Node> autoCompletionTextFieldBinding =
                new AutoCompletionTextFieldBinding<>(
                    dialog.getEditor(),
                    param -> {
                        List<Node> suggestions = new ArrayList<>();
                        suggestions.addAll(environment.getSymbolRegistry().symbols(param.getUserText()));

                        return suggestions;
                    },
                    new NodeStringConverter());

            autoCompletionTextFieldBinding.setVisibleRowCount(20);
            Node[] ref = {null};
            autoCompletionTextFieldBinding.setOnAutoCompleted(e -> {
                ref[0] = e.getCompletion();
            });


            dialog.showAndWait().ifPresent(signature -> {
                if (ref[0] != null) {
                    List<Change> changes = new ArrayList<>();

                    InsertNode insertOverride = new InsertNode()
                        .node(new OverrideNode().id(Change.newId()))
                        .parent(packageNode.id());

                    changes.add(insertOverride);

                    changes.add(new InsertReference()
                        .ref(ref[0].id())
                        .id(Change.newId())
                        .parent(insertOverride.node().id()));

                    environment.getWorkspace().apply(changes);

                    OverrideNode o = environment.getWorkspace().node(insertOverride.node().id());
                    getSelectionModel().select(o);
                }
            });
        }

        public void run() {
            Node fn = getSelectionModel().getSelectedItem();
            if (!(fn instanceof NamedNode)) {
                return;
            }
            try {
                Callable callable = (fn instanceof Callable) ? (Callable) fn : new CallableRef(fn);
                Object result;
                if (callable.argCount() > 0) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Execute function");
                    dialog.setHeaderText("Enter function arguments");
                    dialog.setContentText(((NamedNode) fn).name());

                    Optional<String> params = dialog.showAndWait();
                    if (params.isPresent()) {
                        Object[] args = Lexer.read(new StringReader(params.get()))
                            .stream()
                            .filter(s -> !s.equals("(") && !s.equals(")"))
                            .map(NodeUtils::parseLiteral)
                            .map(x -> callable.isLazy() ? (Delayed<?>) () -> x : x)
                            .toArray();
                        result = callable.call(args);
                    } else {
                        return;
                    }

                } else {
                    result = callable.call();
                }
                new Alert(Alert.AlertType.INFORMATION, "Result: " + NodeUtils.formatLiteral(result)).show();
            } catch (Exception err) {
                err.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error: " + err.toString()).show();
            }
        }

        public void delete() {
            if (isReadonly(packageNode)) {
                return;
            }
            Node selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                environment.getWorkspace().apply(singletonList(new Delete().id(selectedItem.id())));
            }
        }
    }
}