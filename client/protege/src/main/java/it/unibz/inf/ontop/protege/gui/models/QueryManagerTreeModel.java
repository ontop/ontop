package it.unibz.inf.ontop.protege.gui.models;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.protege.core.QueryManager;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

public class QueryManagerTreeModel implements TreeModel, QueryManager.EventListener {
    private final QueryManager queryManager;

    public QueryManagerTreeModel(QueryManager queryManager) {
        this.queryManager = queryManager;
        queryManager.addListener(this);
    }

    @Override
    public Object getRoot() {
        return queryManager;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent == queryManager) {
            ImmutableList<QueryManager.Group> groups = ImmutableList.copyOf(queryManager.getGroups());
            QueryManager.Group group = groups.get(index);
            return (group.isDegenerate())
                ? group.getQueries().iterator().next()
                : group;
        }
        if (parent instanceof QueryManager.Group) {
            QueryManager.Group group = (QueryManager.Group) parent;
            ImmutableList<QueryManager.Query> queries = ImmutableList.copyOf(group.getQueries());
            return queries.get(index);
        }

        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == queryManager) {
            return queryManager.getGroups().size();
        }
        if (parent instanceof QueryManager.Group) {
            QueryManager.Group group = (QueryManager.Group) parent;
            return group.getQueries().size();
        }

        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof QueryManager.Query;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
// TODO:
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {

        if (child instanceof QueryManager.Group) {
            QueryManager.Group group = (QueryManager.Group) child;
            return group.getIndex();
        }

        if (child instanceof QueryManager.Query) {
            QueryManager.Query query = (QueryManager.Query) child;
            return query.getIndex();
        }

        return -1;
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
    public void added(QueryManager.Group group) {
        listeners.forEach(l -> l.treeNodesInserted(createEvent(group)));
    }

    @Override
    public void added(QueryManager.Query query) {
        listeners.forEach(l -> l.treeNodesInserted(createEvent(query)));
    }

    @Override
    public void removed(QueryManager.Group group) {
        listeners.forEach(l -> l.treeNodesRemoved(createEvent(group)));
    }

    @Override
    public void removed(QueryManager.Query query) {
        listeners.forEach(l -> l.treeNodesRemoved(createEvent(query)));
    }

    @Override
    public void changed(QueryManager.Query query) {
        listeners.forEach(l -> l.treeNodesChanged(createEvent(query)));
    }

    private TreeModelEvent createEvent(Object node) {
        Object parent = getParent(node);
        return new TreeModelEvent(
                this,
                getPathToRoot(parent),
                new int[]{ getIndexOfChild(parent, node) }, new Object[]{ node });
    }

    private TreePath getPathToRoot(Object node) {
        if (node == queryManager)
            return new TreePath(new Object[]{ queryManager });

        if (node instanceof QueryManager.Group)
            return new TreePath(new Object[]{ queryManager, node });

        if (node instanceof QueryManager.Query) {
            QueryManager.Query query = (QueryManager.Query) node;
            if (query.getGroup().isDegenerate())
                return new TreePath(new Object[]{ queryManager, node });
            else
                return new TreePath(new Object[]{ queryManager, query.getGroup(), query });
        }
        return null;
    }

    private Object getParent(Object node) {
        if (node instanceof QueryManager.Group)
            return queryManager;

        if (node instanceof QueryManager.Query) {
            QueryManager.Query query = (QueryManager.Query) node;
            if (query.getGroup().isDegenerate())
                return queryManager;
            else
                return query.getGroup();
        }
        return null;
    }
}
