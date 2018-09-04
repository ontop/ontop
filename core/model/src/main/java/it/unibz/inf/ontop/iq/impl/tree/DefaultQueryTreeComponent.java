package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.tools.VariableCollector;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;


/**
 * TODO: describe
 *
 * Every time a node is added, please call the method collectPossiblyNewVariables()!
 */
public class DefaultQueryTreeComponent implements QueryTreeComponent {

    private final QueryTree tree;
    private final VariableGenerator variableGenerator;

    /**
     * TODO: explain
     */
    protected DefaultQueryTreeComponent(QueryTree tree, CoreUtilsFactory coreUtilsFactory) {
        this(tree,
                coreUtilsFactory.createVariableGenerator(
                        VariableCollector.collectVariables(
                                tree.getNodesInTopDownOrder())));
    }

    private DefaultQueryTreeComponent(QueryTree tree, VariableGenerator variableGenerator) {
        this.tree = tree;
        this.variableGenerator = variableGenerator;
    }

    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        return tree.getChildren(node);
    }

    @Override
    public Stream<QueryNode> getChildrenStream(QueryNode node) {
        return tree.getChildrenStream(node);
    }

    @Override
    public QueryNode getRootNode() {
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
    public ImmutableSet<TrueNode> getTrueNodes() {
        return tree.getTrueNodes();
    }

    @Override
    public ImmutableSet<IntensionalDataNode> getIntensionalNodes() {
        return tree.getIntensionalNodes();
    }

    @Override
    public boolean contains(QueryNode node) {
        return tree.contains(node);
    }

    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        collectPossiblyNewVariables(replacingNode);
        tree.replaceNode(previousNode, replacingNode);
    }

    @Override
    public void replaceSubTree(QueryNode subTreeRootNode, QueryNode replacingNode) {
        getChildren(subTreeRootNode).stream()
                .forEach(this::removeSubTree);
        replaceNode(subTreeRootNode, replacingNode);
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

            for (QueryNode externalChild : subQuery.getChildren(externalParent)) {
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
    public Optional<ArgumentPosition> getOptionalPosition(QueryNode parentNode,
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
    public QueryNode removeOrReplaceNodeByUniqueChild(QueryNode node) throws IllegalTreeUpdateException {
        return tree.removeOrReplaceNodeByUniqueChild(node);
    }

    @Override
    public void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode, QueryNode parentNode,
                                      Optional<ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        collectPossiblyNewVariables(replacingNode);
        tree.replaceNodesByOneNode(queryNodes, replacingNode, parentNode, optionalPosition);
    }


    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode,
                         Optional<ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException {
        collectPossiblyNewVariables(childNode);
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
        return Optional.empty();
    }

    @Override
    public Optional<QueryNode> getFirstChild(QueryNode node) {
        ImmutableList<QueryNode> children = tree.getChildren(node);

        switch(children.size()) {
            case 0:
                return Optional.empty();
            default:
                return Optional.of(children.get(0));
        }
    }

    @Override
    public void insertParent(QueryNode childNode, QueryNode newParentNode) throws IllegalTreeUpdateException {
        insertParent(childNode, newParentNode, Optional.empty());
    }

    @Override
    public void insertParent(QueryNode childNode, QueryNode newParentNode, Optional<ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        collectPossiblyNewVariables(newParentNode);
        tree.insertParent(childNode, newParentNode, optionalPosition);
    }

    @Override
    public void transferChild(QueryNode childNode, QueryNode formerParentNode, QueryNode newParentNode,
                              Optional<ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException {

        tree.transferChild(childNode, formerParentNode, newParentNode, optionalPosition);
    }

    @Override
    public Variable generateNewVariable() {
        return variableGenerator.generateNewVariable();
    }

    @Override
    public Variable generateNewVariable(Variable formerVariable) {
        return variableGenerator.generateNewVariableFromVar(formerVariable);
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return variableGenerator.getKnownVariables();
    }

    @Override
    public QueryNode replaceNodeByChild(QueryNode parentNode, Optional<ArgumentPosition> optionalReplacingChildPosition) {
        return tree.replaceNodeByChild(parentNode, optionalReplacingChildPosition);
    }

    @Override
    public QueryTreeComponent createSnapshot() {
        return new DefaultQueryTreeComponent(tree.createSnapshot(), variableGenerator.createSnapshot());
    }

    /**
     * TODO: optimize by it but materializing (and maintaining) the results.
     */
    @Override
    public ImmutableSet<Variable> getVariables(QueryNode node) {
        if (node instanceof ExplicitVariableProjectionNode) {
            return ((ExplicitVariableProjectionNode) node).getVariables();
        }
        else {
            return getProjectedVariableStream(node)
                    .collect(ImmutableCollectors.toSet());
        }
    }

    @Override
    public UUID getVersionNumber() {
        return tree.getVersionNumber();
    }

    @Override
    public QueryNode replaceSubTreeByIQ(QueryNode subTreeRoot, IQTree replacingSubTree) {
        QueryNode iqRoot = replacingSubTree.getRootNode();
        QueryNode newSubTreeRoot = contains(iqRoot) ? iqRoot.clone() : iqRoot;
        replaceSubTree(subTreeRoot, newSubTreeRoot);

        insertIQChildren(newSubTreeRoot, replacingSubTree.getChildren());
        return newSubTreeRoot;
    }

    /**
     * Recursive
     */
    private void insertIQChildren(QueryNode parentNode, ImmutableList<IQTree> childrenTrees) {

        ImmutableList<QueryNode> newChildren = childrenTrees.stream()
                .map(IQTree::getRootNode)
                .map(n -> contains(n) ? n.clone() : n)
                .collect(ImmutableCollectors.toList());

        if (parentNode instanceof BinaryOrderedOperatorNode) {
            addChild(parentNode, newChildren.get(0), Optional.of(LEFT), false);
            addChild(parentNode, newChildren.get(1), Optional.of(RIGHT), false);
        }
        else {
            newChildren
                    .forEach(c -> addChild(parentNode, c, Optional.empty(),false));
        }

        IntStream.range(0, childrenTrees.size())
                .forEach(i -> insertIQChildren(newChildren.get(i), childrenTrees.get(i).getChildren()));
    }


    private Stream<Variable> getProjectedVariableStream(QueryNode node) {
        if (node instanceof ExplicitVariableProjectionNode) {
            return ((ExplicitVariableProjectionNode) node).getVariables().stream();
        }
        else {
            return getChildrenStream(node)
                    .flatMap(this::getProjectedVariableStream);
        }
    }


    /**
     * To be called every time a new node is added to the tree component.
     */
    private void collectPossiblyNewVariables(QueryNode newNode) {
        variableGenerator.registerAdditionalVariables(newNode.getLocalVariables());
    }
}
