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

        String t = DomUtil.nameFromTypeId(this.node.getNodeType()).substring(0,1).toUpperCase();
        if (this.node.getNodeType() == TEXT_NODE) {
            final Text n = (Text)this.node;
            if (n.isElementContentWhitespace()) {
                t = "W";
            }
        }
        sb.append(t);

        sb.append(" ");
        sb.append(this.node.getNodeName());

        String val = this.node.getNodeValue();
        if (Objects.nonNull(val)) {
            val = val.trim();
            if (!val.isEmpty()) {
                sb.append(" (");
                if (val.length() < 32) {
                    sb.append(val);
                } else {
                    sb.append(val, 0, 32);
                    sb.append("\u2026");
                }
                sb.append(")");
            }
        }

        return sb.toString();
    }
}
