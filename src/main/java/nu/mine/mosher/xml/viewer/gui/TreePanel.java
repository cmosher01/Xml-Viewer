package nu.mine.mosher.xml.viewer.gui;


import nu.mine.mosher.xml.viewer.model.DomTreeModel;
import nu.mine.mosher.xml.viewer.model.DomTreeNode;

import javax.swing.*;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.*;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.*;

import static nu.mine.mosher.xml.viewer.XmlViewer.prefs;
import static nu.mine.mosher.xml.viewer.gui.XmlViewerGui.ACCEL;


public class TreePanel extends JTree {
    private int visualSize = prefZoom();

    public TreePanel(DomTreeModel model) {
        super(model);
    }

    public void init() {
        setFont(new Font(Font.DIALOG, Font.PLAIN, Math.round(1.0f * visualSize)));
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setShowsRootHandles(true);
        setRootVisible(true);
        final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
    }

    private static int prefZoom() {
        return prefs().getInt("zoom", 10);
    }

    private static void prefZoom(final int zoom) {
        prefs().putInt("zoom", zoom);
    }

    public void appendViewMenuItems(final JMenu menu) {
        final JMenuItem itemZoomIn = new JMenuItem("Zoom in");
        itemZoomIn.setMnemonic(KeyEvent.VK_I);
        itemZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ACCEL));
        itemZoomIn.addActionListener(e -> zoom(+1));
        menu.add(itemZoomIn);

        final JMenuItem itemZoomOut = new JMenuItem("Zoom out");
        itemZoomOut.setMnemonic(KeyEvent.VK_O);
        itemZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ACCEL));
        itemZoomOut.addActionListener(e -> zoom(-1));
        menu.add(itemZoomOut);

        menu.addSeparator();

        final JMenuItem itemExpandAll = new JMenuItem("Expand all");
        itemExpandAll.setMnemonic(KeyEvent.VK_X);
        itemExpandAll.addActionListener(e -> expandAll());
        menu.add(itemExpandAll);

        final JMenuItem itemCollapseAll = new JMenuItem("Collapse all");
        itemCollapseAll.setMnemonic(KeyEvent.VK_C);
        itemCollapseAll.addActionListener(e -> collapseAll());
        menu.add(itemCollapseAll);
    }

    private void zoom(final int i) {
        zoomContrained(4, Math.round(i * (this.visualSize / 8.0f + 1.0f)), 64);
        setFont(new Font(Font.DIALOG, Font.PLAIN, Math.round(1.0f * visualSize)));
        repaint();
    }

    private void zoomContrained(final int min, final int delta, final int max) {
        final int v = this.visualSize + delta;
        if (v < min) {
            this.visualSize = min;
        } else if (max < v) {
            this.visualSize = max;
        } else {
            this.visualSize = v;
        }
        prefZoom(this.visualSize);
    }

    private void expandAll() {
        expandAll(this, true);
    }

    private void collapseAll() {
        expandAll(this, false);
    }

    private static void expandAll(final JTree tree, final boolean expand) {
        final Object root = tree.getModel().getRoot();
        if (Objects.nonNull(root)) {
            final TreeUI treeUI = tree.getUI();
            try {
                tree.setUI(null);
                expandAll(tree, new TreePath(root), expand);
            } finally {
                tree.setUI(treeUI);
            }
        }
    }

    private static boolean expandAll(final JTree tree, final TreePath parent, final boolean expand) {
        boolean childExpandCalled = false;

        final DomTreeNode node = (DomTreeNode)parent.getLastPathComponent();
        for (int i = 0; i < node.getChildCount(); ++i) {
            final DomTreeNode child = node.getChild(i);
            final TreePath path = parent.pathByAddingChild(child);
            childExpandCalled = expandAll(tree, path, expand) || childExpandCalled;
        }

        if (!childExpandCalled) {
            if (expand) {
                tree.expandPath(parent);
            } else {
                tree.collapsePath(parent);
            }
        }

        return childExpandCalled;
    }

    @Override
    public Enumeration<TreePath> getExpandedDescendants(final TreePath parent) {
        if (!isExpanded(parent)) {
            return null;
        }
        final ArrayList<TreePath> list = new ArrayList<>(4096);
        getOpenedChild(parent, list);
        return Collections.enumeration(list);
    }

    private void getOpenedChild(final TreePath paramTreeNode, final Collection<TreePath> list)
    {
        final Object parent = paramTreeNode.getLastPathComponent();
        final TreeModel model = getModel();
        final int n = model.getChildCount(parent);
        for (int i = 0; i < n; ++i) {
            final Object child = model.getChild(parent, i);
            final TreePath childPath = paramTreeNode.pathByAddingChild(child);
            if (!model.isLeaf(child) && isExpanded(childPath)) {
                list.add(childPath);
                getOpenedChild(childPath, list);
            }
        }
    }
}
