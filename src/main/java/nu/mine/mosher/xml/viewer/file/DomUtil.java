package nu.mine.mosher.xml.viewer.file;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import java.util.*;
import java.util.function.Function;

import static nu.mine.mosher.xml.viewer.util.StringUnicodeEncoderDecoder.filter;

public class DomUtil {
    public static class NodeProp {
        public final String label;
        public final Class<?> nodeClass;
        public final Function<Node, String> displayFn;

        NodeProp(String label, Class<?> nodeClass, Function<Node, String> displayFn) {
            this.label = label;
            this.nodeClass = nodeClass;
            this.displayFn = displayFn;
        }
    }

    public static void dump(final Node node, final int indent) {
        final String tab = String.join("", Collections.nCopies(indent, "   "));
        System.out.printf("%s%02d %s=%s\n", tab, node.getNodeType(), node.getNodeName(), filter(node.getNodeValue()));
        for (Node c = node.getFirstChild(); Objects.nonNull(c); c = c.getNextSibling()) {
            dump(c, indent + 1);
        }
    }


    public static List<NodeProp> prpdefs = new ArrayList<>();

    static {
        prpdefs.add(new NodeProp("node type", Node.class, node -> nameFromTypeId(node.getNodeType())));

        prpdefs.add(new NodeProp("node name", Node.class, Node::getNodeName));

        prpdefs.add(new NodeProp("base URI", Node.class, Node::getBaseURI));
        prpdefs.add(new NodeProp("ns URI", Node.class, Node::getNamespaceURI));
        prpdefs.add(new NodeProp("ns prefix", Node.class, Node::getPrefix));
        prpdefs.add(new NodeProp("local name", Node.class, Node::getLocalName));



        prpdefs.add(new NodeProp("data type", Element.class, node -> schemaType(((Element)node).getSchemaTypeInfo())));

        prpdefs.add(new NodeProp("data type", Attr.class, node -> schemaType(((Attr)node).getSchemaTypeInfo())));
        prpdefs.add(new NodeProp("spec'd?", Attr.class, node -> Boolean.toString(((Attr)node).getSpecified())));
        prpdefs.add(new NodeProp("ID?", Attr.class, node -> Boolean.toString(((Attr)node).isId())));

        prpdefs.add(new NodeProp("doc URI",Document.class, node -> ((Document)node).getDocumentURI()));
        prpdefs.add(new NodeProp("in enc",Document.class, node -> ((Document)node).getInputEncoding()));
        prpdefs.add(new NodeProp("xml enc",Document.class, node -> ((Document)node).getXmlEncoding()));
        prpdefs.add(new NodeProp("xml vers",Document.class, node -> ((Document)node).getXmlVersion()));
        prpdefs.add(new NodeProp("standalone",Document.class, node -> Boolean.toString(((Document)node).getXmlStandalone())));

        prpdefs.add(new NodeProp("sys ID",DocumentType.class, node -> ((DocumentType)node).getSystemId()));
        prpdefs.add(new NodeProp("pub ID",DocumentType.class, node -> ((DocumentType)node).getPublicId()));
        prpdefs.add(new NodeProp("doctype",DocumentType.class, node -> "\n"+((DocumentType)node).getInternalSubset()));

        prpdefs.add(new NodeProp("sys ID",Notation.class, node -> ((Notation)node).getSystemId()));
        prpdefs.add(new NodeProp("pub ID",Notation.class, node -> ((Notation)node).getPublicId()));

        prpdefs.add(new NodeProp("sys ID",Entity.class, node -> ((Entity)node).getSystemId()));
        prpdefs.add(new NodeProp("pub ID",Entity.class, node -> ((Entity)node).getPublicId()));



        prpdefs.add(new NodeProp("len", CharacterData.class, node -> Integer.toString(((CharacterData)node).getLength())));
        prpdefs.add(new NodeProp("white?", Text.class, node -> Boolean.toString(((Text)node).isElementContentWhitespace())));



        prpdefs.add(new NodeProp("node value", Node.class, Node::getNodeValue));
    }

    private static String schemaType(final TypeInfo schemaType) {
        if (Objects.isNull(schemaType)) {
            return null;
        }
        String ns = schemaType.getTypeNamespace();
        if (Objects.isNull(ns)) {
            ns = "";
        }
        String n = schemaType.getTypeName();
        if (Objects.isNull(n)) {
            n = "";
        }

        if (ns.isEmpty() && n.isEmpty()) {
            return null;
        }

        return String.format("%s:%s", ns, n);
    }

    public static class NodeInfo {
        public final Class<?> typeClass;
        public final String typeName;

        private NodeInfo(Class<?> typeClass, String typeName) {
            this.typeClass = typeClass;
            this.typeName = typeName;
        }
    }

    public static final Map<Short, NodeInfo> nodeInfos = new HashMap<>();

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

    public static String nameFromTypeId(final short nodeType) {
        if (!nodeInfos.containsKey(nodeType)) {
            return "node type "+ nodeType;
        }
        return nodeInfos.get(nodeType).typeName;
    }
}
