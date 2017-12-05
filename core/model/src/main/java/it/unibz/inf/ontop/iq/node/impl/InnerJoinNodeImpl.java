package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.*;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ConstructionNodeTools constructionNodeTools;

    @AssistedInject
    protected InnerJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition,
                                TermNullabilityEvaluator nullabilityEvaluator,
                                TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                                ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                                IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                ConstructionNodeTools constructionNodeTools) {
        super(optionalFilterCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools, substitutionFactory);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.constructionNodeTools = constructionNodeTools;
    }

    @AssistedInject
    private InnerJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                              TermNullabilityEvaluator nullabilityEvaluator,
                              TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                              ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                              ConstructionNodeTools constructionNodeTools) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools, substitutionFactory);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.constructionNodeTools = constructionNodeTools;
    }

    @AssistedInject
    private InnerJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                              TypeFactory typeFactory, DatalogTools datalogTools,
                              ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                              ConstructionNodeTools constructionNodeTools) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools, substitutionFactory);
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.constructionNodeTools = constructionNodeTools;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return new InnerJoinNodeImpl(getOptionalFilterCondition(), getNullabilityEvaluator(),
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), getImmutabilityTools(), iqFactory, substitutionFactory, constructionNodeTools);
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new InnerJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator(),
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), getImmutabilityTools(), iqFactory, substitutionFactory, constructionNodeTools);
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {

        if (substitution.isEmpty()) {
            return DefaultSubstitutionResults.noChange();
        }

        ImmutableSet<Variable> nullVariables = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue().equals(termFactory.getNullConstant()))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());


        ImmutableSet<Variable > otherNodesProjectedVariables = query.getOtherChildrenStream(this, childNode)
                .flatMap(c -> query.getVariables(c).stream())
                .collect(ImmutableCollectors.toSet());

        /*
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(nullVariables::contains)) {
            // Reject
            return DefaultSubstitutionResults.declareAsEmpty();
        }

        return computeAndEvaluateNewCondition(substitution, Optional.empty())
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> DefaultSubstitutionResults.noChange(substitution));
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return applyDescendingSubstitution(substitution);
    }

    private SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(substitution, cond))
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> DefaultSubstitutionResults.noChange(substitution));
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {

        if (isFilteringNullValue(variable))
            return false;

        // Non-already
        boolean alsoProjectedByAnotherChild = false;

        for(QueryNode child : query.getChildren(this)) {
            if (query.getVariables(child).contains(variable)) {
                // Joining conditions cannot be null
                if (alsoProjectedByAnotherChild)
                    return false;

                if (child.isVariableNullable(query, variable))
                    alsoProjectedByAnotherChild = true;
                else
                    return false;
            }
        }

        if (!alsoProjectedByAnotherChild)
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        return true;
    }

    private SubstitutionResults<InnerJoinNode> applyEvaluation(ExpressionEvaluator.EvaluationResult evaluationResult,
                                                               ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        if (evaluationResult.isEffectiveFalse()) {
            return DefaultSubstitutionResults.declareAsEmpty();
        }
        else {
            InnerJoinNode newNode = changeOptionalFilterCondition(evaluationResult.getOptionalExpression());
            return DefaultSubstitutionResults.newNode(newNode, substitution);
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof InnerJoinNode) &&
            this.getOptionalFilterCondition().equals(((InnerJoinNode) node).getOptionalFilterCondition());
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {

        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY,
                query.getVariables(this));
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        ImmutableList<QueryNode> remainingChildren = query.getChildrenStream(this)
                .filter(c -> c != trueChild)
                .collect(ImmutableCollectors.toList());
        switch (remainingChildren.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DECLARE_AS_TRUE, ImmutableSet.of());
            case 1:
                return getOptionalFilterCondition()
                        .map(immutableExpression -> new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                                query.getFactory().createFilterNode(immutableExpression),
                                ImmutableSet.of()))
                        .orElseGet(() -> new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD,
                                remainingChildren.get(0), ImmutableSet.of()));
            default:
                return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE, ImmutableSet.of());
        }
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: explain
     */
    @Override
    public IQTree liftBinding(ImmutableList<IQTree> initialChildren, VariableGenerator variableGenerator) {
        final ImmutableSet<Variable> projectedVariables = initialChildren.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        // Non-final
        ImmutableList<IQTree> currentChildren = initialChildren;
        ImmutableSubstitution<ImmutableTerm> currentSubstitution = substitutionFactory.getSubstitution();
        Optional<ImmutableExpression> currentJoiningCondition = getOptionalFilterCondition();
        boolean hasConverged = false;

        try {

            while (!hasConverged) {
                LiftingStepResults results = liftChildBinding(currentChildren, currentJoiningCondition, variableGenerator);
                hasConverged = results.hasConverged;
                currentChildren = results.children;
                currentSubstitution = results.substitution.composeWith(currentSubstitution);
                currentJoiningCondition = results.joiningCondition;
            }
            IQTree joinIQ = createJoinOrFilterOrTrue(currentChildren, currentJoiningCondition);

            ImmutableSubstitution<ImmutableTerm> ascendingSubstitution = substitutionFactory
                    .getSubstitution(currentSubstitution.getImmutableMap().entrySet().stream()
                            .filter(e -> projectedVariables.contains(e.getKey()))
                            .collect(ImmutableCollectors.toMap()));

            ImmutableSet<Variable> childrenVariables = currentChildren.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());

            /*
             * NB: creates a construction if a substitution needs to be propagated and/or if some variables
             * have to be projected away
             */
            return projectedVariables.equals(childrenVariables) && ascendingSubstitution.isEmpty()
                    ? joinIQ
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(projectedVariables, ascendingSubstitution), joinIQ, true);

        } catch (EmptyIQException e) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
    }

    /**
     * TODO: consider the constraint
     */
    @Override
    public IQTree applyDescendingSubstitution(VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children) {
        SubstitutionResults<InnerJoinNode> results = applyDescendingSubstitution(descendingSubstitution);

        InnerJoinNode joinNode;
        switch (results.getLocalAction()) {
            case NO_CHANGE:
                joinNode = this;
                break;
            case NEW_NODE:
                joinNode = results.getOptionalNewNode().get();
                break;
            case DECLARE_AS_EMPTY:
                return iqFactory.createEmptyNode(computeNewlyProjectedVariables(descendingSubstitution, children));
            default:
                throw new MinorOntopInternalBugException("Unexpected local action " +
                        "after applying a descending substitution to a inner join: " + results.getLocalAction());
        }

        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(c -> c.applyDescendingSubstitution(descendingSubstitution, constraint))
                .filter(c -> !(c instanceof TrueNode))
                .collect(ImmutableCollectors.toList());

        if (updatedChildren.stream()
                .anyMatch(IQTree::isDeclaredAsEmpty)) {
            return iqFactory.createEmptyNode(computeNewlyProjectedVariables(descendingSubstitution, children));
        }

        switch (updatedChildren.size()) {
            case 0:
                return iqFactory.createTrueNode();
            case 1:
                return updatedChildren.get(0);
            default:
                return iqFactory.createNaryIQTree(joinNode, updatedChildren);
        }
    }

    private ImmutableSet<Variable> computeNewlyProjectedVariables(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableList<IQTree> children) {
        ImmutableSet<Variable> formerProjectedVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        return constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, formerProjectedVariables);
    }

    /**
     * Lifts the binding OF AT MOST ONE child
     */
    private LiftingStepResults liftChildBinding(ImmutableList<IQTree> initialChildren,
                                                Optional<ImmutableExpression> initialJoiningCondition,
                                                VariableGenerator variableGenerator) throws EmptyIQException {
        ImmutableList<IQTree> liftedChildren = initialChildren.stream()
                .map(c -> c.liftBinding(variableGenerator))
                .filter(c -> !(c.getRootNode() instanceof TrueNode))
                .collect(ImmutableCollectors.toList());

        if (liftedChildren.stream()
                .anyMatch(IQTree::isDeclaredAsEmpty))
            throw new EmptyIQException();


        Optional<IQTree> optionalSelectedLiftedChild = liftedChildren.stream()
                .filter(iq -> iq.getRootNode() instanceof ConstructionNode)
                .findFirst();

        /*
         * No substitution to lift -> converged
         */
        if (!optionalSelectedLiftedChild.isPresent())
            return new InnerJoinNodeImpl.LiftingStepResults(substitutionFactory.getSubstitution(), liftedChildren,
                    initialJoiningCondition, true);

        UnaryIQTree selectedLiftedChild = (UnaryIQTree) optionalSelectedLiftedChild.get();

        ImmutableList<IQTree> otherLiftedChildren = liftedChildren.stream()
                .filter(c -> c != selectedLiftedChild)
                .collect(ImmutableCollectors.toList());

        ConstructionNode selectedChildConstructionNode = (ConstructionNode) selectedLiftedChild.getRootNode();
        IQTree selectedGrandChild = selectedLiftedChild.getChild();

        try {
            return liftSelectedChildBinding(selectedChildConstructionNode,
                    selectedGrandChild,
                    otherLiftedChildren, initialJoiningCondition, variableGenerator,
                    this::convertIntoLiftingStepResults);
        } catch (UnsatisfiableJoiningConditionException e) {
            throw new EmptyIQException();
        }
    }

    /**
     * TODO: should we try to preserve the children order?
     */
    private LiftingStepResults convertIntoLiftingStepResults(
            ImmutableList<IQTree> otherLiftedChildren, IQTree selectedGrandChild,
            Optional<ImmutableExpression> newCondition, ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        ImmutableList<IQTree> newChildren = Stream.concat(
                otherLiftedChildren.stream()
                        .map(c -> c.applyDescendingSubstitution(descendingSubstitution, newCondition)),
                Stream.of(selectedGrandChild))
                .collect(ImmutableCollectors.toList());

        return new LiftingStepResults(ascendingSubstitution, newChildren, newCondition, false);
    }



    private IQTree createJoinOrFilterOrTrue(ImmutableList<IQTree> currentChildren,
                                            Optional<ImmutableExpression> currentJoiningCondition) {
        switch (currentChildren.size()) {
            case 0:
                return iqFactory.createTrueNode();
            case 1:
                IQTree uniqueChild = currentChildren.get(0);
                return currentJoiningCondition
                        .map(e -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(e), uniqueChild))
                        .orElse(uniqueChild);
            default:
                InnerJoinNode newJoinNode = currentJoiningCondition.equals(getOptionalFilterCondition())
                        ? this
                        : changeOptionalFilterCondition(currentJoiningCondition);
                return iqFactory.createNaryIQTree(newJoinNode, currentChildren, true);
        }
    }



    private static class LiftingStepResults {
        public final ImmutableSubstitution<ImmutableTerm> substitution;
        public final ImmutableList<IQTree> children;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public final Optional<ImmutableExpression> joiningCondition;
        public final boolean hasConverged;

        private LiftingStepResults(ImmutableSubstitution<ImmutableTerm> substitution, ImmutableList<IQTree> children,
                                   Optional<ImmutableExpression> joiningCondition, boolean hasConverged) {
            this.substitution = substitution;
            this.children = children;
            this.joiningCondition = joiningCondition;
            this.hasConverged = hasConverged;
        }
    }


    private static class EmptyIQException extends Exception {
    }

 }
