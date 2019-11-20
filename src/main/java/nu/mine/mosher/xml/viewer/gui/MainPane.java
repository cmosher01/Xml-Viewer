/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.gui;



import nu.mine.mosher.xml.viewer.model.DomTreeModel;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Dimension;
import java.awt.GridLayout;



class MainPane extends JPanel {
    public MainPane(final DomTreeModel model) {
        super(new GridLayout(1, 1), true);

        setOpaque(true);
        addNotify();

        this.tree = createTreeControl(model);
        final JScrollPane scrollpane = createScrollPane(this.tree);

        add(scrollpane);
    }

    public void appendViewMenuItems(JMenu menuView) {
        this.tree.appendViewMenuItems(menuView);
    }

    private TreePanel tree;

    private static TreePanel createTreeControl(final DomTreeModel model) {
        final TreePanel jtree = new TreePanel(model);

        jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jtree.setShowsRootHandles(true);
        jtree.setRootVisible(false);

        return jtree;
    }

    private static JScrollPane createScrollPane(final JTree jtree) {
        final JScrollPane scrollpane = new JScrollPane(jtree);

        scrollpane.setPreferredSize(new Dimension(640, 480));

        return scrollpane;
    }
}
