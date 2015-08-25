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
        tree.replaceNode(previousNode, replacingNode, true);
    }

    @Override
    public void addSubTree(IntermediateQuery subQuery, QueryNode parentNode) {
        throw new RuntimeException("TODO: implement it!");
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
        throw new RuntimeException("TODO: implement it!");
    }

    @Override
    public ImmutableList<QueryNode> getAncestors(QueryNode descendantNode) throws IllegalTreeException {
        throw new RuntimeException("TODO: implement it!");
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode node) throws IllegalTreeException {
        return tree.getParent(node);
    }

    @Override
    public void removeOrReplaceNodeByUniqueChildren(QueryNode node) throws IllegalTreeUpdateException {
        throw new RuntimeException("TODO: implement it!");
    }

    @Override
    public void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode)
            throws IllegalTreeUpdateException {
        throw new RuntimeException("TODO: implement it!");
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode,
                         Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        tree.addChild(parentNode, childNode, optionalPosition, true);
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
