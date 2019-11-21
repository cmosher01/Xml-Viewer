package nu.mine.mosher.xml.viewer.gui;



import nu.mine.mosher.xml.viewer.model.DomTreeModel;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;

import java.awt.event.KeyEvent;

import static nu.mine.mosher.xml.viewer.XmlViewer.prefs;
import static nu.mine.mosher.xml.viewer.gui.XmlViewerGui.ACCEL;


public class TreePanel extends JTree {
    public TreePanel(DomTreeModel model) {
        super(model);
    }

    public void init() {
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setShowsRootHandles(true);
        setRootVisible(true);
    }

    private int visualSize = prefZoom();

    private static int prefZoom() {
        return prefs().getInt("zoom", 10);
    }

    private static void prefZoom(int zoom) {
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
    }

    private void zoom(final int i) {
        zoomContrained(4, i*(this.visualSize/16+1), 64);
        System.out.println("font size: "+this.visualSize);
        setFont(getFont().deriveFont(1.0f*visualSize));
    }

    private void zoomContrained(final int min, final int delta, final int max) {
        final int v = this.visualSize+delta;
        if (v < min) {
            this.visualSize = min;
        } else if (max < v) {
            this.visualSize = max;
        } else {
            this.visualSize = v;
        }
        prefZoom(this.visualSize);
    }

}
