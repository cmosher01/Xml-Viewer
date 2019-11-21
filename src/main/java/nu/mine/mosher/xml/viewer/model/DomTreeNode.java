package nu.mine.mosher.xml.viewer.model;



import org.w3c.dom.Node;

import java.util.Objects;

import static nu.mine.mosher.xml.viewer.Util.filter;



public class DomTreeNode {
    private final Node node;

    public DomTreeNode(final Node node) {
        this.node = node;
    }

    public int getChildCount() {
        return this.node.getChildNodes().getLength();
    }

    public boolean isLeaf() {
        return !this.node.hasChildNodes();
    }

    public DomTreeNode getChild(final int index) {
        int i = 0;
        for (Node c = this.node.getFirstChild(); Objects.nonNull(c); c = c.getNextSibling()) {
            if (i++ == index) {
                return new DomTreeNode(c);
            }
        }
        return null;
    }

    public int getIndexOfChild(final DomTreeNode child) {
        int i = 0;
        for (Node c = this.node.getFirstChild(); Objects.nonNull(c); c = c.getNextSibling()) {
            if (c == child.node) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Override
    public String toString() {
        return filter(this.node.toString());
    }
}
