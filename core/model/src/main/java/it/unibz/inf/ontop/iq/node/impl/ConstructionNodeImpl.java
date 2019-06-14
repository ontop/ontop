package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryNodeException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer.AscendingSubstitutionNormalization;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "BindingAnnotationWithoutInject"})
public class ConstructionNodeImpl extends CompositeQueryNodeImpl implements ConstructionNode {

    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);
    @SuppressWarnings("FieldCanBeLocal")

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
    private final Constant nullValue;
    private final AscendingSubstitutionNormalizer substitutionNormalizer;
    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 OntopModelSettings settings,
                                 AscendingSubstitutionNormalizer substitutionNormalizer,
                                 CoreUtilsFactory coreUtilsFactory) {
        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.unificationTools = unificationTools;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionTools = substitutionTools;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.nullValue = termFactory.getNullConstant();
        this.iqFactory = iqFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionNormalizer = substitutionNormalizer;
        this.childVariables = extractChildVariables(projectedVariables, substitution);

        if (settings.isTestModeEnabled())
            validateNode();
    }

    /**
     * Validates the node independently of its child
     */
    private void validateNode() throws InvalidQueryNodeException {
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
                                 ImmutableUnificationTools unificationTools,
                                 ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                 AscendingSubstitutionNormalizer substitutionNormalizer,
                                 CoreUtilsFactory coreUtilsFactory) {
        super(substitutionFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
        this.substitution = substitutionFactory.getSubstitution();
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionFactory = substitutionFactory;
        this.nullValue = termFactory.getNullConstant();
        this.childVariables = extractChildVariables(projectedVariables, substitution);
        this.substitutionNormalizer = substitutionNormalizer;
        this.coreUtilsFactory = coreUtilsFactory;

        validateNode();
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

    /**
     * Immutable fields, can be shared.
     */
    @Override
    public ConstructionNode clone() {
        return iqFactory.createConstructionNode(projectedVariables, substitution);
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

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (getChildVariables().contains(variable))
            return isChildVariableNullable(query, variable);

        return Optional.ofNullable(substitution.get(variable))
                .map(t -> isTermNullable(query, t))
                .orElseThrow(() -> new IllegalArgumentException("The variable " + variable + " is not projected by " + this));
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return child.getVariableNullability().update(substitution, projectedVariables);
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return substitution.isDefining(variable)
                || (getChildVariables().contains(variable) && child.isConstructed(variable));
    }

    /**
     * TODO: implement it seriously!
     */
    @Override
    public boolean isDistinct(IQTree child) {
        return false;
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
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            UnionNode newUnionNode = iqFactory.createUnionNode(getVariables());
            return iqFactory.createNaryIQTree(newUnionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        try {
            Optional<ImmutableExpression> childConstraint = computeChildConstraint(substitution, Optional.of(constraint),
                    child.getVariableNullability());
            IQTree newChild = childConstraint
                    .map(child::propagateDownConstraint)
                    .orElse(child);
            return iqFactory.createUnaryIQTree(this, newChild);

        } catch (EmptyTreeException e) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformConstruction(tree,this, child);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitConstruction(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidQueryNodeException, InvalidIntermediateQueryException {
        validateNode();

        ImmutableSet<Variable> requiredChildVariables = getChildVariables();

        ImmutableSet<Variable> childVariables = child.getVariables();

        if (!childVariables.containsAll(requiredChildVariables)) {
            throw new InvalidIntermediateQueryException("This child " + child
                    + " does not project all the variables " +
                    "required by the CONSTRUCTION node (" + requiredChildVariables + ")\n" + this);
        }
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> childDefs = child.getPossibleVariableDefinitions();

        if (childDefs.isEmpty()) {
            ImmutableSubstitution<NonVariableTerm> def = substitution.getNonVariableTermFragment();
            return def.isEmpty()
                    ? ImmutableSet.of()
                    : ImmutableSet.of(def);
        }

        return childDefs.stream()
                .map(childDef -> childDef.composeWith(substitution))
                .map(s -> s.reduceDomainToIntersectionWith(projectedVariables))
                .map(ImmutableSubstitution::getNonVariableTermFragment)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQProperties iqProperties) {
        IQTree newChild = child.removeDistincts();

        IQProperties newProperties = newChild.equals(child)
                ? iqProperties.declareDistinctRemovalWithoutEffect()
                : iqProperties.declareDistinctRemovalWithEffect();

        return iqFactory.createUnaryIQTree(this, newChild, newProperties);
    }

    /**
     * TODO: involve the function to reduce the number of false positive
     */
    private boolean isNullable(ImmutableTerm term, ImmutableSet<Variable> nullableChildVariables) {
        if (term instanceof Constant)
            return term.equals(termFactory.getNullConstant());
        // TODO: improve this
        else if (term.isGround())
            return false;
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
            return substitutionValue.isNullable(nullableVariables);

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
                && substitution.equals(node.getSubstitution());
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

    /**
     *  - Merges with a child construction
     *  - Removes itself if useless
     */
    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {

        IQTree liftedChild = child.normalizeForOptimization(variableGenerator);
        QueryNode liftedChildRoot = liftedChild.getRootNode();
        if (liftedChildRoot instanceof ConstructionNode)
            return mergeWithChild((ConstructionNode) liftedChildRoot, (UnaryIQTree) liftedChild, currentIQProperties);
        else if (liftedChild.isDeclaredAsEmpty()) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
        /*
         * If useless, returns the child
         */
        else if (liftedChild.getVariables().equals(projectedVariables))
            return liftedChild;
        else {
            ImmutableSubstitution<ImmutableTerm> newSubstitution = substitution.simplifyValues(liftedChild.getVariableNullability());
            ConstructionNode newConstructionNode = newSubstitution.equals(substitution)
                    ? this
                    : iqFactory.createConstructionNode(projectedVariables, newSubstitution);

            return iqFactory.createUnaryIQTree(newConstructionNode, liftedChild, currentIQProperties.declareNormalizedForOptimization());
        }
    }


    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child) {

        return applyDescendingSubstitution(descendingSubstitution, child,
                (c, r) -> propagateDescendingSubstitutionToChild(c, r, constraint));
    }

    /**
     *
     * TODO: better handle the constraint
     *
     * Returns the new child
     */
    private IQTree propagateDescendingSubstitutionToChild(IQTree child,
                                                          PropagationResults<VariableOrGroundTerm> tauFPropagationResults,
                                                          Optional<ImmutableExpression> constraint) throws EmptyTreeException {

        VariableNullability dummyVariableNullability = coreUtilsFactory.createDummyVariableNullability(
                child.getVariables().stream());

        Optional<ImmutableExpression> descendingConstraint = computeChildConstraint(tauFPropagationResults.theta,
                constraint, dummyVariableNullability);

        return Optional.of(tauFPropagationResults.delta)
                .filter(delta -> !delta.isEmpty())
                .map(delta -> child.applyDescendingSubstitution(delta, descendingConstraint))
                .orElse(child);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        return applyDescendingSubstitution(descendingSubstitution, child,
                (c, r) -> Optional.of(r.delta)
                        .filter(delta -> !delta.isEmpty())
                        .map(c::applyDescendingSubstitutionWithoutOptimizing)
                        .orElse(c));
    }

    private IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> tau, IQTree child,
            DescendingSubstitutionChildUpdateFunction updateChildFct) {

        ImmutableSet<Variable> newProjectedVariables = constructionNodeTools.computeNewProjectedVariables(tau, projectedVariables);

        ImmutableSubstitution<NonFunctionalTerm> tauC = tau.getNonFunctionalTermFragment();
        ImmutableSubstitution<GroundFunctionalTerm> tauF = tau.getGroundFunctionalTermFragment();

        try {
            PropagationResults<NonFunctionalTerm> tauCPropagationResults = propagateTauC(tauC, child);
            PropagationResults<VariableOrGroundTerm> tauFPropagationResults = propagateTauF(tauF, tauCPropagationResults);

            Optional<FilterNode> filterNode = tauFPropagationResults.filter
                    .map(iqFactory::createFilterNode);

            IQTree newChild = updateChildFct.apply(child, tauFPropagationResults);

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
            return iqFactory.createEmptyNode(newProjectedVariables);
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
                .map(e -> termFactory.getStrictEquality(thetaFBar.apply(e.getKey()), e.getValue()));

        Stream<ImmutableExpression> blockedExpressions = gamma.getImmutableMap().entrySet().stream()
                .filter(e -> !newDeltaC.isDefining(e.getKey()))
                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()));

        return termFactory.getConjunction(Stream.concat(thetaFRelatedExpressions, blockedExpressions));
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
                            .map(e -> termFactory.getStrictEquality(thetaBar.apply(e.getKey()), tauF.apply(e.getValue()))),
                        // tauF vs newDelta
                        tauF.getImmutableMap().entrySet().stream()
                                .filter(e -> tauCPropagationResults.delta.isDefining(e.getKey()))
                                .map(e -> termFactory.getStrictEquality(tauCPropagationResults.delta.apply(e.getKey()),
                                        tauF.apply(e.getValue()))));

        Optional<ImmutableExpression> newF = termFactory.getConjunction(Stream.concat(
                tauCPropagationResults.filter
                        .map(ImmutableExpression::flattenAND)
                        .orElseGet(Stream::empty),
                newConditionStream));

        return new PropagationResults<>(newTheta, delta, newF);
    }

    private Optional<ImmutableExpression> computeChildConstraint(ImmutableSubstitution<ImmutableTerm> theta,
                                                                 Optional<ImmutableExpression> initialConstraint,
                                                                 VariableNullability childVariableNullability)
            throws EmptyTreeException {

        Optional<ImmutableExpression.Evaluation> descendingConstraintResults = initialConstraint
                .map(theta::applyToBooleanExpression)
                .map(exp -> exp.evaluate(childVariableNullability));

        if (descendingConstraintResults
                .filter(ImmutableExpression.Evaluation::isEffectiveFalse)
                .isPresent())
            throw new EmptyTreeException();

        return descendingConstraintResults
                .flatMap(ImmutableExpression.Evaluation::getExpression);
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
                                Map.Entry::getKey,
                               e -> valueTransformationFct.apply(e.getValue())
                        )));
    }

    private IQTree mergeWithChild(ConstructionNode childConstructionNode, UnaryIQTree childIQ, IQProperties currentIQProperties) {

        IQTree grandChild = childIQ.getChild();

        AscendingSubstitutionNormalization ascendingNormalization = substitutionNormalizer.normalizeAscendingSubstitution(
                childConstructionNode.getSubstitution().composeWith(substitution).simplifyValues(grandChild.getVariableNullability()),
                projectedVariables
        );

        IQTree newGrandChild = ascendingNormalization.updateChild(grandChild);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = ascendingNormalization.getAscendingSubstitution();

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                newSubstitution);

        return newGrandChild.getVariables().equals(newConstructionNode.getVariables())
                ? newGrandChild
                : iqFactory.createUnaryIQTree(newConstructionNode, newGrandChild, currentIQProperties.declareNormalizedForOptimization());
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

    @FunctionalInterface
    private interface DescendingSubstitutionChildUpdateFunction {

        IQTree apply(IQTree child, PropagationResults<VariableOrGroundTerm> tauFPropagationResults)
                throws EmptyTreeException;

    }
}
