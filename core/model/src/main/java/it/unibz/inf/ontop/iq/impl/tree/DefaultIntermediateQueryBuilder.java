package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * TODO: explain
 */
public class DefaultIntermediateQueryBuilder implements IntermediateQueryBuilder {

    private final IntermediateQueryFactory iqFactory;
    private DistinctVariableOnlyDataAtom projectionAtom;
    private QueryTree tree;
    private boolean canEdit;

    @AssistedInject
    protected DefaultIntermediateQueryBuilder(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        tree = null;
        canEdit = true;
    }


    @Override
    public void init(DistinctVariableOnlyDataAtom projectionAtom, QueryNode rootNode){
        if (tree != null)
            throw new IllegalArgumentException("Already initialized IntermediateQueryBuilder.");

        if ((rootNode instanceof ExplicitVariableProjectionNode)
            && !projectionAtom.getVariables().equals(((ExplicitVariableProjectionNode)rootNode).getVariables())) {
            throw new IllegalArgumentException("The root node " + rootNode
                    + " is not consistent with the projection atom " + projectionAtom);
        }


        // TODO: use Guice to construct this tree
        tree = new DefaultTree(rootNode);
        this.projectionAtom = projectionAtom;
        canEdit = true;
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode) throws IntermediateQueryBuilderException {
        checkEditMode();
        try {
            tree.addChild(parentNode, childNode);
        } catch (IllegalTreeUpdateException e) {
            throw new IntermediateQueryBuilderException(e.getMessage());
        }
    }

    @Override
    public IQ buildIQ() throws IntermediateQueryBuilderException {
        checkInitialization();

        canEdit = false;
        IQTree iqTree = convertTree(tree.getRootNode());
        return iqFactory.createIQ(projectionAtom, iqTree);
    }

    /**
     * Recursive
     */
    private IQTree convertTree(QueryNode rootNode) {
        if (rootNode instanceof LeafIQTree) {
            return (LeafIQTree) rootNode;
        }
        else if (rootNode instanceof UnaryOperatorNode) {
            // Recursive
            IQTree childTree = convertTree(tree.getChildren(rootNode).get(0));
            return iqFactory.createUnaryIQTree((UnaryOperatorNode) rootNode, childTree);
        }
        else if (rootNode instanceof BinaryNonCommutativeOperatorNode) {
            IQTree leftChildTree = convertTree(tree.getChildren(rootNode).get(0));
            IQTree rightChildTree = convertTree(tree.getChildren(rootNode).get(1));

            return iqFactory.createBinaryNonCommutativeIQTree((BinaryNonCommutativeOperatorNode) rootNode,
                    leftChildTree, rightChildTree);
        }
        else if (rootNode instanceof NaryOperatorNode) {
            ImmutableList<IQTree> childTrees = tree.getChildren(rootNode).stream()
                    .map(this::convertTree)
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree((NaryOperatorNode) rootNode, childTrees);
        }

        throw new MinorOntopInternalBugException("Unexpected type of query node: " + rootNode);
    }


    private void checkInitialization() throws IntermediateQueryBuilderException {
        if (tree == null)
            throw new IntermediateQueryBuilderException("Not initialized!");
    }

    private void checkEditMode() throws IntermediateQueryBuilderException {
        checkInitialization();

        if (!canEdit)
            throw new IllegalArgumentException("Cannot be edited anymore (the query has already been built).");
    }

}
