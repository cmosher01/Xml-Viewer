package nu.mine.mosher.xml.viewer.gui;


import nu.mine.mosher.xml.viewer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.*;

import static nu.mine.mosher.xml.viewer.XmlViewer.prefs;
import static nu.mine.mosher.xml.viewer.gui.XmlViewerGui.ACCEL;


class TreePanel extends JTree {
    private int visualSize = prefZoom();
    private FrameManager framer;
    private JMenuItem itemZoomIn;
    private JMenuItem itemZoomOut;
    private JMenuItem itemExpandAll;
    private JMenuItem itemCollapseAll;
    private boolean expall;

    public TreePanel(final DomTreeModel model, final FrameManager framer) {
        super(model);
        this.framer = framer;
    }

    public void init() {
        updateFont();
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

    public void updateMenu(final boolean haveTree) {
        itemZoomIn.setEnabled(haveTree);
        itemZoomOut.setEnabled(haveTree);
        itemExpandAll.setEnabled(haveTree);
        itemCollapseAll.setEnabled(haveTree);
    }

    public void appendViewMenuItems(final JMenu menu) {
        itemZoomIn = new JMenuItem("Zoom in");
        itemZoomIn.setMnemonic(KeyEvent.VK_I);
        itemZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ACCEL));
        itemZoomIn.addActionListener(e -> zoom(+1));
        menu.add(itemZoomIn);

        itemZoomOut = new JMenuItem("Zoom out");
        itemZoomOut.setMnemonic(KeyEvent.VK_O);
        itemZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ACCEL));
        itemZoomOut.addActionListener(e -> zoom(-1));
        menu.add(itemZoomOut);

        menu.addSeparator();

        itemExpandAll = new JMenuItem("Expand all");
        itemExpandAll.setMnemonic(KeyEvent.VK_X);
        itemExpandAll.addActionListener(e -> {
            final ExpandTask task = new ExpandTask();
            canceller(task, this.framer.frame());
            task.execute();
        });
        menu.add(itemExpandAll);

        itemCollapseAll = new JMenuItem("Collapse all");
        itemCollapseAll.setMnemonic(KeyEvent.VK_C);
        itemCollapseAll.addActionListener(e -> collapseAll());
        menu.add(itemCollapseAll);
    }

    private void updateFont() {
        setFont(new Font(Font.DIALOG, Font.PLAIN, Math.round(1.0f * visualSize)));
    }

    private void zoom(final int i) {
        zoomContrained(4, Math.round(i * (this.visualSize / 8.0f + 1.0f)), 64);
        updateFont();
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

    private void expandAll(final JTree tree, final boolean expand) {
        final Object root = tree.getModel().getRoot();
        if (Objects.nonNull(root)) {
            final TreeUI treeUI = tree.getUI();
            try {
                tree.setUI(null);
                expandAll(tree, new TreePath(root), expand);
            } finally {
                final Logger log = LoggerFactory.getLogger(this.getClass());
                final long begin = System.currentTimeMillis();
                // this is actually the time-consuming step
                tree.setUI(treeUI);
                log.info("getExpandedDescendants complete: "+(System.currentTimeMillis()-begin)+" ms");
            }
        }
    }

    private static boolean expandAll(final JTree tree, final TreePath parent, final boolean expand) {
        boolean childExpandCalled = false;

        final DomTreeNode node = (DomTreeNode)parent.getLastPathComponent();
        final int n = node.getChildCount();
        for (int i = 0; i < n; ++i) {
            final TreePath path = parent.pathByAddingChild(node.getChild(i));
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

    // override this to improve its efficiency
    @Override
    public Enumeration<TreePath> getExpandedDescendants(final TreePath path) {
        final ArrayList<TreePath> paths = new ArrayList<>(4096);
        getOpenedChild(path, paths);
        return Collections.enumeration(paths);
    }

    private void getOpenedChild(final TreePath path, final Collection<TreePath> paths) {
        final Object parent = path.getLastPathComponent();
        final TreeModel model = getModel();
        final int n = model.getChildCount(parent);
        for (int i = 0; i < n; ++i) {
            final Object child = model.getChild(parent, i);
            final TreePath childPath = path.pathByAddingChild(child);
            if (!model.isLeaf(child) && isExpanded(childPath)) {
                paths.add(childPath);
                getOpenedChild(childPath, paths);
            }
        }
    }








    private class ExpandTask extends SwingWorker<Void, Void> {
        private JDialog dialog;

        @Override
        protected Void doInBackground() {
            expandAll();
            return null;
        }

        @Override
        protected void done() {
            super.done();
            this.dialog.setVisible(false);
            this.dialog.dispose();
        }

        public void setCanceller(final JDialog dialog) {
            this.dialog = dialog;
        }
    }



    private void canceller(final ExpandTask task, final JFrame frame) {
        final JDialog dialog = new JDialog(frame, "Wait");
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final JLabel label = new JLabel("Please wait...");
        label.setHorizontalAlignment(JLabel.CENTER);

        final JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
        closePanel.add(Box.createHorizontalGlue());
        closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));

        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(label, BorderLayout.CENTER);
        contentPane.add(closePanel, BorderLayout.PAGE_END);
        contentPane.setOpaque(true);
        dialog.setContentPane(contentPane);

        dialog.setSize(new Dimension(300, 150));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        task.setCanceller(dialog);
    }
}
