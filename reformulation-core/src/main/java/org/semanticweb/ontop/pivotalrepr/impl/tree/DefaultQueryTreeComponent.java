package org.semanticweb.ontop.pivotalrepr.impl.tree;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeException;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * TODO: describe
 */
public class DefaultQueryTreeComponent implements QueryTreeComponent {

    private final QueryTree tree;

    /**
     * TODO: explain
     */
    protected DefaultQueryTreeComponent(QueryTree tree) {
        this.tree = tree;
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        return tree.getChildren(node);
    }

    @Override
    public ConstructionNode getRootConstructionNode() {
        return tree.getRootNode();
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() {
        return tree.getNodesInBottomUpOrder();
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() throws IllegalTreeException {
        return tree.getNodesInTopDownOrder();
    }

    @Override
    public boolean contains(QueryNode node) {
        return tree.contains(node);
    }

    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        tree.replaceNode(previousNode, replacingNode);
    }

    @Override
    public void addSubTree(IntermediateQuery subQuery, QueryNode subQueryTopNode, QueryNode localTopNode)
            throws IllegalTreeUpdateException {
        Queue<QueryNode> localParents = new LinkedList<>();
        localParents.add(localTopNode);
        Map<QueryNode, QueryNode> localToExternalNodeMap = new HashMap<>();
        localToExternalNodeMap.put(localTopNode, subQueryTopNode);

        while(!localParents.isEmpty()) {
            QueryNode localParent = localParents.poll();
            QueryNode externalParent = localToExternalNodeMap.get(localParent);

            for (QueryNode externalChild : subQuery.getCurrentSubNodesOf(externalParent)) {
                QueryNode localChild = externalChild.clone();
                localToExternalNodeMap.put(localChild, externalChild);
                localParents.add(localChild);
                addChild(localParent, localChild, subQuery.getOptionalPosition(externalParent, externalChild), false);
            }
        }

    }

    @Override
    public void removeSubTree(QueryNode subTreeRoot) {
        tree.removeSubTree(subTreeRoot);
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode) {
        return tree.getSubTreeNodesInTopDownOrder(currentNode);
    }

    @Override
    public Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                                       QueryNode childNode) {
        return tree.getOptionalPosition(parentNode, childNode);
    }

    @Override
    public ImmutableList<QueryNode> getAncestors(QueryNode descendantNode) throws IllegalTreeException {
        ImmutableList.Builder<QueryNode> ancestorBuilder = ImmutableList.builder();

        // Non-final
        Optional<QueryNode> optionalAncestor = getParent(descendantNode);

        while(optionalAncestor.isPresent()) {
            QueryNode ancestor = optionalAncestor.get();
            ancestorBuilder.add(ancestor);

            optionalAncestor = getParent(ancestor);
        }
        return ancestorBuilder.build();
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode node) throws IllegalTreeException {
        return tree.getParent(node);
    }

    @Override
    public void removeOrReplaceNodeByUniqueChildren(QueryNode node) throws IllegalTreeUpdateException {
        tree.removeOrReplaceNodeByUniqueChild(node);
    }

    @Override
    public void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode, QueryNode parentNode,
                                      Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        tree.replaceNodesByOneNode(queryNodes, replacingNode, parentNode, optionalPosition);
    }


    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode,
                         Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException {
        tree.addChild(parentNode, childNode, optionalPosition, true, canReplace);
    }

    @Override
    public Optional<QueryNode> nextSibling(QueryNode node) throws IllegalTreeException {
        Optional<QueryNode> optionalParent = tree.getParent(node);
        if (optionalParent.isPresent()) {
            ImmutableList<QueryNode> siblings = tree.getChildren(optionalParent.get());

            int index = siblings.indexOf(node);

            if (index == -1) {
                throw new IllegalTreeException("The node " + node + " does not appear as a child of its parent");
            }
            else if (index < (siblings.size() -1)) {
                return Optional.of(siblings.get(index + 1));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<QueryNode> getFirstChild(QueryNode node) {
        ImmutableList<QueryNode> children = tree.getChildren(node);

        switch(children.size()) {
            case 0:
                return Optional.absent();
            default:
                return Optional.of(children.get(0));
        }
    }
}
