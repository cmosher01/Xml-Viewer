package nu.mine.mosher.xml.viewer.model;



import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static nu.mine.mosher.xml.viewer.Util.filter;



/**
 * Wrapper for a DOM Node, to give it a proper interface for use by a JTree.
 */
public class DomTreeNode {
    private final Node node;
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
        return filter(this.node.toString());
    }
}
