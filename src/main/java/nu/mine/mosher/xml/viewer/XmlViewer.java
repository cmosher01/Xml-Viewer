package nu.mine.mosher.xml.viewer;

import nu.mine.mosher.gnopt.Gnopt;
import nu.mine.mosher.io.LogFiles;
import nu.mine.mosher.xml.viewer.file.*;
import nu.mine.mosher.xml.viewer.gui.XmlViewerGui;
import org.slf4j.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.prefs.Preferences;


public class XmlViewer {
    private static final Logger log;

    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", LogFiles.getLogFileOf(XmlViewer.class).getPath());
        System.err.println(System.getProperty("org.slf4j.simpleLogger.logFile"));
        log = LoggerFactory.getLogger(XmlViewer.class);
    }

    public static Preferences prefs() {
        return Preferences.userNodeForPackage(XmlViewer.class);
    }

    public static void main(final String... args) {
        try {
            run(args);
        } catch (final Throwable e) {
            log.error("program terminating", e);
        } finally {
            System.out.flush();
            System.err.flush();
        }
    }

    private static void run(final String... args) throws IOException, ParserConfigurationException, SAXException, Gnopt.InvalidOption, InvocationTargetException, InterruptedException {
        final XmlViewerOptions opts = Gnopt.process(XmlViewerOptions.class, args);

        if (opts.help) {
            System.out.println("usage: xml-viewer [input.xml]");
        } else if (opts.dump) {
            if (opts.xml.isPresent()) {
                final Document dom = FileUtil.asDom(opts.xml.get());
                DomUtil.dump(dom, 0);
            }
        } else {
            XmlViewerGui.create(opts.xml);
        }
    }
}
