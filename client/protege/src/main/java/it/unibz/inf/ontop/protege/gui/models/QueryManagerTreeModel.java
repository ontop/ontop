package it.unibz.inf.ontop.protege.gui.models;

import it.unibz.inf.ontop.protege.core.QueryManager;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class QueryManagerTreeModel implements TreeModel, QueryManager.EventListener {
    private final QueryManager queryManager;

    public QueryManagerTreeModel(QueryManager queryManager) {
        this.queryManager = queryManager;
        queryManager.addListener(this);
    }

    @Override
    public Object getRoot() {
        return queryManager.getRoot();
    }

    @Override
    public Object getChild(Object parentO, int index) {
        QueryManager.Item parent = (QueryManager.Item)parentO;
        return index < parent.getChildNumber() ? parent.getChild(index) :  null;
    }

    @Override
    public int getChildCount(Object parentO) {
        QueryManager.Item parent = (QueryManager.Item)parentO;
        return parent.getChildNumber();
    }

    @Override
    public boolean isLeaf(Object node) {
        QueryManager.Item item = (QueryManager.Item)node;
        return item.getChildNumber() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
// TODO:
    }

    @Override
    public int getIndexOfChild(Object parentO, Object childO) {
        QueryManager.Item parent = (QueryManager.Item)parentO;
        QueryManager.Item child = (QueryManager.Item)childO;
        return parent.getChildIndex(child);
    }

    private final ArrayList<TreeModelListener> listeners = new ArrayList<>();

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (l != null && !listeners.contains(l))
            listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void added(QueryManager.Item item, int indexInParent) {
        listeners.forEach(l -> l.treeNodesInserted(createEvent(item, indexInParent)));
    }

    @Override
    public void removed(QueryManager.Item item, int indexInParent) {
        listeners.forEach(l -> l.treeNodesRemoved(createEvent(item, indexInParent)));
    }

    @Override
    public void changed(QueryManager.Item item, int indexInParent) {
        listeners.forEach(l -> l.treeNodesChanged(createEvent(item, indexInParent)));
    }

    private TreeModelEvent createEvent(QueryManager.Item item, int indexInParent) {
        Object parent = item.getParent();
        return new TreeModelEvent(
                this,
                getPathToRoot(parent),
                new int[]{ indexInParent }, new Object[]{ item });
    }

    private TreePath getPathToRoot(Object node) {
        List<QueryManager.Item> reversedPath = new ArrayList<>();
        QueryManager.Item current = (QueryManager.Item)node;
        while (current != null) {
            reversedPath.add(current);
            current = current.getParent();
        }
        Object[] path = new Object[reversedPath.size()];
        for (int i = path.length - 1; i >= 0; i--)
            path[i] = reversedPath.get(path.length - 1 - i);

        return new TreePath(path);
    }
}
