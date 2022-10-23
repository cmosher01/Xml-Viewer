package nu.mine.mosher.xml.viewer;

import ch.qos.logback.classic.*;
import nu.mine.mosher.xml.viewer.gui.XmlViewerGui;
import nu.mine.mosher.xml.viewer.util.*;
import org.slf4j.Logger;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.prefs.Preferences;

public class XmlViewer {
    private static Logger LOG;

    public static class LogConfig extends LogbackConfigurator {
    }

    public static Preferences prefs() {
        return Preferences.userNodeForPackage(XmlViewer.class);
    }

    public static void main(final String... args) {
        try {
            initLogging();
            LOG.info("version: {}", Version.version(XmlViewer.class.getPackage()));
            XmlViewerGui.create();
        } catch (final Throwable e) {
            logProgramTermination(e);
        } finally {
            if (Objects.nonNull(LOG)) {
                LOG.info("program exiting");
            }
            System.out.flush();
            System.err.flush();
        }
    }

    private static void initLogging() {
        LogConfig.testSubsystem();
        LOG = LoggerFactory.getLogger(XmlViewer.class);

        final LoggerContext ctx = (LoggerContext)LoggerFactory.getILoggerFactory();
        ctx.getLogger("sun.awt.X11.wrappers").setLevel(Level.WARN);
    }

    private static void logProgramTermination(final Throwable e) {
        Objects.requireNonNull(e);
        if (Objects.nonNull(LOG)) {
            LOG.error("Program terminating due to error:", e);
        } else {
            try {
                final Path pathTemp = Files.createTempFile(XmlViewer.class.getName()+"-", ".log");
                e.printStackTrace(new PrintStream(new FileOutputStream(pathTemp.toFile()), true));
            } catch (final Throwable reallyBad) {
                e.printStackTrace();
                reallyBad.printStackTrace();
            }
        }
    }
}
