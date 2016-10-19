package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.executor.expression.PushDownExpressionExecutorImpl;
import it.unibz.inf.ontop.executor.join.JoinInternalCompositeExecutor;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfSubTreeExecutorImpl;
import it.unibz.inf.ontop.executor.merging.QueryMergingExecutorImpl;
import it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationExecutorImpl;
import it.unibz.inf.ontop.executor.truenode.TrueNodeRemovalExecutorImpl;
import it.unibz.inf.ontop.executor.union.LiftUnionAsHighAsPossibleProposalExecutor;
import it.unibz.inf.ontop.executor.union.UnionLiftInternalExecutorImpl;
import it.unibz.inf.ontop.executor.unsatisfiable.RemoveEmptyNodesExecutorImpl;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.executor.groundterm.GroundTermRemovalFromDataNodeExecutorImpl;
import it.unibz.inf.ontop.executor.pullout.PullVariableOutOfDataNodeExecutorImpl;
import it.unibz.inf.ontop.pivotalrepr.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.pivotalrepr.validation.StandardIntermediateQueryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(IntermediateQueryImpl.class);

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


    /**
     * TODO: explain
     */
    private static final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends StandardProposalExecutor>> STD_EXECUTOR_CLASSES;
    static {
        STD_EXECUTOR_CLASSES = ImmutableMap.<Class<? extends QueryOptimizationProposal>, Class<? extends StandardProposalExecutor>>of(
                LiftUnionAsHighAsPossibleProposal.class, LiftUnionAsHighAsPossibleProposalExecutor.class);
    }

    /**
     * TODO: explain
     */
    private static final ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>> INTERNAL_EXECUTOR_CLASSES;
    static {
        ImmutableMap.Builder<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>
                internalExecutorMapBuilder = ImmutableMap.builder();
        internalExecutorMapBuilder.put(InnerJoinOptimizationProposal.class, JoinInternalCompositeExecutor.class);
        internalExecutorMapBuilder.put(SubstitutionPropagationProposal.class, SubstitutionPropagationExecutorImpl.class);
        internalExecutorMapBuilder.put(PushDownBooleanExpressionProposal.class, PushDownExpressionExecutorImpl.class);
        internalExecutorMapBuilder.put(GroundTermRemovalFromDataNodeProposal.class, GroundTermRemovalFromDataNodeExecutorImpl.class);
        internalExecutorMapBuilder.put(PullVariableOutOfDataNodeProposal.class, PullVariableOutOfDataNodeExecutorImpl.class);
        internalExecutorMapBuilder.put(PullVariableOutOfSubTreeProposal.class, PullVariableOutOfSubTreeExecutorImpl.class);
        internalExecutorMapBuilder.put(RemoveEmptyNodeProposal.class, RemoveEmptyNodesExecutorImpl.class);
        internalExecutorMapBuilder.put(QueryMergingProposal.class, QueryMergingExecutorImpl.class);
        internalExecutorMapBuilder.put(UnionLiftProposal.class, UnionLiftInternalExecutorImpl.class);
        internalExecutorMapBuilder.put(TrueNodeRemovalProposal.class, TrueNodeRemovalExecutorImpl.class);
        INTERNAL_EXECUTOR_CLASSES = internalExecutorMapBuilder.build();
    }


    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    public IntermediateQueryImpl(MetadataForQueryOptimization metadata, DistinctVariableOnlyDataAtom projectionAtom,
                                 QueryTreeComponent treeComponent) {
        this.metadata = metadata;
        this.projectionAtom = projectionAtom;
        this.treeComponent = treeComponent;

        // TODO: disable it in production
        this.validate();
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
        return new IntermediateQueryImpl(metadata, projectionAtom, treeComponent.createSnapshot());
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
     * TODO: make this extensible by using Guice as a dependency-injection solution for loading arbitrary ProposalExecutor
     */
    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> R applyProposal(P proposal,
                                                       boolean requireUsingInternalExecutor)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        return applyProposal(proposal, requireUsingInternalExecutor, false);
    }

    @Override
    public <R extends ProposalResults, P extends QueryOptimizationProposal<R>> R applyProposal(P proposal,
                                                                                               boolean requireUsingInternalExecutor,
                                                                                               boolean disableValidationTests)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        if (!disableValidationTests) {
            // TODO: disable it in production
            validate();
        }

        /**
         * It assumes that the concrete proposal classes DIRECTLY
         * implements a registered interface (extending QueryOptimizationProposal).
         */
        Class<?>[] proposalClassHierarchy = proposal.getClass().getInterfaces();

        if (!requireUsingInternalExecutor) {
            /**
             * First look for a standard executor
             */
            for (Class proposalClass : proposalClassHierarchy) {
                if (STD_EXECUTOR_CLASSES.containsKey(proposalClass)) {
                    StandardProposalExecutor<P, R> executor;
                    try {
                        executor = STD_EXECUTOR_CLASSES.get(proposalClass).newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    return executor.apply(proposal, this);
                }
            }
        }

        /**
         * Then, look for a internal one
         */
        for (Class proposalClass : proposalClassHierarchy) {
            if (INTERNAL_EXECUTOR_CLASSES.containsKey(proposalClass)) {
                InternalProposalExecutor<P, R> executor;
                try {
                    executor = INTERNAL_EXECUTOR_CLASSES.get(proposalClass).newInstance();
                } catch (InstantiationException | IllegalAccessException e ) {
                    throw new RuntimeException(e.getMessage());
                }
                /**
                 * Has a SIDE-EFFECT on the tree component.
                 */
                R results = executor.apply(proposal, this, treeComponent);
                if (!disableValidationTests) {
                    // TODO: disable it in production
                    validate();
                }
                return results;
            }
        }

        if (requireUsingInternalExecutor) {
            throw new RuntimeException("No INTERNAL executor found for a proposal of the type " + proposal.getClass());
        }
        else {
            throw new RuntimeException("No executor found for a proposal of the type " + proposal.getClass());
        }
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

    public Variable renameAllVariablesWitSuffix() {
        return treeComponent.generateNewVariable();
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
        IntermediateQueryValidator validator = new StandardIntermediateQueryValidator();
        validator.validate(this);
    }
}
