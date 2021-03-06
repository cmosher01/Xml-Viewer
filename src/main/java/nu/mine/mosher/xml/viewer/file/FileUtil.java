package nu.mine.mosher.xml.viewer.file;

import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.IOException;
import java.net.URL;



public class FileUtil {
    public static Document asDom(final URL xml, final boolean validate) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder builder = documentBuilderFactory(validate).newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler());

        return builder.parse(xml.toExternalForm());
    }

    private static DocumentBuilderFactory documentBuilderFactory(final boolean validate) throws ParserConfigurationException {
        final DocumentBuilderFactory factoryDocBuild = DocumentBuilderFactory.newInstance();

        factoryDocBuild.setNamespaceAware(true);
        factoryDocBuild.setValidating(validate);
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
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/schema", validate);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
        factoryDocBuild.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
//        factoryDocBuild.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true); // crashes
        factoryDocBuild.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        factoryDocBuild.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

        return factoryDocBuild;
    }

    private static class MyErrorHandler implements ErrorHandler {
        public void warning(SAXParseException spe) throws SAXException {
            throw new SAXException("WARN " + getParseExceptionInfo(spe), spe);
        }

        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException("ERROR " + getParseExceptionInfo(spe), spe);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            throw new SAXException("FATAL " + getParseExceptionInfo(spe), spe);
        }

        private static String getParseExceptionInfo(SAXParseException spe) {
            return String.format("uri: %s, line: %d, msg: %s", spe.getSystemId(), spe.getLineNumber(), spe.getMessage());
        }
    }
}
