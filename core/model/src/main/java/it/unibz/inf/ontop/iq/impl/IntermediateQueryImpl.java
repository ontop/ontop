package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.UUID;
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

    private final DistinctVariableOnlyDataAtom projectionAtom;

    private final IntermediateQueryValidator validator;

    private final OntopModelSettings settings;

    private final IntermediateQueryFactory iqFactory;


    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    public IntermediateQueryImpl(DistinctVariableOnlyDataAtom projectionAtom,
                                 QueryTreeComponent treeComponent,
                                 IntermediateQueryValidator validator, OntopModelSettings settings,
                                 IntermediateQueryFactory iqFactory) {
        this.projectionAtom = projectionAtom;
        this.treeComponent = treeComponent;
        this.validator = validator;
        this.settings = settings;
        this.iqFactory = iqFactory;

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
        return new IntermediateQueryImpl(projectionAtom, treeComponent.createSnapshot(),
                validator, settings, iqFactory);
    }

    @Override
    public Stream<QueryNode> getOtherChildrenStream(QueryNode parent, QueryNode childToOmit) {
        return treeComponent.getChildrenStream(parent)
                .filter(c -> c != childToOmit);
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
    public UUID getVersionNumber() {
        return treeComponent.getVersionNumber();
    }

    @Override
    public IntermediateQueryBuilder newBuilder() {
        return iqFactory.createIQBuilder();
    }

    @Override
    public QueryNode getRootNode() throws InconsistentIntermediateQueryException{
        try {
            return treeComponent.getRootNode();
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
    public Optional<QueryNode> getChild(QueryNode currentNode, BinaryOrderedOperatorNode.ArgumentPosition position) {
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

    @Override
    public Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                                    QueryNode childNode) {
        return treeComponent.getOptionalPosition(parentNode, childNode);
    }

    @Override
    public Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode child) {
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
        return createSnapshot();
    }

    @Override
    public String toString() {
        return PRINTER.stringify(this);
    }

    @Override
    public IntermediateQueryFactory getFactory() {
        return iqFactory;
    }

    @Override
    public ImmutableSet<Variable> getVariablesRequiredByAncestors(QueryNode queryNode) {
        ImmutableSet.Builder<Variable> requiredVariableBuilder = ImmutableSet.builder();

        // Non-final
        Optional<QueryNode> optionalAncestor = getParent(queryNode);
        while (optionalAncestor.isPresent()) {
            QueryNode ancestor = optionalAncestor.get();

            ancestor.getRequiredVariables(this)
                    .forEach(requiredVariableBuilder::add);

            if (ancestor instanceof ExplicitVariableProjectionNode)
                break;
            optionalAncestor = getParent(ancestor);
        }
        // Root reached
        if (!optionalAncestor.isPresent())
            requiredVariableBuilder.addAll(getProjectionAtom().getVariables());

        ImmutableSet<Variable> requiredVariables = requiredVariableBuilder.build();
        /*
         * Restrict to variables which can actually be provided.
         * Assumption: variables defined by a node should not appear in its subtree
         */
        return getVariables(queryNode).stream()
                .filter(requiredVariables::contains)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public IntermediateQuery getSubquery(QueryNode subQueryRoot, DistinctVariableOnlyDataAtom projectionAtom) {
        IntermediateQueryBuilder builder = iqFactory.createIQBuilder();
        builder.init(projectionAtom, subQueryRoot);
        builder.appendSubtree(subQueryRoot, this);
        return builder.build();
    }

    private void validate() throws InvalidIntermediateQueryException {
        validator.validate(this);
    }
}
