package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: explain
 *
 * Mutable
 *
 */
public class DefaultTree implements QueryTree {

    private TreeNode rootNode;
    private final Map<QueryNode, TreeNode> nodeIndex;
    private final Map<TreeNode, ChildrenRelation> childrenIndex;
    private final Map<TreeNode, TreeNode> parentIndex;
    private final Set<TrueNode> trueNodes;
    private final Set<IntensionalDataNode> intensionalNodes;
    private UUID versionNumber;


    protected DefaultTree(QueryNode rootQueryNode) {
        nodeIndex = new HashMap<>();
        childrenIndex = new HashMap<>();
        parentIndex = new HashMap<>();
        trueNodes = new HashSet<>();
        intensionalNodes = new HashSet<>();

        // Adds the root node
        rootNode = new TreeNode(rootQueryNode);
        insertNodeIntoIndex(rootQueryNode, rootNode);
        childrenIndex.put(rootNode, createChildrenRelation(rootNode));
        // No parent

        versionNumber = UUID.randomUUID();
    }

    private DefaultTree(TreeNode rootNode,
                        Map<QueryNode, TreeNode> nodeIndex,
                        Map<TreeNode, ChildrenRelation> childrenIndex,
                        Map<TreeNode, TreeNode> parentIndex,
                        Set<TrueNode> trueNodes,
                        Set<IntensionalDataNode> intensionalNodes,
                        UUID versionNumber) {
        this.rootNode = rootNode;
        this.nodeIndex = nodeIndex;
        this.childrenIndex = childrenIndex;
        this.parentIndex = parentIndex;
        this.trueNodes = trueNodes;
        this.intensionalNodes = intensionalNodes;
        this.versionNumber = versionNumber;
    }

    @Override
    public QueryNode getRootNode() {
        return rootNode.getQueryNode();
    }

    @Override
    public void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition,
                         boolean mustBeNew, boolean canReplace) throws IllegalTreeUpdateException {
        TreeNode parentNode = accessTreeNode(parentQueryNode);

        TreeNode childNode;
        if (nodeIndex.containsKey(childQueryNode)) {
            if (mustBeNew) {
                throw new IllegalTreeUpdateException("Node " + childQueryNode + " already in the graph");
            }
            else {
                childNode = accessTreeNode(childQueryNode);

                TreeNode previousParent = getParentTreeNode(childNode);
                if (previousParent != null) {
                    removeChild(previousParent, childNode);
                }
                parentIndex.put(childNode, parentNode);
                accessChildrenRelation(parentNode).addChild(childNode, optionalPosition, canReplace);
            }
        }
        /**
         * New node
         */
        else {
            createNewNode(childQueryNode, parentNode, optionalPosition, canReplace);
        }
    }

    /**
     * Low-level
     */
    private void createNewNode(QueryNode childQueryNode, TreeNode parentNode,
                               Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition, boolean canReplace)
            throws IllegalTreeUpdateException {
        TreeNode childNode = new TreeNode(childQueryNode);
        insertNodeIntoIndex(childQueryNode, childNode);

        childrenIndex.put(childNode, createChildrenRelation(childNode));

        parentIndex.put(childNode, parentNode);
        accessChildrenRelation(parentNode).addChild(childNode, optionalPosition, canReplace);
    }

    private static ChildrenRelation createChildrenRelation(TreeNode parentTreeNode) {
        if (parentTreeNode.getQueryNode() instanceof BinaryOrderedOperatorNode) {
            return new BinaryChildrenRelation(parentTreeNode);
        }
        else {
            return new StandardChildrenRelation(parentTreeNode);
        }
    }


    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        ChildrenRelation childrenRelation = accessChildrenRelation(accessTreeNode(node));
        if (childrenRelation == null) {
            return ImmutableList.of();
        }
        else {
            return childrenRelation.getChildQueryNodes();
        }
    }

    @Override
    public Stream<QueryNode> getChildrenStream(QueryNode node) {
        ChildrenRelation childrenRelation = accessChildrenRelation(accessTreeNode(node));
        if (childrenRelation == null) {
            return Stream.of();
        }
        else {
            return childrenRelation.getChildQueryNodeStream();
        }
    }

    @Override
    public boolean contains(QueryNode node) {
        return nodeIndex.containsKey(node);
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() {
        return getNodesInTopDownOrder().reverse();
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        Queue<TreeNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(rootNode);
        builder.add(rootNode.getQueryNode());

        while (!nodesToExplore.isEmpty()) {
            TreeNode node = nodesToExplore.poll();
            for (TreeNode childNode : accessChildrenRelation(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        TreeNode treeNode = accessTreeNode(previousNode);
        if (treeNode == null) {
            throw new IllegalArgumentException("The previous query node must be in the tree");
        }
        if (contains(replacingNode)) {
            throw new IllegalArgumentException("The replacing node must not be already in the tree");
        }

        treeNode.changeQueryNode(replacingNode);
        removeNodeFromIndex(previousNode);
        insertNodeIntoIndex(replacingNode, treeNode);

        if ((!(previousNode instanceof BinaryOrderedOperatorNode))
                && (replacingNode instanceof BinaryOrderedOperatorNode)) {
            ChildrenRelation newChildrenRelation = accessChildrenRelation(treeNode)
                    .convertToBinaryChildrenRelation();
            // Overrides the previous entry
            childrenIndex.put(treeNode, newChildrenRelation);
        }
        else if ((previousNode instanceof BinaryOrderedOperatorNode)
                && (!(replacingNode instanceof BinaryOrderedOperatorNode))) {
            ChildrenRelation newChildrenRelation = accessChildrenRelation(treeNode)
                    .convertToStandardChildrenRelation();
            // Overrides the previous entry
            childrenIndex.put(treeNode, newChildrenRelation);
        }
    }

    @Override
    public void removeSubTree(QueryNode subQueryTreeRoot) {
        TreeNode subTreeRoot = accessTreeNode(subQueryTreeRoot);

        Queue<TreeNode> nodesToRemove = new LinkedList<>();
        nodesToRemove.add(subTreeRoot);

        while(!nodesToRemove.isEmpty()) {
            TreeNode treeNode = nodesToRemove.poll();
            nodesToRemove.addAll(accessChildrenRelation(treeNode).getChildren());

            removeNode(treeNode);
        }
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentQueryNode) {
        TreeNode currentTreeNode = accessTreeNode(currentQueryNode);

        Queue<TreeNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(currentTreeNode);
        // The root is excluded from the list

        while (!nodesToExplore.isEmpty()) {
            TreeNode node = nodesToExplore.poll();
            for (TreeNode childNode : accessChildrenRelation(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode childQueryNode) {
        TreeNode childTreeNode = accessTreeNode(childQueryNode);

        TreeNode parentTreeNode = getParentTreeNode(childTreeNode);
        if (parentTreeNode == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(parentTreeNode.getQueryNode());
        }
    }

    @Override
    public QueryNode removeOrReplaceNodeByUniqueChild(QueryNode parentQueryNode) throws IllegalTreeUpdateException {
        TreeNode parentTreeNode = accessTreeNode(parentQueryNode);
        removeNodeFromIndex(parentQueryNode);
        ImmutableList<TreeNode> children = accessChildrenRelation(parentTreeNode).getChildren();

        if (children.size() == 1) {
            TreeNode childTreeNode = children.get(0);
            childrenIndex.remove(parentTreeNode);
            // May be null
            TreeNode grandParentTreeNode = getParentTreeNode(parentTreeNode);
            parentIndex.remove(parentTreeNode);

            /*
             * When the child node becomes the new root
             */
            if (grandParentTreeNode == null) {
                rootNode = childTreeNode;
                parentIndex.remove(childTreeNode);
            }
            else {
                parentIndex.put(childTreeNode, grandParentTreeNode);
                ChildrenRelation grandParentRelation = accessChildrenRelation(grandParentTreeNode);
                grandParentRelation.replaceChild(parentTreeNode, childTreeNode);
            }
            return childTreeNode.getQueryNode();
        }
        else {
            throw new IllegalTreeUpdateException("The query node " + parentQueryNode + " does not have a unique child");
        }
    }

    @Override
    public void replaceNodesByOneNode(ImmutableList<QueryNode> nodesToRemove, QueryNode replacingNode,
                                      QueryNode parentNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException {
        if (replacingNode instanceof BinaryOrderedOperatorNode) {
            throw new RuntimeException("Having a BinaryAsymmetricOperatorNode replacing node is not yet supported");
        }
        addChild(parentNode, replacingNode, optionalPosition, true, true);


        for(QueryNode nodeToRemove : nodesToRemove) {
            boolean isParentBinaryAsymmetricOperator = (nodeToRemove instanceof BinaryOrderedOperatorNode);

            TreeNode treeNodeToRemove = accessTreeNode(nodeToRemove);

            for (QueryNode child : accessChildrenRelation(treeNodeToRemove).getChildQueryNodes()) {
                if (!nodesToRemove.contains(child)) {
                    if (isParentBinaryAsymmetricOperator) {
                        throw new RuntimeException("Re-integrating children of a BinaryAsymmetricOperatorNode " +
                                "is not yet supported");
                    }
                    else {
                        addChild(replacingNode, child, Optional.<BinaryOrderedOperatorNode.ArgumentPosition>empty(), false, true);
                    }
                }
            }
            removeNode(treeNodeToRemove);
        }
    }

    @Override
    public Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode childNode) {
        TreeNode parentTreeNode = accessTreeNode(parentNode);
        TreeNode childTreeNode = accessTreeNode(childNode);

        ChildrenRelation childrenRelation = accessChildrenRelation(parentTreeNode);
        return childrenRelation.getOptionalPosition(childTreeNode);
    }

    @Override
    public void insertParent(QueryNode childNode, QueryNode newParentNode,
                             Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException {
        if (contains(newParentNode)) {
            throw new IllegalTreeUpdateException(newParentNode + " is already present so cannot be inserted again");
        }


        TreeNode childTreeNode = accessTreeNode(childNode);

        TreeNode newParentTreeNode = new TreeNode(newParentNode);
        insertNodeIntoIndex(newParentNode, newParentTreeNode);
        childrenIndex.put(newParentTreeNode, createChildrenRelation(newParentTreeNode));

        Optional<QueryNode> optionalFormerParent = getParent(childNode);
        if (!optionalFormerParent.isPresent()) {
            rootNode = newParentTreeNode;
        } else {
            QueryNode grandParentNode = optionalFormerParent.get();
            TreeNode grandParentTreeNode = accessTreeNode(grandParentNode);
            changeChild(grandParentTreeNode, childTreeNode, newParentTreeNode);
        }

        addChild(newParentNode, childNode, optionalPosition, false, false);
    }

    public ImmutableSet<TrueNode> getTrueNodes() {
        return ImmutableSet.copyOf(trueNodes);
    }

    @Override
    public ImmutableSet<IntensionalDataNode> getIntensionalNodes(){
        return ImmutableSet.copyOf(intensionalNodes);
    }

    @Override
    public QueryNode replaceNodeByChild(QueryNode parentNode,
                                        Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalReplacingChildPosition) {
        TreeNode parentTreeNode = accessTreeNode(parentNode);

        ChildrenRelation childrenRelation = accessChildrenRelation(parentTreeNode);

        TreeNode childTreeNode;
        if (optionalReplacingChildPosition.isPresent()) {
            childTreeNode = childrenRelation.getChild(optionalReplacingChildPosition.get())
                    .orElseThrow(() -> new IllegalTreeUpdateException("No child at the position"
                            + optionalReplacingChildPosition.get()));
        }
        else {
            ImmutableList<TreeNode> children = childrenRelation.getChildren();
            if (children.isEmpty()) {
                throw new IllegalTreeUpdateException("The node cannot be replaced by a child " +
                        "(does not have any)");
            }
            childTreeNode = children.get(0);
        }

        childrenIndex.remove(parentTreeNode);
        // May be null
        TreeNode grandParentTreeNode = getParentTreeNode(parentTreeNode);
        parentIndex.remove(parentTreeNode);

        if (grandParentTreeNode == null) {
            rootNode = childTreeNode;
            parentIndex.remove(childTreeNode);
        }
        else {
            parentIndex.put(childTreeNode, grandParentTreeNode);
            ChildrenRelation grandParentRelation = accessChildrenRelation(grandParentTreeNode);
            grandParentRelation.replaceChild(parentTreeNode, childTreeNode);
        }
        return childTreeNode.getQueryNode();
    }

    @Override
    public QueryTree createSnapshot() {
        Map<QueryNode, TreeNode> newNodeIndex = nodeIndex.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().cloneShallowly()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        Map<TreeNode, ChildrenRelation> newChildrenIndex = childrenIndex.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(
                        e.getKey().findNewTreeNode(newNodeIndex),
                        e.getValue().clone(newNodeIndex)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        Map<TreeNode, TreeNode> newParentIndex = parentIndex.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().findNewTreeNode(newNodeIndex),
                        e.getValue().findNewTreeNode(newNodeIndex)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        return new DefaultTree(newNodeIndex.get(rootNode.getQueryNode()), newNodeIndex, newChildrenIndex,
                newParentIndex, new HashSet<>(trueNodes), new HashSet<>(intensionalNodes), versionNumber);
    }

    @Override
    public void transferChild(QueryNode childNode, QueryNode formerParentNode, QueryNode newParentNode,
                              Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) {

        TreeNode formerParentTreeNode = accessTreeNode(formerParentNode);
        TreeNode childTreeNode = accessTreeNode(childNode);

        accessChildrenRelation(formerParentTreeNode).removeChild(childTreeNode);

        addChild(newParentNode, childNode, optionalPosition, false, false);
    }

    @Override
    public UUID getVersionNumber() {
        return versionNumber;
    }

    private void updateVersionNumber() {
        versionNumber = UUID.randomUUID();
    }

    /**
     * Low-level
     */
    private void removeNode(TreeNode treeNode) {
        removeNodeFromIndex(treeNode.getQueryNode());
        TreeNode parentNode = getParentTreeNode(treeNode);
        if (parentNode != null) {
            accessChildrenRelation(parentNode).removeChild(treeNode);
        }
        parentIndex.remove(treeNode);

        /**
         * Its children have no parent anymore
         */
        for (TreeNode childTreeNode : childrenIndex.get(treeNode).getChildren()) {
            parentIndex.remove(childTreeNode);
        }
        childrenIndex.remove(treeNode);
    }

    private void removeChild(TreeNode parentNode, TreeNode childNodeToRemove) {
        if (getParentTreeNode(childNodeToRemove) == parentNode) {
            parentIndex.remove(childNodeToRemove);
        }

        if (childrenIndex.containsKey(parentNode)) {
            accessChildrenRelation(parentNode).removeChild(childNodeToRemove);
        }
    }

    private void changeChild(TreeNode parentNode, TreeNode childNodeToReplace, TreeNode replacingChild) {
        if (getParentTreeNode(childNodeToReplace) == parentNode) {
            parentIndex.remove(childNodeToReplace);
            parentIndex.put(replacingChild, parentNode);
        }
        else {
            throw new IllegalArgumentException(childNodeToReplace + " is not the child of " + parentNode);
        }

        if (childrenIndex.containsKey(parentNode)) {
            accessChildrenRelation(parentNode).replaceChild(childNodeToReplace, replacingChild);
        }
        else {
            throw new IllegalTreeUpdateException(parentNode + " has no childrenRelation");
        }
    }

    private TreeNode accessTreeNode(QueryNode node) {
        TreeNode treeNode = nodeIndex.get(node);
        if (treeNode == null) {
            throw new IllegalArgumentException("The given query node is not in the tree");
        }
        return treeNode;
    }

    private ChildrenRelation accessChildrenRelation(TreeNode node) {
        ChildrenRelation relation = childrenIndex.get(node);
        if (relation == null) {
            throw new RuntimeException("Internal error: the tree node does not have a children relation.");
        }
        return relation;
    }

    /**
     * The returned value might be null.
     *
     * The point of this structure is to enforce the use of a TreeNode as argument.
     */
    private TreeNode getParentTreeNode(TreeNode child) {
        TreeNode parentTreeNode = parentIndex.get(child);

        if (parentTreeNode == null)
            return null;

        // Makes sure the parent node is still present in the tree
        else if (contains(parentTreeNode.getQueryNode()))
            return parentTreeNode;
        else
            throw new RuntimeException("Internal error: points to a parent that is not (anymore) in the tree");
    }


    /**
     * Low-low-level
     */
    private void insertNodeIntoIndex(QueryNode queryNode, TreeNode treeNode) {
        nodeIndex.put(queryNode, treeNode);
        if (queryNode instanceof TrueNode){
           trueNodes.add((TrueNode) queryNode);
        }
        else if (queryNode instanceof IntensionalDataNode){
            intensionalNodes.add((IntensionalDataNode) queryNode);
        }
        updateVersionNumber();
    }

    /**
     * Low-low-level
     */
    private void removeNodeFromIndex(QueryNode queryNode) {
        nodeIndex.remove(queryNode);

        if (queryNode instanceof TrueNode) {
            trueNodes.remove(queryNode);
        } else if (queryNode instanceof IntensionalDataNode){
            intensionalNodes.remove(queryNode);
        }
        updateVersionNumber();
    }

}
