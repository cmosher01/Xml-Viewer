package nu.mine.mosher.xml.viewer;

import nu.mine.mosher.gnopt.Gnopt;
import nu.mine.mosher.io.LogFiles;
import nu.mine.mosher.xml.viewer.file.DomUtil;
import nu.mine.mosher.xml.viewer.file.FileUtil;
import nu.mine.mosher.xml.viewer.gui.XmlViewerGui;
import org.slf4j.*;
import org.w3c.dom.*;
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
        int exit = 1;
        try {
            run(args);
            exit = 0;
        } catch (final Throwable e) {
            log.error("program terminating", e);
        } finally {
            System.out.flush();
            System.err.flush();
//            System.exit(exit);
        }
    }

    private static void run(final String... args) throws IOException, ParserConfigurationException, SAXException, Gnopt.InvalidOption, InvocationTargetException, InterruptedException {
        final XmlViewerOptions opts = Gnopt.process(XmlViewerOptions.class, args);

        if (opts.help) {
            System.out.println("usage: xml-viewer [input.xml [schema.xsd [...]]]");
        } else if (opts.dump) {
            if (opts.xml.isPresent()) {
                final Document dom = FileUtil.asDom(opts.xml.get(), opts.schemata);
                DomUtil.dump(dom, 0);
            }
        } else {
            XmlViewerGui.create(opts.xml, opts.schemata);
        }
    }

//    private GridPane createProps() {
//        final GridPane props = new GridPane();
//        props.setPadding(new Insets(40));
//        props.setHgap(3);
//        props.setVgap(10);
//
//        ColumnConstraints colcons0 = new ColumnConstraints();
//        colcons0.setHgrow(Priority.NEVER);
//        props.getColumnConstraints().add(colcons0);
//        ColumnConstraints colcons1 = new ColumnConstraints();
//        colcons1.setHgrow(Priority.ALWAYS);
//        props.getColumnConstraints().add(colcons1);
//
//        final int LABEL_COL = 0;
//        final int VALUE_COL = 1;
//        int row = 0;
//        for (final NodeProp prop : prpdefs) {
//            final javafx.scene.text.Text labelLabel = new javafx.scene.text.Text(prop.label + ":");
//            props.add(labelLabel, LABEL_COL, row);
//            GridPane.setValignment(labelLabel, VPos.TOP);
//            GridPane.setHalignment(labelLabel, HPos.LEFT);
//
//            final Label labelValue = new Label();
//            labelValue.setWrapText(true);
//            labelValue.textProperty().bind(Bindings.createStringBinding(wrapNodeFn(prop), currentNode));
//            props.add(labelValue, VALUE_COL, row);
//            GridPane.setValignment(labelValue, VPos.TOP);
//            GridPane.setHalignment(labelValue, HPos.LEFT);
//
//            ++row;
//        }
//
//
//
//        return props;
//    }
//
//    private Callable<String> wrapNodeFn(final NodeProp prop) {
//        return () -> {
//            final Optional<Node> node = currentNode.get();
//            if (!node.isPresent()) {
//                return "";
//            }
//            final short nType = node.get().getNodeType();
//            final NodeInfo nodeInfo = nodeInfos.get(nType);
//            if (prop.nodeClass.isAssignableFrom(nodeInfo.typeClass)) {
//                return prop.displayFn.apply(node.get());
//            }
//            return "";
//        };
//    }
//
//    private static class DomNodeCell extends TreeCell<Node> {
//        @Override
//        protected void updateItem(final Node item, final boolean empty) {
//            super.updateItem(item, empty);
//            if (empty || Objects.isNull(item)) {
//                setText(null);
//                setGraphic(null);
//                setStyle("");
//                return;
//            }
//
//            setText(filter(item.getNodeName()));
//            setStyle("-fx-background-color: " + backColor(item.getNodeType()) + ";");
//        }
//    }
//
//    private TreeView<Node> createTree(final URL xml, final Set<URL> schemata) throws IOException, SAXException, ParserConfigurationException {
//        final TreeView<Node> tree = new TreeView<>(createTreeItems(xml, schemata));
//
//        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (Objects.isNull(newValue) || Objects.isNull(newValue.getValue())) {
//                this.currentNode.set(Optional.empty());
//            } else {
//                this.currentNode.set(Optional.ofNullable(newValue.getValue()));
//            }
//        });
//
//        tree.getSelectionModel().clearSelection();
//
//        tree.setCellFactory(treeView -> new DomNodeCell());
//
//        return tree;
//    }
//
//
//    private static String backColor(short nodeType) {
//        switch (nodeType) {
//            case Node.ELEMENT_NODE:
//                return "lightgreen";
//            case Node.ATTRIBUTE_NODE:
//                return "lightyellow";
//            case Node.TEXT_NODE:
//                return "lightblue";
//        }
//        return "lightgray";
//    }
}
