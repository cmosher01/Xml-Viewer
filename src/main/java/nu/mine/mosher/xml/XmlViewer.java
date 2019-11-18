package nu.mine.mosher.xml;

import javafx.application.*;
import javafx.beans.binding.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.owasp.encoder.Encode;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;


public class XmlViewer extends Application {
    private ReadOnlyObjectWrapper<Optional<Node>> currentNode = new ReadOnlyObjectWrapper<>(Optional.empty());

    public static void main(final String... args) {
        launch(args);
    }

    private static URL asUrl(final String pathOrUrl) throws IOException {
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

    @Override
    public void start(final Stage stage) throws IOException, ParserConfigurationException, SAXException {
        Platform.runLater(() -> {
            final List<String> p = getParameters().getUnnamed();

            if (p.contains("--help")) {
                System.out.println("usage: xml-viewer [input.xml [schema.xsd [...]]]");
                System.out.flush();
                Platform.exit();
                return;
            }


            try {
                final Set<URL> schemata = new HashSet<>();
                URL xml = null;
                if (0 < p.size()) {
                    xml = asUrl(p.get(0));
                    for (int i = 1; i < p.size(); ++i) {
                        schemata.add(asUrl(p.get(i)));
                    }
                }

                if (Objects.isNull(xml)) {
                    // TODO: no args provided, so ask user for xml and (optionally) schema files
                    System.out.println("usage: xml-viewer [input.xml [schema.xsd [...]]]");
                    System.out.flush();
                    Platform.exit();
                    return;
                }


                final TreeView<Node> treeView = createTree(xml, schemata);

                setUserAgentStylesheet(STYLESHEET_MODENA);
                SplitPane.setResizableWithParent(treeView, Boolean.FALSE);
                final ScrollPane propsView = new ScrollPane(createProps());
                propsView.setFitToWidth(true);
                final SplitPane splitPane = new SplitPane(treeView, propsView);

                final Scene scene = new Scene(splitPane, 1024, 1024);
                scene.getStylesheets().add("stylesheet.css");
                stage.setScene(scene);

                stage.show();
            } catch (final Throwable e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static class NodeProp {
        final String label;
        final Class<?> nodeClass;
        final Function<Node, String> displayFn;
        NodeProp(String label, Class<?> nodeClass, Function<Node, String> displayFn) {
            this.label = label;
            this.nodeClass = nodeClass;
            this.displayFn = displayFn;
        }
    }

    private List<NodeProp> prpdefs = new ArrayList<>();
    {
        prpdefs.add(new NodeProp("node type", Node.class, node -> nameFromTypeId(node.getNodeType())));
        prpdefs.add(new NodeProp("ns URI", Node.class, Node::getNamespaceURI));
        prpdefs.add(new NodeProp("ns prefix", Node.class, Node::getPrefix));
        prpdefs.add(new NodeProp("local name", Node.class, Node::getLocalName));
        prpdefs.add(new NodeProp("tag name", Element.class, node -> ((Element)node).getTagName()));
        prpdefs.add(new NodeProp("node name", Node.class, Node::getNodeName));
        prpdefs.add(new NodeProp("base URI", Node.class, Node::getBaseURI));
        prpdefs.add(new NodeProp("data type", Element.class, node -> ((Element)node).getSchemaTypeInfo().getTypeNamespace()+":"+((Element)node).getSchemaTypeInfo().getTypeName()));
        prpdefs.add(new NodeProp("node value", Node.class, node -> filter(node.getNodeValue())));
    }

    private GridPane createProps() {
        final GridPane props = new GridPane();
        props.setPadding(new Insets(40));
        props.setHgap(3);
        props.setVgap(10);

        ColumnConstraints colcons0 = new ColumnConstraints();
        colcons0.setHgrow(Priority.NEVER);
        props.getColumnConstraints().add(colcons0);
        ColumnConstraints colcons1 = new ColumnConstraints();
        colcons1.setHgrow(Priority.ALWAYS);
        props.getColumnConstraints().add(colcons1);

        final int LABEL_COL = 0;
        final int VALUE_COL = 1;
        int row = 0;
        for (final NodeProp prop : prpdefs) {
            final javafx.scene.text.Text labelLabel = new javafx.scene.text.Text(prop.label + ":");
            props.add(labelLabel, LABEL_COL, row);
            GridPane.setValignment(labelLabel, VPos.TOP);
            GridPane.setHalignment(labelLabel, HPos.LEFT);

            final Label labelValue = new Label();
            labelValue.setWrapText(true);
            labelValue.textProperty().bind(Bindings.createStringBinding(wrapNodeFn(prop), currentNode));
            props.add(labelValue, VALUE_COL, row);
            GridPane.setValignment(labelValue, VPos.TOP);
            GridPane.setHalignment(labelValue, HPos.LEFT);

            ++row;
        }



        return props;
    }

    private Callable<String> wrapNodeFn(final NodeProp prop) {
        return () -> {
            final Optional<Node> node = currentNode.get();
            if (!node.isPresent()) {
                return "";
            }
            final short nType = node.get().getNodeType();
            final NodeInfo nodeInfo = nodeInfos.get(nType);
            if (prop.nodeClass.isAssignableFrom(nodeInfo.typeClass)) {
                return prop.displayFn.apply(node.get());
            }
            return "";
        };
    }

    private static class DomNodeCell extends TreeCell<Node> {
        @Override
        protected void updateItem(final Node item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || Objects.isNull(item)) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }

            setText(filter(item.getNodeName()));
            setStyle("-fx-background-color: " + backColor(item.getNodeType()) + ";");
        }
    }

    private TreeView<Node> createTree(final URL xml, final Set<URL> schemata) throws IOException, SAXException, ParserConfigurationException {
        final TreeView<Node> tree = new TreeView<>(createTreeItems(xml, schemata));

        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.isNull(newValue) || Objects.isNull(newValue.getValue())) {
                this.currentNode.set(Optional.empty());
            } else {
                this.currentNode.set(Optional.ofNullable(newValue.getValue()));
            }
        });

        tree.getSelectionModel().clearSelection();

        tree.setCellFactory(treeView -> new DomNodeCell());

        return tree;
    }

    private static class NodeInfo {
        final Class<?> typeClass;
        final String typeName;

        private NodeInfo(Class<?> typeClass, String typeName) {
            this.typeClass = typeClass;
            this.typeName = typeName;
        }
    }

    private static final Map<Short, NodeInfo> nodeInfos = new HashMap<>();
    static {
        nodeInfos.put(Node.ELEMENT_NODE, new NodeInfo(Element.class, "element"));
        nodeInfos.put(Node.ATTRIBUTE_NODE, new NodeInfo(Attr.class, "attribute"));
        nodeInfos.put(Node.TEXT_NODE, new NodeInfo(Text.class, "text node"));
        nodeInfos.put(Node.CDATA_SECTION_NODE, new NodeInfo(CDATASection.class, "cdata section"));
        nodeInfos.put(Node.ENTITY_REFERENCE_NODE, new NodeInfo(EntityReference.class, "entity reference"));
        nodeInfos.put(Node.ENTITY_NODE, new NodeInfo(Entity.class, "entity"));
        nodeInfos.put(Node.PROCESSING_INSTRUCTION_NODE, new NodeInfo(ProcessingInstruction.class, "processing instruction"));
        nodeInfos.put(Node.COMMENT_NODE, new NodeInfo(Comment.class, "comment"));
        nodeInfos.put(Node.DOCUMENT_NODE, new NodeInfo(Document.class, "document"));
        nodeInfos.put(Node.DOCUMENT_TYPE_NODE, new NodeInfo(DocumentType.class, "doctype"));
        nodeInfos.put(Node.DOCUMENT_FRAGMENT_NODE, new NodeInfo(DocumentFragment.class, "fragment"));
        nodeInfos.put(Node.NOTATION_NODE, new NodeInfo(Notation.class, "notation"));
    }

    private String nameFromTypeId(final short nodeType) {
        if (!nodeInfos.containsKey(nodeType)) {
            return "node";
        }
        return nodeInfos.get(nodeType).typeName;
    }

    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private TreeItem<Node> createTreeItems(final URL xml, final Set<URL> schemata) throws ParserConfigurationException, IOException, SAXException {
        final boolean schema = (0 < schemata.size());

        final DocumentBuilderFactory factoryDocBuild = DocumentBuilderFactory.newInstance();
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
        factoryDocBuild.setNamespaceAware(true);
        factoryDocBuild.setValidating(schema);
        factoryDocBuild.setExpandEntityReferences(false);
        factoryDocBuild.setCoalescing(false);
        factoryDocBuild.setIgnoringElementContentWhitespace(false);
        factoryDocBuild.setIgnoringComments(false);
        factoryDocBuild.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        factoryDocBuild.setAttribute(JAXP_SCHEMA_SOURCE, schemata.stream().map(URL::toExternalForm).toArray());

        final DocumentBuilder builder = factoryDocBuild.newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler(new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.err), StandardCharsets.UTF_8), true)));

        final Document doc = builder.parse(xml.toExternalForm());

        final TreeItem<Node> root = new TreeItem<>(doc);
        addChildrenItem(root);

        return root;
    }

    private void addChildrenItem(final TreeItem<Node> root) {
        final Node node = root.getValue();

        if (node.hasAttributes()) {
            final NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                final Node attr = attrs.item(i);
                final TreeItem<Node> treeItem = new TreeItem<>(attr);
                root.getChildren().add(treeItem);

                addChildrenItem(treeItem);
            }
        }

        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            final TreeItem<Node> treeItem = new TreeItem<>(child);
            root.getChildren().add(treeItem);

            addChildrenItem(treeItem);
        }
    }

    private static String backColor(short nodeType) {
        switch (nodeType) {
            case Node.ELEMENT_NODE:
                return "lightgreen";
            case Node.ATTRIBUTE_NODE:
                return "lightyellow";
            case Node.TEXT_NODE:
                return "lightblue";
        }
        return "lightgray";
    }

    private static String filter(String s) {
        if (Objects.isNull(s)) {
            return "<null>";
        }
        return Encode.forXml(s)
            .replace(" ", ".")
            .replace("\t", "&x09;")
            .replace("\n", "&x0a;")
            .replace("\r", "&x0d");
    }


    private static class MyErrorHandler implements ErrorHandler {
        private final PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        public void warning(SAXParseException spe) {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException("Error: " + getParseExceptionInfo(spe));
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            throw new SAXException("Fatal Error: " + getParseExceptionInfo(spe));
        }

        private static String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "<null>";
            }
            return "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
        }
    }
}
