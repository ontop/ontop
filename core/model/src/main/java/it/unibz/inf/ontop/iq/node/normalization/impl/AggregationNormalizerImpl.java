package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.AggregationFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class AggregationNormalizerImpl implements AggregationNormalizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected AggregationNormalizerImpl(CoreSingletons coreSingletons,
                                        NotRequiredVariableRemover notRequiredVariableRemover) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.notRequiredVariableRemover = notRequiredVariableRemover;
    }

    /**
     * Blocks distinct. May block some bindings and some filter conditions.
     *
     * TODO: enable lifting some filter conditions
     * TODO: we may consider remove distincts in the sub-tree when cardinality does not affect the substitution definitions
     */
    @Override
    public IQTree normalizeForOptimization(AggregationNode aggregationNode, IQTree child,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {
        IQTreeCache normalizedTreeCache = treeCache.declareAsNormalizedForOptimizationWithEffect();

        if (aggregationNode.getGroupingVariables().isEmpty() && aggregationNode.getSubstitution().isEmpty()) {
            return iqFactory.createTrueNode();
        }
        else if (aggregationNode.getSubstitution().isEmpty()) {
            IQTree newTree = iqTreeTools.createUnaryIQTree(
                    iqFactory.createDistinctNode(),
                    iqFactory.createConstructionNode(aggregationNode.getGroupingVariables()),
                    child);

            return newTree.normalizeForOptimization(variableGenerator);
        }

        IQTree shrunkChild = notRequiredVariableRemover.optimize(child.normalizeForOptimization(variableGenerator),
                aggregationNode.getLocallyRequiredVariables(), variableGenerator);

        if (shrunkChild.isDeclaredAsEmpty()) {
            return normalizeEmptyChild(aggregationNode, normalizedTreeCache);
        }

        // State after lifting the bindings
        AggregationNormalizationState stateAfterLiftingBindings = UnaryIQTreeDecomposition.of(shrunkChild, ConstructionNode.class)
                .map((n, t) -> normalizeWithChildConstructionNode(aggregationNode, n, t, variableGenerator))
                .orElseGet(() -> new AggregationNormalizationState(aggregationNode, Optional.empty(),
                        shrunkChild, variableGenerator, Optional.empty()));

        AggregationNormalizationState finalState = stateAfterLiftingBindings.simplifyAggregationSubstitution();
        // TODO: consider filters

        return finalState.createNormalizedTree(normalizedTreeCache);
    }

    /**
     * If the child is empty, returns no tuple if there are some grouping variables.
     * Otherwise, returns a single tuple.
     */
    private IQTree normalizeEmptyChild(AggregationNode aggregationNode, IQTreeCache normalizedTreeCache) {
        ImmutableSet<Variable> projectedVariables = aggregationNode.getVariables();

        if (!aggregationNode.getGroupingVariables().isEmpty())
            return iqFactory.createEmptyNode(projectedVariables);

        Substitution<ImmutableTerm> newSubstitution = aggregationNode.getSubstitution()
                .transform(this::simplifyEmptyAggregate);

        return iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(projectedVariables, newSubstitution),
                iqFactory.createTrueNode(), normalizedTreeCache);
    }

    private ImmutableTerm simplifyEmptyAggregate(ImmutableFunctionalTerm aggregateTerm) {
        FunctionSymbol functionSymbol = aggregateTerm.getFunctionSymbol();
        if (functionSymbol instanceof AggregationFunctionSymbol) {
            return ((AggregationFunctionSymbol) functionSymbol).evaluateEmptyBag(termFactory);
        }
        throw new MinorOntopInternalBugException("Was expecting an AggregationFunctionSymbol");
    }

    private AggregationNormalizationState normalizeWithChildConstructionNode(AggregationNode aggregationNode,
                                                                             ConstructionNode childConstructionNode,
                                                                             IQTree grandChild,
                                                                             VariableGenerator variableGenerator) {
        return new AggregationNormalizationState(aggregationNode, Optional.of(childConstructionNode), grandChild, variableGenerator, Optional.empty())
                .propagateNonGroupingBindingsIntoToAggregationSubstitution()
                .liftGroupingBindings()
                .simplifyAggregationSubstitution();
    }


    protected class AggregationNormalizationState {

        private static final int MAX_ITERATIONS = 1000;
        private final ImmutableSet<Variable> groupingVariables;
        // NB: may not be always normalized (e.g. not starting with aggregation functional terms)
        private final Substitution<ImmutableFunctionalTerm> aggregationSubstitution;

        private final Optional<FilterNode> sampleFilter;
        private final Optional<ConstructionNode> childConstructionNode;
        private final IQTree grandChild;
        private final VariableGenerator variableGenerator;
        // The oldest ancestor is first
        private final UnaryOperatorSequence<ConstructionNode> ancestors;

        /**
         * Initial state
         */
        private AggregationNormalizationState(AggregationNode aggregationNode, Optional<ConstructionNode> childConstructionNode,
                                                IQTree grandChild, VariableGenerator variableGenerator,
                                                Optional<FilterNode> sampleFilter) {

            this(UnaryOperatorSequence.of(),  aggregationNode.getGroupingVariables(), aggregationNode.getSubstitution(),
                    childConstructionNode, grandChild, variableGenerator, sampleFilter);
        }

        private AggregationNormalizationState(UnaryOperatorSequence<ConstructionNode> ancestors,
                                              ImmutableSet<Variable> groupingVariables,
                                              Substitution<ImmutableFunctionalTerm> aggregationSubstitution,
                                              Optional<ConstructionNode> childConstructionNode,
                                              IQTree grandChild, VariableGenerator variableGenerator,
                                              Optional<FilterNode> sampleFilter) {
            this.ancestors = ancestors;
            this.groupingVariables = groupingVariables;
            this.aggregationSubstitution = aggregationSubstitution;
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
            this.variableGenerator = variableGenerator;
            this.sampleFilter = sampleFilter;
        }

        private AggregationNormalizationState update(UnaryOperatorSequence<ConstructionNode> ancestors,
                                              ImmutableSet<Variable> groupingVariables,
                                              Substitution<ImmutableFunctionalTerm> aggregationSubstitution,
                                              Optional<ConstructionNode> childConstructionNode,
                                              IQTree grandChild,
                                              Optional<FilterNode> sampleFilter) {

            return new AggregationNormalizationState(ancestors, groupingVariables, aggregationSubstitution, childConstructionNode, grandChild, variableGenerator, sampleFilter);
        }

            /**
             * All the bindings of non-grouping variables in the child construction node are propagated to
             * the aggregation substitution
             */
        public AggregationNormalizationState propagateNonGroupingBindingsIntoToAggregationSubstitution() {
            if (childConstructionNode.isEmpty())
                return this;

            // NB: non-grouping variables that are USED by the aggregation node (we can safely ignore the non-used ones)
            Set<Variable> nonGroupingVariables = Sets.difference(
                    iqTreeTools.extractChildVariables(groupingVariables, aggregationSubstitution),
                    groupingVariables);

            Substitution<ImmutableTerm> nonGroupingSubstitution = childConstructionNode.get().getSubstitution()
                    .restrictDomainTo(nonGroupingVariables);

            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution =
                            nonGroupingSubstitution.compose(aggregationSubstitution).builder()
                                    .restrictDomainTo(aggregationSubstitution.getDomain())
                                    .transform(t -> (ImmutableFunctionalTerm)t)
                                    .build();

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(
                    groupingVariables,
                    newAggregationSubstitution);

            Optional<ConstructionNode> newChildConstructionNode = childConstructionNode
                    .map(ConstructionNode::getSubstitution)
                    .map(s -> s.restrictDomainTo(groupingVariables))
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(newAggregationNode.getChildVariables(), s));

            return update(ancestors, groupingVariables, newAggregationSubstitution,
                    newChildConstructionNode, grandChild, sampleFilter);
        }

        /**
         * Lifts (fragments of) bindings that are injective.
         *
         * propagateNonGroupingBindingsIntoToAggregationSubstitution() is expected to have been called before
         *
         */
        public AggregationNormalizationState liftGroupingBindings() {
            if (childConstructionNode.isEmpty())
                return this;

            Substitution<ImmutableTerm> substitution = childConstructionNode.get().getSubstitution();
            if (substitution.isEmpty())
                return this;

            if (!groupingVariables.containsAll(substitution.getDomain()))
                throw new MinorOntopInternalBugException("Was expecting all the non-grouping bindings to be lifted");


            // Only projecting grouping variables
            // (mimicking the special case when GROUP BY reduces itself to a DISTINCT and a projection)
            ConstructionNode groupingConstructionNode = iqFactory.createConstructionNode(groupingVariables, substitution);

            // Non-final
            InjectiveBindingLiftState subState = new InjectiveBindingLiftState(groupingConstructionNode, grandChild,
                    variableGenerator, coreSingletons);

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                InjectiveBindingLiftState newSubState = subState.liftBindings();

                // Convergence
                if (newSubState.equals(subState)) {
                    return convertIntoState(subState);
                }
                else
                    subState = newSubState;
            }
            throw new MinorOntopInternalBugException("AggregationNormalizerImpl.liftGroupingBindings() " +
                    "did not converge after " + MAX_ITERATIONS);
        }

        private AggregationNormalizationState convertIntoState(InjectiveBindingLiftState subState) {

            UnaryOperatorSequence<ConstructionNode> subStateAncestors = subState.getAncestors();

            UnaryOperatorSequence<ConstructionNode> newAncestors = ancestors.append(
                    // Ancestors of the sub-state modified so as to project the aggregation variables
                    subStateAncestors.stream()
                            .map(a -> iqFactory.createConstructionNode(
                                    Sets.union(a.getVariables(), aggregationSubstitution.getDomain()).immutableCopy(),
                                    a.getSubstitution())));

            // Applies all the substitutions of the ancestors to the substitution of the aggregation node
            // Needed when some grouping variables are also used in the aggregates
            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = subStateAncestors.stream()
                    .reduce(aggregationSubstitution,
                            (s, a) -> a.getSubstitution()
                                            .compose(s)
                                            .builder()
                                            .restrictDomainTo(aggregationSubstitution.getDomain())
                                            .transform(t -> (ImmutableFunctionalTerm)t)
                                            .build(),
                            (s1, s2) -> {
                                throw new MinorOntopInternalBugException("Substitution merging was not expected");
                            });

            // The closest parent informs us about the new grouping variables
            ImmutableSet<Variable> newGroupingVariables = subStateAncestors.isEmpty()
                    ? groupingVariables
                    : Sets.difference(
                            subStateAncestors.getLast().getChildVariables(),
                            newAggregationSubstitution.getDomain())
                    .immutableCopy();

            Optional<Variable> sampleVariable = newGroupingVariables.isEmpty() && !groupingVariables.isEmpty()
                    ? Optional.of(variableGenerator.generateNewVariable("aggv"))
                    : Optional.empty();

            Substitution<ImmutableFunctionalTerm> finalAggregationSubstitution = sampleVariable.map(
                    s -> newAggregationSubstitution.compose(
                            substitutionFactory.getSubstitution(sampleVariable.get(),
                                    termFactory.getDBSample(termFactory.getDBIntegerConstant(1),
                                            termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType())))
                            .builder()
                            .transform(t -> (ImmutableFunctionalTerm)t)
                            .build()
            ).orElse(newAggregationSubstitution);

            // Is created if, either, the node includes a substitution, or a sample variable is required.
            Optional<ConstructionNode> newChildConstructionNode = subState.getChildConstructionNode()
                    // Only keeps the child construction node if it has a substitution
                    .filter(n -> !n.getSubstitution().isEmpty())
                    .map(n -> iqFactory.createConstructionNode(
                            iqTreeTools.extractChildVariables(newGroupingVariables, finalAggregationSubstitution),
                            n.getSubstitution()));

            // Creates a filter over the sample variable so that only rows that have a non-null value in it are kept.
            Optional<FilterNode> newFilter = sampleVariable.map(s -> iqFactory.createFilterNode(termFactory.getDBIsNotNull(s)));

            return update(newAncestors, newGroupingVariables, finalAggregationSubstitution,
                    newChildConstructionNode,
                    subState.getGrandChildTree(), newFilter);
        }

        /**
         * Simplifies the substitution of the aggregation node and partially lift some bindings
         * so as to guarantee that all the values of the substitution are functional terms using
         * an aggregation function symbol.
         */
        public AggregationNormalizationState simplifyAggregationSubstitution() {
            // NB: use ImmutableSubstitution.simplifyValues()
            // NB: look at FunctionSymbol.isAggregation()

            // Taken from the child sub-tree
            VariableNullability variableNullability = iqTreeTools.createOptionalUnaryIQTree(childConstructionNode, grandChild)
                    .getVariableNullability();

            // The simplification may do the "lifting" inside the functional term (having a non-aggregation
            // functional term above the aggregation one)
            Substitution<ImmutableTerm> simplifiedSubstitution = aggregationSubstitution
                    .transform(t -> t.simplify(variableNullability));

            ImmutableMap<Variable, ImmutableFunctionalTerm.FunctionalTermDecomposition> decompositionMap =
                    simplifiedSubstitution.builder()
                            .restrictRangeTo(ImmutableFunctionalTerm.class)
                            .toMapIgnoreOptional((v, t) -> decomposeFunctionalTerm(t));

            Substitution<ImmutableTerm> liftedSubstitution = substitutionFactory.union(
                    // All variables and constants
                    simplifiedSubstitution.<ImmutableTerm>restrictRangeTo(NonFunctionalTerm.class),
                    // (Possibly decomposed) functional terms
                    simplifiedSubstitution.builder()
                            .<ImmutableTerm>restrictRangeTo(ImmutableFunctionalTerm.class)
                            .transformOrRemove(decompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                            .build());

            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = simplifiedSubstitution.builder()
                    .restrictRangeTo(ImmutableFunctionalTerm.class)
                    .flatTransform(decompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                    .build();

            if (liftedSubstitution.isEmpty())
                return update(
                        ancestors,
                        groupingVariables, newAggregationSubstitution,
                        childConstructionNode, grandChild, sampleFilter);

            ConstructionNode liftedConstructionNode = iqFactory.createConstructionNode(
                    Sets.union(groupingVariables, aggregationSubstitution.getDomain()).immutableCopy(),
                   liftedSubstitution);

            ImmutableSet<Variable> newGroupingVariables = Sets.difference(liftedConstructionNode.getChildVariables(),
                    newAggregationSubstitution.getDomain()).immutableCopy();

            return update(
                    ancestors.append(liftedConstructionNode),
                    newGroupingVariables, newAggregationSubstitution,
                    childConstructionNode, grandChild, sampleFilter);
        }

        /**
         * Decomposes functional terms so as to lift non-aggregation function symbols above and block
         * the aggregation functional terms
         */
        protected Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> decomposeFunctionalTerm(
                ImmutableFunctionalTerm functionalTerm) {

            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if (functionSymbol.isAggregation())
                return Optional.empty();

            ImmutableList<? extends ImmutableTerm> arguments = functionalTerm.getTerms();

            // One entry per functional sub-term
            ImmutableMap<Integer, ImmutableFunctionalTerm.FunctionalTermDecomposition> subTermDecompositions =
                    IntStream.range(0, arguments.size())
                    .filter(i -> arguments.get(i) instanceof ImmutableFunctionalTerm)
                    .boxed()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            // Recursive
                            i -> getFunctionalTermDecomposition((ImmutableFunctionalTerm) arguments.get(i))));

            ImmutableList<ImmutableTerm> newArguments = IntStream.range(0, arguments.size())
                    .mapToObj(i -> Optional.ofNullable(subTermDecompositions.get(i))
                            .map(ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                            // Previous argument when non-functional
                            .orElseGet(() -> arguments.get(i)))
                    .collect(ImmutableCollectors.toList());

            ImmutableFunctionalTerm newFunctionalTerm = termFactory.getImmutableFunctionalTerm(functionSymbol, newArguments);

            Substitution<ImmutableFunctionalTerm> subTermSubstitution = subTermDecompositions.values().stream()
                    .map(ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                    .reduce(substitutionFactory.getSubstitution(), substitutionFactory::union);

            return Optional.of(termFactory.getFunctionalTermDecomposition(newFunctionalTerm, subTermSubstitution));
        }

        private ImmutableFunctionalTerm.FunctionalTermDecomposition getFunctionalTermDecomposition(ImmutableFunctionalTerm arg)  {
            return decomposeFunctionalTerm(arg)
                    .orElseGet(() -> {
                        Variable var = variableGenerator.generateNewVariable();
                        return termFactory.getFunctionalTermDecomposition(var, substitutionFactory.getSubstitution(var, arg));});
        }


        protected IQTree createNormalizedTree(IQTreeCache normalizedTreeCache) {
            IQTree newChildTree = iqTreeTools.createOptionalUnaryIQTree(childConstructionNode, grandChild);

            UnaryIQTree aggregationTree = iqFactory.createUnaryIQTree(
                    iqFactory.createAggregationNode(groupingVariables, aggregationSubstitution),
                    newChildTree, normalizedTreeCache);

            IQTree baseTree = iqTreeTools.createOptionalUnaryIQTree(sampleFilter, aggregationTree);

            return iqTreeTools.createAncestorsUnaryIQTree(ancestors, baseTree)
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }
    }

}
