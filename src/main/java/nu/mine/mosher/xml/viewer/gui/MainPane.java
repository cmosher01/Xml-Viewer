package nu.mine.mosher.xml.viewer.gui;


import nu.mine.mosher.xml.viewer.XmlViewer;
import nu.mine.mosher.xml.viewer.model.*;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


class MainPane extends JPanel {
    private TreePanel tree;
    private PropertiesPanel properties;

    public MainPane(final DomTreeModel model) {
        super(new BorderLayout(), true);

        setOpaque(true);

        final int width = width();
        final int height = height();

        setPreferredSize(new Dimension(width, height));

        addNotify();

        this.tree = createTreeControl(model);
        final JScrollPane scrlTree = new JScrollPane(this.tree);
        scrlTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrlTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this.properties = new PropertiesPanel();
        final JScrollPane scrlProp = new JScrollPane(this.properties);
        scrlProp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrlProp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrlTree, scrlProp);
        final double ratio = 1.0 / 3.0;
        splitPane.setResizeWeight(ratio);
        splitPane.setDividerLocation((int)Math.round(ratio * width));
        add(splitPane);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                super.componentResized(e);
                width(MainPane.this.getWidth());
                height(MainPane.this.getHeight());
            }
        });
    }

    public void updateMenu(final boolean haveTree) {
        this.tree.updateMenu(haveTree);
    }

    public void appendViewMenuItems(final JMenu menuView) {
        this.tree.appendViewMenuItems(menuView);
    }

    private TreePanel createTreeControl(final DomTreeModel model) {
        final TreePanel tree = new TreePanel(model);
        tree.init();
        tree.addTreeSelectionListener(e -> {
            try {
                final TreePath sel = e.getNewLeadSelectionPath();
                final Optional<DomTreeNode> currentNode;
                if (Objects.nonNull(sel)) {
                    currentNode = Optional.ofNullable((DomTreeNode)sel.getLastPathComponent());
                } else {
                    currentNode = Optional.empty();
                }
                properties.display(currentNode);
            } catch (final Throwable ex) {
                throw new IllegalStateException(ex);
            }
        });
        return tree;
    }

    private static int width() {
        return Integer.parseInt(XmlViewer.prefs().get("width", "1024"));
    }

    private static void width(final int width) {
        XmlViewer.prefs().put("width", Integer.toString(width, 10));
    }

    private static int height() {
        return Integer.parseInt(XmlViewer.prefs().get("height", "768"));
    }

    private static void height(final int height) {
        XmlViewer.prefs().put("height", Integer.toString(height, 10));
    }
}
