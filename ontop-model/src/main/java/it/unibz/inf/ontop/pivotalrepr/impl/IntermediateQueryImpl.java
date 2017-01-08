package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.injection.OntopModelProperties;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.pivotalrepr.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: describe
 *
 * BEWARE: this class has a non-trivial mutable internal state!
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * Thrown when the internal state of the intermediate query is found to be inconsistent.
     *
     * Should not be expected (internal error).
     *
     */
    public static class InconsistentIntermediateQueryException extends RuntimeException {
        public InconsistentIntermediateQueryException(String message) {
            super(message);
        }
    }

    /**
     * TODO: use Guice to replace it.
     */
    private static final IntermediateQueryPrinter PRINTER = new BasicQueryTreePrinter();


    /**
     * Highly mutable (low control) so MUST NOT BE SHARED (except with InternalProposalExecutor)!
     */
    private final QueryTreeComponent treeComponent;

    private final MetadataForQueryOptimization metadata;

    private final DistinctVariableOnlyDataAtom projectionAtom;

    private final ExecutorRegistry executorRegistry;

    private final IntermediateQueryValidator validator;

    private final OntopModelProperties settings;

    private final OntopModelFactory modelFactory;


    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    public IntermediateQueryImpl(MetadataForQueryOptimization metadata, DistinctVariableOnlyDataAtom projectionAtom,
                                 QueryTreeComponent treeComponent, ExecutorRegistry executorRegistry,
                                 IntermediateQueryValidator validator, OntopModelProperties settings,
                                 OntopModelFactory modelFactory) {
        this.metadata = metadata;
        this.projectionAtom = projectionAtom;
        this.treeComponent = treeComponent;
        this.executorRegistry = executorRegistry;
        this.validator = validator;
        this.settings = settings;
        this.modelFactory = modelFactory;

        if (settings.isTestModeEnabled())
            validate();
    }

    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return projectionAtom;
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return treeComponent.getKnownVariables();
    }

    @Override
    public IntermediateQuery createSnapshot() {
        return new IntermediateQueryImpl(metadata, projectionAtom, treeComponent.createSnapshot(),
                executorRegistry, validator, settings, modelFactory);
    }

    @Override
    public Stream<QueryNode> getOtherChildrenStream(QueryNode parent, QueryNode childToOmmit) {
        return treeComponent.getChildrenStream(parent)
                .filter(c -> c != childToOmmit);
    }

    /**
     * TODO: replace by a more efficient implementation
     */
    @Override
    public boolean hasAncestor(QueryNode descendantNode, QueryNode ancestorNode) {
        return getAncestors(descendantNode).contains(ancestorNode);
    }

    @Override
    public ImmutableSet<Variable> getVariables(QueryNode subTreeRootNode) {
        return treeComponent.getVariables(subTreeRootNode);
    }

    @Override
    public int getVersionNumber() {
        return treeComponent.getVersionNumber();
    }

    @Override
    public IntermediateQueryBuilder newBuilder() {
        return modelFactory.create(metadata, executorRegistry);
    }

    @Override
    public MetadataForQueryOptimization getMetadata() {
        return metadata;
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws InconsistentIntermediateQueryException{
        try {
            return treeComponent.getRootConstructionNode();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() throws InconsistentIntermediateQueryException {
        try {
            return treeComponent.getNodesInBottomUpOrder();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        try {
            return treeComponent.getNodesInTopDownOrder();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        return treeComponent.getChildren(node);
    }

    @Override
    public Stream<QueryNode> getChildrenStream(QueryNode node) {
        return treeComponent.getChildrenStream(node);
    }

    @Override
    public Optional<QueryNode> getChild(QueryNode currentNode, NonCommutativeOperatorNode.ArgumentPosition position) {
        return getChildren(currentNode).stream()
                .filter(c -> getOptionalPosition(currentNode, c)
                        .filter(position::equals)
                        .isPresent())
                .findFirst();
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode) {
        return treeComponent.getSubTreeNodesInTopDownOrder(currentNode);
    }

    @Override
    public Stream<IntensionalDataNode> getIntensionalNodes(){
        return treeComponent.getIntensionalNodes().stream();
    }

    @Override
    public Stream<TrueNode> getTrueNodes(){
        return treeComponent.getTrueNodes().stream();
    }

    @Override
    public boolean contains(QueryNode node) {
        return treeComponent.contains(node);
    }

    /**
     * TODO: remove this method
     */
    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> R applyProposal(P proposal,
                                                       boolean requireUsingInternalExecutor)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        return applyProposal(proposal, requireUsingInternalExecutor, false);
    }

    /**
     * TODO: remove requireUsingInternalExecutor
     */
    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> R applyProposal(P proposal,
                                                                                               boolean requireUsingInternalExecutor,
                                                                                               boolean disableValidationTests)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        if ((!disableValidationTests) && settings.isTestModeEnabled()) {
            validate();
        }

        InternalProposalExecutor<P, R> executor = executorRegistry.getExecutor(proposal);

        /**
         * Has a SIDE-EFFECT on the tree component.
         */
        R results = executor.apply(proposal, this, treeComponent);
        if ((!disableValidationTests) && settings.isTestModeEnabled()) {
            validate();
        }

        return results;
    }

    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> R applyProposal(P propagationProposal)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        return applyProposal(propagationProposal, false, false);
    }

    @Override
    public Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                                     QueryNode childNode) {
        return treeComponent.getOptionalPosition(parentNode, childNode);
    }

    @Override
    public Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode child) {
        Optional<QueryNode> optionalParent = getParent(child);
        if (optionalParent.isPresent()) {
            return getOptionalPosition(optionalParent.get(), child);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public ImmutableList<QueryNode> getAncestors(QueryNode descendantNode) {
        try {
            return treeComponent.getAncestors(descendantNode);
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode node) {
        try {
            return treeComponent.getParent(node);
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public Optional<QueryNode> getNextSibling(QueryNode node) {
        try {
            return treeComponent.nextSibling(node);
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public Optional<QueryNode> getFirstChild(QueryNode node) {
        return treeComponent.getFirstChild(node);
    }


    @Override
    public ConstructionNode getClosestConstructionNode(QueryNode node) {
        if (node instanceof ConstructionNode) {
            return (ConstructionNode) node;
        }

        for (QueryNode ancestor : getAncestors(node)) {
            if (ancestor instanceof ConstructionNode) {
                return (ConstructionNode) ancestor;
            }
        }
        throw new InconsistentIntermediateQueryException("The node " + node
                + " has no ancestor that is a ConstructionNode");
    }

    @Override
    public Variable generateNewVariable() {
        return treeComponent.generateNewVariable();
    }

    @Override
    public Variable generateNewVariable(Variable formerVariable) {
        return treeComponent.generateNewVariable(formerVariable);
    }

    /**
     * Not appearing in the interface because users do not
     * have to worry about it.
     */
    @Override
    public IntermediateQuery clone() {
        return IntermediateQueryUtils.convertToBuilder(this).build();
    }

    @Override
    public String toString() {
        return PRINTER.stringify(this);
    }

    private void validate() throws InvalidIntermediateQueryException {
        validator.validate(this);
    }
}
