package nu.mine.mosher.xml.viewer;

import org.w3c.dom.*;

import java.util.*;
import java.util.function.Function;

import static nu.mine.mosher.xml.viewer.Util.filter;

public class DomUtil {
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

    public static void dump(final Node node, final int indent) {
        final String tab = String.join("", Collections.nCopies(indent, "   "));
        System.out.printf("%s%02d %s=%s\n", tab, node.getNodeType(), node.getNodeName(), filter(node.getNodeValue()));
        for(Node c = node.getFirstChild(); Objects.nonNull(c); c = c.getNextSibling()) {
            dump(c, indent+1);
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
}
