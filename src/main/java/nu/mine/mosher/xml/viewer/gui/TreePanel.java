package nu.mine.mosher.xml.viewer.gui;



import javax.swing.JTree;

import static nu.mine.mosher.xml.viewer.XmlViewer.prefs;



public class TreePanel extends JTree {
    private int visualSize = prefZoom();

    private static int prefZoom() {
        return prefs().getInt("zoom", 10);
    }

    private static void prefZoom(int zoom) {
        prefs().putInt("zoom", zoom);
    }

    private void zoom(final int i) {
        zoomContrained(4, i*(this.visualSize/16+1), 64);
        this.framer.updateUi();
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
