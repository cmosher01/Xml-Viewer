package nu.mine.mosher.xml.viewer;

import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.IOException;
import java.net.*;
import java.nio.file.Paths;
import java.util.Set;

public class FileHandler {
    public static URL asUrl(final String pathOrUrl) throws IOException {
        Throwable urlExcept ;
        try {
            return new URI(pathOrUrl).toURL();
        } catch (final Throwable e) {
            urlExcept = e;
        }

        Throwable pathExcept ;
        try {
            return Paths.get(pathOrUrl).toUri().toURL();
        } catch (final Throwable e) {
            pathExcept = e;
        }

        final IOException except = new IOException("Invalid path or URL: "+pathOrUrl);
        except.addSuppressed(pathExcept);
        except.addSuppressed(urlExcept);
        throw except;
    }

    public static Document asDom(final URL xml, final Set<URL> schemata) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder builder = documentBuilderFactory(schemata).newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler());

        return builder.parse(xml.toExternalForm());
    }

    private static DocumentBuilderFactory documentBuilderFactory(Set<URL> schemata) throws ParserConfigurationException {
        final boolean schema = (0 < schemata.size());

        final DocumentBuilderFactory factoryDocBuild = DocumentBuilderFactory.newInstance();

        factoryDocBuild.setNamespaceAware(true);
        factoryDocBuild.setValidating(schema);
        factoryDocBuild.setExpandEntityReferences(false);
        factoryDocBuild.setCoalescing(false);
        factoryDocBuild.setIgnoringElementContentWhitespace(false);
        factoryDocBuild.setIgnoringComments(false);

        factoryDocBuild.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/standard-uri-conformant", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/xinclude", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validate-annotations", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/schema", schema);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
//        factoryDocBuild.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true); // crashes
        factoryDocBuild.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        factoryDocBuild.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        factoryDocBuild.setAttribute(JAXP_SCHEMA_SOURCE, schemata.stream().map(URL::toExternalForm).toArray());

        return factoryDocBuild;
    }

    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private static class MyErrorHandler implements ErrorHandler {
        public void warning(SAXParseException spe) throws SAXException {
            throw new SAXException("WARN " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException("ERROR " + getParseExceptionInfo(spe));
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            throw new SAXException("FATAL " + getParseExceptionInfo(spe));
        }

        private static String getParseExceptionInfo(SAXParseException spe) {
            return String.format("uri: %s, line: %d, msg: %s", spe.getSystemId(), spe.getLineNumber(), spe.getMessage());
        }
    }
}
