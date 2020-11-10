import nu.mine.mosher.xml.viewer.XmlViewer;

module nu.mine.mosher.xml.viewer {
    exports nu.mine.mosher.xml.viewer;
    exports nu.mine.mosher.xml.viewer.util;
    provides ch.qos.logback.classic.spi.Configurator with XmlViewer.LogConfig;
    requires log.files;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.apache.commons.logging;
    requires log4j;
    requires jul.to.slf4j;
    requires java.logging;
    requires java.xml;
    requires java.desktop;
    requires java.prefs;
}
