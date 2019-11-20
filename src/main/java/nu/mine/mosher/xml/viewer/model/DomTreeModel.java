/*
 * Created on Nov 22, 2005
 */
package nu.mine.mosher.xml.viewer.model;


import org.w3c.dom.Document;

import javax.swing.event.*;
import javax.swing.tree.*;
import java.io.Closeable;
import java.util.*;


public class DomTreeModel extends Observable implements TreeModel, Closeable {
    private Document tree;
    private List<TreeModelListener> rListener = new ArrayList<>();


//    public void open(final BufferedInputStream in) throws InvalidLevel, IOException {
//        final GedcomTree gt = Gedcom.readFile(in);
//        setTree(gt);
//    }

    public void close() {
        setTree(null);
    }


    public Object getRoot() {
//        if (this.tree == null) {
//            return null;
//        }

        return "Testing";
    }

    public Object getChild(final Object parent, final int index) {
        if (parent == null) {
            return null;
        } else {
            return getRoot();
        }

//        final TreeNode<GedcomLine> nodeParent = (TreeNode<GedcomLine>) parent;
//
//        int i = 0;
//        for (final TreeNode<GedcomLine> child : nodeParent) {
//            if (i++ == index) {
//                return child;
//            }
//        }
//        return null;
    }

    public int getChildCount(final Object parent) {
//        if (parent == null) {
        return 0;
//        }
//
//        final TreeNode<GedcomLine> nodeParent = (TreeNode<GedcomLine>) parent;
//        return nodeParent.getChildCount();
    }

    public boolean isLeaf(final Object node) {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(final TreePath path, final Object newValue) {
        throw new UnsupportedOperationException();
    }

    public int getIndexOfChild(final Object parent, final Object child) {
//        if (parent == null || child == null) {
            return 0;
//        }
//
//        final TreeNode<GedcomLine> nodeParent = (TreeNode<GedcomLine>) parent;
//
//        int i = 0;
//        for (final TreeNode<GedcomLine> c : nodeParent) {
//            if (c == child) {
//                return i;
//            }
//            ++i;
//        }
//        return -1;
    }


    public void addTreeModelListener(final TreeModelListener listener) {
        this.rListener.add(listener);
    }

    public void removeTreeModelListener(final TreeModelListener listener) {
        this.rListener.remove(listener);
    }


    private void setTree(final Document tree) {
        this.tree = tree;

        fireTreeStructureChanged();
        setChanged();

        notifyObservers();
    }

    private void fireTreeStructureChanged() {
        final TreeModelEvent eventRootChanged = new TreeModelEvent(this, getRootPath());

        for (final TreeModelListener listener : this.rListener) {
            listener.treeStructureChanged(eventRootChanged);
        }
    }

    private Object[] getRootPath() {
        final Object root;
        if (this.tree == null) {
            root = "";
        } else {
            root = this.tree;
        }

        return new Object[]{root};
    }
}
