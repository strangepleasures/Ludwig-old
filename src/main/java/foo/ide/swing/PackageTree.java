package foo.ide.swing;

import foo.workspace.Workspace;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;

class PackageTree extends JTree {
    private static final Color BACKGROUND = new Color(-16166704);

    PackageTree(Workspace workspace) {
        super(new PackageTreeModel(workspace));

        setUI(new BasicTreeUI() {
            @Override
            protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
                g.setColor(tree.isRowSelected(row) ? BACKGROUND : Color.WHITE);
                g.fillRect(0, row * tree.getRowHeight(), tree.getWidth(), tree.getRowHeight());
                super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
            }
        });

        setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                component.setPreferredSize(new Dimension(tree.getWidth(), tree.getRowHeight()));
                return component;
            }
        });

        setRootVisible(false);

    }
}
