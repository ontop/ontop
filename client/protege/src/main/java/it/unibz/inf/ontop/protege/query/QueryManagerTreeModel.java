package it.unibz.inf.ontop.protege.query;

import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.utils.EventListenerList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class QueryManagerTreeModel implements TreeModel, OBDAModelManagerListener, QueryManagerListener {
    private final OBDAModelManager obdaModelManager;

    public QueryManagerTreeModel(OBDAModelManager obdaModelManager) {
        this.obdaModelManager = obdaModelManager;
    }

    @Override
    public QueryManager.Item getRoot() {
        return obdaModelManager.getCurrentOBDAModel().getQueryManager().getRoot();
    }

    @Override
    public QueryManager.Item getChild(Object parentO, int index) {
        QueryManager.Item parent = (QueryManager.Item)parentO;
        if (index < 0 || index >= parent.getChildCount())
            return null;

        return parent.getChild(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((QueryManager.Item)parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((QueryManager.Item)node).getChildCount() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        QueryManager.Item item = (QueryManager.Item)path.getLastPathComponent();
        //item.setUserObject(newValue);
        //nodeChanged(aNode);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if(parent == null || child == null)
            return -1;

        return ((QueryManager.Item)parent).getIndexOfChild((QueryManager.Item)child);
    }

    private final EventListenerList<TreeModelListener> listeners = new EventListenerList<>();

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void inserted(QueryManager.Item item, int indexInParent) {
        createEventAndNotify(item, indexInParent, TreeModelListener::treeNodesInserted);
    }

    @Override
    public void removed(QueryManager.Item item, int indexInParent) {
        createEventAndNotify(item, indexInParent, TreeModelListener::treeNodesRemoved);
    }

    @Override
    public void renamed(QueryManager.Item item, int indexInParent) {
        createEventAndNotify(item, indexInParent, TreeModelListener::treeNodesChanged);
    }

    @Override
    public void changed(QueryManager.Item query, int indexInParent) { /* NO-OP */ }

    private void createEventAndNotify(QueryManager.Item item, int indexInParent, BiConsumer<TreeModelListener, TreeModelEvent> eventConsumer) {
        List<QueryManager.Item> path = new LinkedList<>();
        for (QueryManager.Item c = item.getParent(); c != null; c = c.getParent())
            path.add(0, c); // the path starts from the root

        TreeModelEvent event = new TreeModelEvent(this, new TreePath(path.toArray()),
                new int[]{ indexInParent }, new Object[]{ item });

        listeners.fire(l -> eventConsumer.accept(l, event));
    }

    @Override
    public void activeOntologyChanged(OBDAModel obdaModel) {
        TreeModelEvent event = new TreeModelEvent(this, new TreePath(new Object[]{ getRoot() }),
                null, new Object[]{ getRoot() });

        listeners.fire(l -> l.treeStructureChanged(event));
    }
}
