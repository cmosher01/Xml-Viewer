/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.model;


import nu.mine.mosher.xml.viewer.file.FileUtil;
import org.xml.sax.SAXException;

import javax.swing.event.*;
import javax.swing.tree.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;


public class DomTreeModel extends Observable implements TreeModel, Closeable {
    private Optional<DomTreeNode> tree = Optional.empty();
    private List<TreeModelListener> rListener = new ArrayList<>();


    public void open(final URL xml, final Set<URL> schemata) throws IOException, SAXException, ParserConfigurationException {
        setTree(Optional.of(new DomTreeNode(FileUtil.asDom(xml, schemata))));
    }

    public void close() {
        setTree(Optional.empty());
    }

    public DomTreeNode getRoot() {
        return this.tree.orElse(null);
    }

    public DomTreeNode getChild(final Object parent, final int index) {
        if (parent == null) {
            return null;
        }

        final DomTreeNode n = (DomTreeNode)parent;
        return n.getChild(index);
    }

    public int getChildCount(final Object parent) {
        if (parent == null) {
            return 0;
        }

        final DomTreeNode n = (DomTreeNode)parent;
        return n.getChildCount();
    }

    public boolean isLeaf(final Object node) {
        if (node == null) {
            return true;
        }

        final DomTreeNode n = (DomTreeNode)node;
        return n.isLeaf();
    }

    public void valueForPathChanged(final TreePath path, final Object newValue) {
        throw new UnsupportedOperationException();
    }

    public int getIndexOfChild(final Object parent, final Object child) {
        if (parent == null || child == null) {
            return -1;
        }

        final DomTreeNode p = (DomTreeNode)parent;
        final DomTreeNode c = (DomTreeNode)child;
        return p.getIndexOfChild(c);
    }

    public void addTreeModelListener(final TreeModelListener listener) {
        this.rListener.add(listener);
    }

    public void removeTreeModelListener(final TreeModelListener listener) {
        this.rListener.remove(listener);
    }

    private void setTree(final Optional<DomTreeNode> tree) {
        this.tree = tree;

        fireTreeStructureChanged();
        setChanged();

        notifyObservers();
    }

    private void fireTreeStructureChanged() {
        for (final TreeModelListener listener : this.rListener) {
            listener.treeStructureChanged(new TreeModelEvent(this, getRootPath()));
        }
    }

    private DomTreeNode[] getRootPath() {
        if (!this.tree.isPresent()) {
            return null;
        }
        return new DomTreeNode[]{getRoot()};
    }
}
