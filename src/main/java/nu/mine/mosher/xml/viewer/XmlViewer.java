package nu.mine.mosher.xml.viewer;

import nu.mine.mosher.io.LogFiles;
import nu.mine.mosher.xml.viewer.gui.XmlViewerGui;
import org.slf4j.*;

import java.util.prefs.Preferences;

public class XmlViewer {
    private static final Logger log;

    static {
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.setProperty("org.slf4j.simpleLogger.logFile", LogFiles.getLogFileOf(XmlViewer.class).getPath());
        System.err.println(System.getProperty("org.slf4j.simpleLogger.logFile"));
        log = LoggerFactory.getLogger(XmlViewer.class);
    }

    public static Preferences prefs() {
        return Preferences.userNodeForPackage(XmlViewer.class);
    }

    public static void main(final String... args) {
        try {
            XmlViewerGui.create();
        } catch (final Throwable e) {
            log.error("program terminating", e);
        } finally {
            log.info("program exiting");
            System.out.flush();
            System.err.flush();
        }
    }
}
