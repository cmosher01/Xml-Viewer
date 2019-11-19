package nu.mine.mosher.xml.viewer;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

public class XmlViewerGui {
    public static void gui(XmlViewerOptions opts) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> new XmlViewerGui().run());
    }

    private void run() {

    }
}
