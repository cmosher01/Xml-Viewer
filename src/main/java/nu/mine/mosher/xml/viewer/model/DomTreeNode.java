package nu.mine.mosher.xml.viewer.model;


import nu.mine.mosher.xml.viewer.file.DomUtil;
import org.w3c.dom.*;

import java.util.*;

import static nu.mine.mosher.xml.viewer.StringUnicodeEncoderDecoder.filter;
import static org.w3c.dom.Node.*;


/**
 * Wrapper for a DOM Node, to give it a proper interface for use by a JTree.
 */
public class DomTreeNode {
    public final Node node;
    private final int cChildren;
    private final Map<DomTreeNode, Integer> childToIndex = new HashMap<>();
    private final Map<Integer, DomTreeNode> indexToChild = new HashMap<>();

    public DomTreeNode(final Node node) {
        this.node = node;

        int indexOfNewChild = 0;

        if (this.node.hasAttributes()) {
            final NamedNodeMap attrs = this.node.getAttributes();
            for (int a = 0; a < attrs.getLength(); ++a) {
                final Node attr = attrs.item(a);
                DomTreeNode child = new DomTreeNode(attr);
                this.indexToChild.put(indexOfNewChild, child);
                this.childToIndex.put(child, indexOfNewChild);
                ++indexOfNewChild;
            }
        }

        if (this.node.getNodeType() == DOCUMENT_TYPE_NODE) {
            final DocumentType doctype = (DocumentType)this.node;
            final NamedNodeMap entities = doctype.getEntities();
            for (int a = 0; a < entities.getLength(); ++a) {
                final Node attr = entities.item(a);
                DomTreeNode child = new DomTreeNode(attr);
                this.indexToChild.put(indexOfNewChild, child);
                this.childToIndex.put(child, indexOfNewChild);
                ++indexOfNewChild;
            }
            final NamedNodeMap notes = doctype.getNotations();
            for (int a = 0; a < notes.getLength(); ++a) {
                final Node attr = notes.item(a);
                DomTreeNode child = new DomTreeNode(attr);
                this.indexToChild.put(indexOfNewChild, child);
                this.childToIndex.put(child, indexOfNewChild);
                ++indexOfNewChild;
            }
        }

        for (Node c = this.node.getFirstChild(); Objects.nonNull(c); c = c.getNextSibling()) {
            DomTreeNode child = new DomTreeNode(c);
            this.indexToChild.put(indexOfNewChild, child);
            this.childToIndex.put(child, indexOfNewChild);
            ++indexOfNewChild;
        }

        this.cChildren = indexOfNewChild;
    }

    public int getChildCount() {
        return this.cChildren;
    }

    public boolean isLeaf() {
        return this.cChildren == 0;
    }

    public DomTreeNode getChild(final int index) {
        return this.indexToChild.getOrDefault(index, null);
    }

    public int getIndexOfChild(final DomTreeNode child) {
        return childToIndex.getOrDefault(child, -1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");

        String t = DomUtil.nameFromTypeId(this.node.getNodeType()).substring(0,1).toUpperCase();
        if (this.node.getNodeType() == TEXT_NODE) {
            final Text n = (Text)this.node;
            if (n.isElementContentWhitespace()) {
                t = "W";
            }
        }
        sb.append("<span style='color:gray'>");
        sb.append(t);
        sb.append("</span>");

        sb.append(" ");
        sb.append("<span style='color:");
        if (t.equalsIgnoreCase("w")) {
            sb.append("silver");
        } else if (this.node.getNodeType() == ELEMENT_NODE) {
            sb.append("orange");
        } else if (this.node.getNodeType() == ATTRIBUTE_NODE) {
            final Attr attr = (Attr)this.node;
            final String ns = attr.getNamespaceURI();
            if (Objects.nonNull(ns) && ns.equals("http://www.w3.org/2000/xmlns/")) {
                sb.append("gray");
            } else {
                sb.append("blue");
            }
        }
        sb.append("'>");
        if (this.node.getNodeType() == ELEMENT_NODE || this.node.getNodeType() == ATTRIBUTE_NODE) {
            sb.append("<strong>");
        }
        sb.append(this.node.getNodeName());
        if (this.node.getNodeType() == ELEMENT_NODE || this.node.getNodeType() == ATTRIBUTE_NODE) {
            sb.append("</strong>");
        }
        sb.append("</span>");

        String val = this.node.getNodeValue();
        if (Objects.nonNull(val) && this.node.getNodeType() != ATTRIBUTE_NODE) {
            val = val.trim();
            if (!val.isEmpty()) {
                val = filter(val);
                sb.append(" (");
                sb.append("<span style='color:green'>");
                if (val.length() < 32) {
                    sb.append(val);
                    sb.append("</span>");
                } else {
                    sb.append(val, 0, 32);
                    sb.append("</pre></span>");
                    sb.append("\u2026");
                }
                sb.append(")");
            }
        }

        sb.append("</html>");
        return sb.toString();
    }
}
