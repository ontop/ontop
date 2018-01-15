package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryNodeException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "BindingAnnotationWithoutInject"})
public class ConstructionNodeImpl extends CompositeQueryNodeImpl implements ConstructionNode {

    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);
    @SuppressWarnings("FieldCanBeLocal")

    private final Optional<ImmutableQueryModifiers> optionalModifiers;
    private final TermNullabilityEvaluator nullabilityEvaluator;
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSubstitution<ImmutableTerm> substitution;
    private final ImmutableSet<Variable> childVariables;

    private final ImmutableUnificationTools unificationTools;
    private final ConstructionNodeTools constructionNodeTools;
    private final ImmutableSubstitutionTools substitutionTools;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";
    private final TermFactory termFactory;
    private final ValueConstant nullValue;
    private final ImmutabilityTools immutabilityTools;
    private final ExpressionEvaluator expressionEvaluator;

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                 @Assisted Optional<ImmutableQueryModifiers> optionalQueryModifiers,
                                 TermNullabilityEvaluator nullabilityEvaluator,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory, ImmutabilityTools immutabilityTools,
                                 ExpressionEvaluator expressionEvaluator) {
        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.optionalModifiers = optionalQueryModifiers;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.unificationTools = unificationTools;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionTools = substitutionTools;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.nullValue = termFactory.getNullConstant();
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
        this.expressionEvaluator = expressionEvaluator;
        this.childVariables = extractChildVariables(projectedVariables, substitution);

        validate();
    }

    private void validate() {
        ImmutableSet<Variable> substitutionDomain = substitution.getDomain();

        // The substitution domain must be a subset of the projectedVariables
        if (!projectedVariables.containsAll(substitutionDomain)) {
            throw new InvalidQueryNodeException("ConstructionNode: all the domain variables " +
                    "of the substitution must be projected.\n" + toString());
        }

        // The variables contained in the domain and in the range of the substitution must be disjoint
        if (substitutionDomain.stream()
                .anyMatch(childVariables::contains)) {
            throw new InvalidQueryNodeException("ConstructionNode: variables defined by the substitution cannot " +
                    "be used for defining other variables.\n" + toString());
        }

        // Substitution to non-projected variables is incorrect
        if (substitution.getImmutableMap().values().stream()
                .filter(v -> v instanceof Variable)
                .map(v -> (Variable) v)
                .anyMatch(v -> !projectedVariables.contains(v))) {
            throw new InvalidQueryNodeException(
                    "ConstructionNode: substituting a variable " +
                            "by a non-projected variable is incorrect.\n"
                + toString());
        }
    }

    /**
     * Without modifiers nor substitution.
     */
    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 TermNullabilityEvaluator nullabilityEvaluator,
                                 ImmutableUnificationTools unificationTools,
                                 ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 ImmutabilityTools immutabilityTools, ExpressionEvaluator expressionEvaluator) {
        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
        this.substitution = substitutionFactory.getSubstitution();
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
        this.expressionEvaluator = expressionEvaluator;
        this.optionalModifiers = Optional.empty();
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionFactory = substitutionFactory;
        this.nullValue = termFactory.getNullConstant();
        this.childVariables = extractChildVariables(projectedVariables, substitution);

        validate();
    }

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                 TermNullabilityEvaluator nullabilityEvaluator,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 ImmutabilityTools immutabilityTools, ExpressionEvaluator expressionEvaluator) {

        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.unificationTools = unificationTools;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionTools = substitutionTools;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
        this.expressionEvaluator = expressionEvaluator;
        this.optionalModifiers = Optional.empty();
        this.nullValue = termFactory.getNullConstant();
        this.childVariables = extractChildVariables(projectedVariables, substitution);

        validate();
    }

    private static ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> projectedVariables,
                                                          ImmutableSubstitution<ImmutableTerm> substitution) {
        ImmutableSet<Variable> variableDefinedByBindings = substitution.getDomain();

        Stream<Variable> variablesRequiredByBindings = substitution.getImmutableMap().values().stream()
                .flatMap(ImmutableTerm::getVariableStream);

        //return only the variables that are also used in the bindings for the child of the construction node
        return Stream.concat(projectedVariables.stream(), variablesRequiredByBindings)
                .filter(v -> !variableDefinedByBindings.contains(v))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getOptionalModifiers() {
        return optionalModifiers;
    }

    /**
     * Immutable fields, can be shared.
     */
    @Override
    public ConstructionNode clone() {
        return iqFactory.createConstructionNode(projectedVariables, substitution, optionalModifiers);
    }

    @Override
    public ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getChildVariables() {
        return childVariables;
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        collectedVariableBuilder.addAll(projectedVariables);

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm)term).getVariables());
            }
        }

        return collectedVariableBuilder.build();
    }

    /**
     * Merges the current substitution with the ascending one
     *
     * This substitution is obtained by composition and then cleaned (only defines the projected variables)
     *
     * Note that expects that the substitution does not rename a projected variable
     * into a non-projected one (this would produce an invalid construction node).
     * That is the responsibility of the SubstitutionPropagationExecutor
     * to prevent such bindings from appearing.
     *
     * TODO: simplify after getting rid of applyAscendingSubstitution()
     */
    private ImmutableSubstitution<ImmutableTerm> mergeWithAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitutionToApply) {
        ImmutableSubstitution<ImmutableTerm> localSubstitution = getSubstitution();
        ImmutableSet<Variable> boundVariables = localSubstitution.getImmutableMap().keySet();

        if (substitutionToApply.getImmutableMap().keySet().stream().anyMatch(boundVariables::contains)) {
            throw new IllegalArgumentException("An ascending substitution MUST NOT include variables bound by " +
                    "the substitution of the current construction node");
        }

        ImmutableSubstitution<ImmutableTerm> compositeSubstitution = substitutionToApply.composeWith(localSubstitution);

        /*
         * Cleans the composite substitution by removing non-projected variables
         */
        return compositeSubstitution
                .reduceDomainToIntersectionWith(projectedVariables)
                .normalizeValues();

    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (getChildVariables().contains(variable))
            return isChildVariableNullable(query, variable);

        return Optional.ofNullable(substitution.get(variable))
                .map(t -> isTermNullable(query, t))
                .orElseThrow(() -> new IllegalArgumentException("The variable " + variable + " is not projected by " + this));
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree child) {
        ImmutableSet<Variable> nullableChildVariables = child.getNullableVariables();


        return Stream.concat(
                getSubstitution().getImmutableMap().entrySet().stream()
                    .filter(e -> isNullable(e.getValue(), nullableChildVariables))
                    .map(Map.Entry::getKey),
                nullableChildVariables.stream()
        ).collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return substitution.isDefining(variable)
                || (getChildVariables().contains(variable) && child.isConstructed(variable));
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        if (!childVariables.contains(variable)) {
            return iqFactory.createUnaryIQTree(this, child);
        }

        IQTree newChild = child.liftIncompatibleDefinitions(variable);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the construction node
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        try {
            Optional<ImmutableExpression> childConstraint = computeChildConstraint(substitution, Optional.of(constraint));
            IQTree newChild = childConstraint
                    .map(child::propagateDownConstraint)
                    .orElse(child);
            return iqFactory.createUnaryIQTree(this, newChild);

        } catch (EmptyTreeException e) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        return transformer.transformConstruction(tree,this, child);
    }

    /**
     * TODO: involve the function to reduce the number of false positive
     */
    private boolean isNullable(ImmutableTerm term, ImmutableSet<Variable> nullableChildVariables) {
        if (term instanceof Constant)
            return term.equals(termFactory.getNullConstant());
        // TODO: improve this
        else if (term.isGround())
            return true;
        // TODO: improve this
        return term.getVariableStream()
                .anyMatch(nullableChildVariables::contains);
    }

    private boolean isChildVariableNullable(IntermediateQuery query, Variable variable) {
        return query.getFirstChild(this)
                .map(c -> c.isVariableNullable(query, variable))
                .orElseThrow(() -> new InvalidIntermediateQueryException(
                        "A construction node with child variables must have a child"));
    }

    private boolean isTermNullable(IntermediateQuery query, ImmutableTerm substitutionValue) {
        if (substitutionValue instanceof ImmutableFunctionalTerm) {
            ImmutableSet<Variable> nullableVariables = substitutionValue.getVariableStream()
                    .filter(v -> isChildVariableNullable(query, v))
                    .collect(ImmutableCollectors.toSet());
            return nullabilityEvaluator.isNullable(substitutionValue, nullableVariables);

        }
        else if (substitutionValue instanceof Constant) {
            return substitutionValue.equals(nullValue);
        }
        else if (substitutionValue instanceof Variable) {
            return isChildVariableNullable(query, (Variable)substitutionValue);
        }
        else {
            throw new IllegalStateException("Unexpected immutable term");
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .filter(n -> n.getVariables().equals(projectedVariables))
                .filter(n -> n.getSubstitution().equals(substitution))
                .filter(n -> n.getOptionalModifiers().equals(optionalModifiers))
                .isPresent();
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getChildVariables();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return substitution.getDomain();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        if (!(queryNode instanceof ConstructionNode))
            return false;
        ConstructionNode node = (ConstructionNode) queryNode;

        return projectedVariables.equals(node.getVariables())
                && substitution.equals(node.getSubstitution())
                && optionalModifiers.equals(node.getOptionalModifiers());
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + projectedVariables + " " + "[" + substitution + "]" ;
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree liftedChildIQTree = childIQTree.liftBinding(variableGenerator);
        QueryNode liftedChildRoot = liftedChildIQTree.getRootNode();
        if (liftedChildRoot instanceof ConstructionNode)
            return liftBinding((ConstructionNode) liftedChildRoot, (UnaryIQTree) liftedChildIQTree, currentIQProperties);
        else if (liftedChildIQTree.isDeclaredAsEmpty()) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
        else
            return iqFactory.createUnaryIQTree(this, liftedChildIQTree, currentIQProperties.declareLifted());
    }

    /**
     * TODO: refactor
     *
     * TODO: handle the constraint
     *
     */
    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> tau,
            Optional<ImmutableExpression> constraint, IQTree child) {

        if (optionalModifiers.isPresent()) {
            throw new RuntimeException("TODO: support query modifiers");
        }

        ImmutableSubstitution<NonFunctionalTerm> tauC = tau.getNonFunctionalTermFragment();
        ImmutableSubstitution<GroundFunctionalTerm> tauF = tau.getGroundFunctionalTermFragment();

        try {
            PropagationResults<NonFunctionalTerm> tauCPropagationResults = propagateTauC(tauC, child);
            PropagationResults<VariableOrGroundTerm> tauFPropagationResults = propagateTauF(tauF, tauCPropagationResults);

            Optional<FilterNode> filterNode = tauFPropagationResults.filter
                    .map(iqFactory::createFilterNode);

            Optional<ImmutableExpression> descendingConstraint = computeChildConstraint(tauFPropagationResults.theta,
                    constraint);

            IQTree newChild = Optional.of(tauFPropagationResults.delta)
                    .filter(delta -> !delta.isEmpty())
                    .map(delta -> child.applyDescendingSubstitution(delta, descendingConstraint))
                    .orElse(child);

            ImmutableSet<Variable> newProjectedVariables = constructionNodeTools.computeNewProjectedVariables(tau, projectedVariables);

            Optional<ConstructionNode> constructionNode = Optional.of(tauFPropagationResults.theta)
                    .filter(theta -> !(theta.isEmpty() && newProjectedVariables.equals(newChild.getVariables())))
                    .map(theta -> iqFactory.createConstructionNode(newProjectedVariables, theta));

            IQTree filterTree = filterNode
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, newChild))
                    .orElse(newChild);

            return constructionNode
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, filterTree))
                    .orElse(filterTree);

        } catch (EmptyTreeException e) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
    }

    private PropagationResults<NonFunctionalTerm> propagateTauC(ImmutableSubstitution<NonFunctionalTerm> tauC, IQTree child)
            throws EmptyTreeException {

        /* ---------------
         * tauC to thetaC
         * ---------------
         */

        ImmutableSubstitution<NonFunctionalTerm> thetaC = substitution.getNonFunctionalTermFragment();

        // Projected variables after propagating tauC
        ImmutableSet<Variable> vC = constructionNodeTools.computeNewProjectedVariables(tauC, projectedVariables);

        ImmutableSubstitution<NonFunctionalTerm> newEta = unificationTools.computeMGUS2(thetaC, tauC)
                .map(eta -> substitutionTools.prioritizeRenaming(eta, vC))
                .orElseThrow(EmptyTreeException::new);

        ImmutableSubstitution<NonFunctionalTerm> thetaCBar = substitutionFactory.getSubstitution(
                newEta.getImmutableMap().entrySet().stream()
                        .filter(e -> vC.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));

        ImmutableSubstitution<NonFunctionalTerm> deltaC = extractDescendingSubstitution(newEta,
                v -> v, thetaC, thetaCBar, projectedVariables);

        /* ---------------
         * deltaC to thetaF
         * ---------------
         */
        ImmutableSubstitution<ImmutableFunctionalTerm> thetaF = substitution.getFunctionalTermFragment();

        ImmutableMultimap<ImmutableTerm, ImmutableFunctionalTerm> m = thetaF.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        e -> deltaC.apply(e.getKey()),
                        e -> deltaC.applyToFunctionalTerm(e.getValue())));

        ImmutableSubstitution<ImmutableFunctionalTerm> thetaFBar = substitutionFactory.getSubstitution(
                m.asMap().entrySet().stream()
                .filter(e -> e.getKey() instanceof Variable)
                .filter(e -> !child.getVariables().contains(e.getKey()))
                .collect(ImmutableCollectors.toMap(
                        e -> (Variable) e.getKey(),
                        e -> e.getValue().iterator().next()
                )));


        ImmutableSubstitution<ImmutableTerm> gamma = extractDescendingSubstitution(deltaC,
                thetaFBar::apply,
                thetaF, thetaFBar,
                projectedVariables);
        ImmutableSubstitution<NonFunctionalTerm> newDeltaC = gamma.getNonFunctionalTermFragment();

        Optional<ImmutableExpression> f = computeF(m, thetaFBar, gamma, newDeltaC);

        return new PropagationResults<>(thetaCBar, thetaFBar, newDeltaC, f);

    }

    private Optional<ImmutableExpression> computeF(ImmutableMultimap<ImmutableTerm, ImmutableFunctionalTerm> m,
                                                   ImmutableSubstitution<ImmutableFunctionalTerm> thetaFBar,
                                                   ImmutableSubstitution<ImmutableTerm> gamma,
                                                   ImmutableSubstitution<NonFunctionalTerm> newDeltaC) {

        ImmutableSet<Map.Entry<Variable, ImmutableFunctionalTerm>> thetaFBarEntries = thetaFBar.getImmutableMap().entrySet();

        Stream<ImmutableExpression> thetaFRelatedExpressions = m.entries().stream()
                .filter(e -> !thetaFBarEntries.contains(e))
                .map(e -> createEquality(thetaFBar.apply(e.getKey()), e.getValue()));

        Stream<ImmutableExpression> blockedExpressions = gamma.getImmutableMap().entrySet().stream()
                .filter(e -> !newDeltaC.isDefining(e.getKey()))
                .map(e -> createEquality(e.getKey(), e.getValue()));

        return immutabilityTools.foldBooleanExpressions(Stream.concat(thetaFRelatedExpressions, blockedExpressions));
    }

    private PropagationResults<VariableOrGroundTerm> propagateTauF(ImmutableSubstitution<GroundFunctionalTerm> tauF,
                                                 PropagationResults<NonFunctionalTerm> tauCPropagationResults) {

        ImmutableSubstitution<ImmutableTerm> thetaBar = tauCPropagationResults.theta;

        ImmutableSubstitution<VariableOrGroundTerm> delta = substitutionFactory.getSubstitution(
                tauF.getImmutableMap().entrySet().stream()
                        .filter(e -> !thetaBar.isDefining(e.getKey()))
                        .filter(e -> !tauCPropagationResults.delta.isDefining(e.getKey()))
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> (VariableOrGroundTerm)e.getValue()
                        )))
                .composeWith2(tauCPropagationResults.delta);

        ImmutableSubstitution<ImmutableTerm> newTheta = substitutionFactory.getSubstitution(
                thetaBar.getImmutableMap().entrySet().stream()
                        .filter(e -> !tauF.isDefining(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));

        Stream<ImmutableExpression> newConditionStream =
                Stream.concat(
                        // tauF vs thetaBar
                        tauF.getImmutableMap().entrySet().stream()
                            .filter(e -> thetaBar.isDefining(e.getKey()))
                            .map(e -> createEquality(thetaBar.apply(e.getKey()), tauF.apply(e.getValue()))),
                        // tauF vs newDelta
                        tauF.getImmutableMap().entrySet().stream()
                                .filter(e -> tauCPropagationResults.delta.isDefining(e.getKey()))
                                .map(e -> createEquality(tauCPropagationResults.delta.apply(e.getKey()),
                                        tauF.apply(e.getValue()))));

        Optional<ImmutableExpression> newF = immutabilityTools.foldBooleanExpressions(Stream.concat(
                tauCPropagationResults.filter
                        .map(e -> e.flattenAND().stream())
                        .orElseGet(Stream::empty),
                newConditionStream));

        return new PropagationResults<>(newTheta, delta, newF);
    }

    private Optional<ImmutableExpression> computeChildConstraint(ImmutableSubstitution<ImmutableTerm> theta,
                                                                 Optional<ImmutableExpression> initialConstraint)
            throws EmptyTreeException {

        Optional<ExpressionEvaluator.EvaluationResult> descendingConstraintResults = initialConstraint
                .map(theta::applyToBooleanExpression)
                .map(exp -> expressionEvaluator.clone().evaluateExpression(exp));

        if (descendingConstraintResults
                .filter(ExpressionEvaluator.EvaluationResult::isEffectiveFalse)
                .isPresent())
            throw new EmptyTreeException();

        return descendingConstraintResults
                .flatMap(ExpressionEvaluator.EvaluationResult::getOptionalExpression);
    }

    /**
     * TODO: find a better name
     *
     */
    private <T extends ImmutableTerm> ImmutableSubstitution<T> extractDescendingSubstitution(
            ImmutableSubstitution<? extends NonFunctionalTerm> substitution,
            java.util.function.Function<NonFunctionalTerm, T> valueTransformationFct,
            ImmutableSubstitution<? extends ImmutableTerm> partialTheta,
            ImmutableSubstitution<? extends ImmutableTerm> newPartialTheta,
            ImmutableSet<Variable> originalProjectedVariables) {

        return substitutionFactory.getSubstitution(
                substitution.getImmutableMap().entrySet().stream()
                        .filter(e -> {
                            Variable v = e.getKey();
                            return (!partialTheta.isDefining(v))
                                    && ((!newPartialTheta.isDefining(v)) || originalProjectedVariables.contains(v));
                        })
                        .collect(ImmutableCollectors.toMap(
                               e -> e.getKey(),
                               e -> valueTransformationFct.apply(e.getValue())
                        )));
    }

    private ImmutableExpression createEquality(ImmutableTerm t1, ImmutableTerm t2) {
        return termFactory.getImmutableExpression(EQ, t1, t2);
    }

    private IQTree liftBinding(ConstructionNode childConstructionNode, UnaryIQTree childIQ, IQProperties currentIQProperties) {

        AscendingSubstitutionNormalization ascendingNormalization = normalizeAscendingSubstitution(
                childConstructionNode.getSubstitution().composeWith(substitution), projectedVariables);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = ascendingNormalization.getAscendingSubstitution();

        IQTree grandChildIQTree = ascendingNormalization.normalizeChild(childIQ.getChild());

        /*
         * TODO: what about updating the query modifiers? Clarify
         */
        Optional<ImmutableQueryModifiers> formerModifiers = getOptionalModifiers();
        if (formerModifiers.isPresent()) {
            Optional<ImmutableQueryModifiers> topModifiers = formerModifiers
                    .flatMap(m1 -> childConstructionNode.getOptionalModifiers()
                            .map(m2 -> ImmutableQueryModifiersImpl.merge(m1, m2))
                            .orElse(Optional.of(m1)));
            if (topModifiers.isPresent()) {
                ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                        newSubstitution, topModifiers);

                return iqFactory.createUnaryIQTree(newConstructionNode, grandChildIQTree, currentIQProperties.declareLifted());
            }
            /*
             * Not mergeable query modifiers --> keeps two nodes
             */
            else {
                ConstructionNode newTopConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                        newSubstitution, formerModifiers);

                ConstructionNode newChildConstructionNode = iqFactory.createConstructionNode(grandChildIQTree.getVariables(),
                        substitutionFactory.getSubstitution(), childConstructionNode.getOptionalModifiers());

                UnaryIQTree newChildIQ = iqFactory.createUnaryIQTree(newChildConstructionNode, grandChildIQTree,
                        currentIQProperties.declareLifted());
                return iqFactory.createUnaryIQTree(newTopConstructionNode, newChildIQ,
                        currentIQProperties.declareLifted());
            }
        }
        /*
         * No top query modifier
         */
        else {
            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                    newSubstitution, childConstructionNode.getOptionalModifiers());

            return iqFactory.createUnaryIQTree(newConstructionNode, grandChildIQTree, currentIQProperties.declareLifted());
        }
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> updateOptionalModifiers(
            Optional<ImmutableQueryModifiers> optionalModifiers,
            ImmutableSubstitution<? extends ImmutableTerm> substitution1,
            ImmutableSubstitution<? extends ImmutableTerm> substitution2) {
        if (!optionalModifiers.isPresent()) {
            return Optional.empty();
        }
        ImmutableQueryModifiers previousModifiers = optionalModifiers.get();
        ImmutableList.Builder<OrderCondition> conditionBuilder = ImmutableList.builder();

        for (OrderCondition condition : previousModifiers.getSortConditions()) {
            ImmutableTerm newTerm = substitution2.apply(substitution1.apply(condition.getVariable()));
            /**
             * If after applying the substitution the term is still a variable,
             * "updates" the OrderCondition.
             *
             * Otherwise, forgets it.
             */
            if (newTerm instanceof Variable) {
                conditionBuilder.add(condition.newVariable((Variable) newTerm));
            }
        }
        return previousModifiers.newSortConditions(conditionBuilder.build());
    }

    private class EmptyTreeException extends Exception {
    }

    public static class PropagationResults<T extends VariableOrGroundTerm> {

        public final ImmutableSubstitution<T> delta;
        public final Optional<ImmutableExpression> filter;
        public final ImmutableSubstitution<ImmutableTerm> theta;

        /**
         * After tauC propagation
         */
        PropagationResults(ImmutableSubstitution<NonFunctionalTerm> thetaCBar,
                           ImmutableSubstitution<ImmutableFunctionalTerm> thetaFBar,
                           ImmutableSubstitution<T> newDeltaC,
                           Optional<ImmutableExpression> f) {
            this.theta = thetaFBar.composeWith(thetaCBar);
            this.delta = newDeltaC;
            this.filter = f;
        }

        /**
         * After tauF propagation
         */
        PropagationResults(ImmutableSubstitution<ImmutableTerm> theta,
                           ImmutableSubstitution<T> delta,
                           Optional<ImmutableExpression> newF) {
            this.theta = theta;
            this.delta = delta;
            this.filter = newF;
        }
    }
}
