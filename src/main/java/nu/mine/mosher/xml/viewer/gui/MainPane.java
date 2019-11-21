/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.gui;



import nu.mine.mosher.xml.viewer.model.DomTreeModel;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;



class MainPane extends JPanel {
    private TreePanel tree;
    private PropertiesPanel properties;

    public MainPane(final DomTreeModel model) {
        super(new BorderLayout(), true);

        setOpaque(true);
        addNotify();

        this.tree = createTreeControl(model);
        final JScrollPane scrlTree = createScrollPane(this.tree);
        scrlTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrlTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this.properties = new PropertiesPanel();
        final JScrollPane scrlProp = createScrollPane(this.properties);
        scrlProp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrlProp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrlTree, scrlProp);
        add(splitPane);
    }

    public void appendViewMenuItems(JMenu menuView) {
        this.tree.appendViewMenuItems(menuView);
    }

    private static TreePanel createTreeControl(final DomTreeModel model) {
        final TreePanel jtree = new TreePanel(model);
        jtree.init();
        return jtree;
    }

    private static JScrollPane createScrollPane(final JComponent c) {
        final JScrollPane scrollpane = new JScrollPane(c);

        scrollpane.setPreferredSize(new Dimension(320, 480));

        return scrollpane;
    }
}
