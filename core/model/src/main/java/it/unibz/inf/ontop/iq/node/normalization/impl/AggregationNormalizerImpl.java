package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.impl.AggregationNodeImpl.extractChildVariables;

public class AggregationNormalizerImpl implements AggregationNormalizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;

    @Inject
    protected AggregationNormalizerImpl(CoreSingletons coreSingletons,
                                        NotRequiredVariableRemover notRequiredVariableRemover) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
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
            IQTree newTree = iqFactory.createUnaryIQTree(
                    iqFactory.createDistinctNode(),
                    iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(aggregationNode.getGroupingVariables()),
                            child));

            return newTree.normalizeForOptimization(variableGenerator);
        }

        IQTree shrunkChild = notRequiredVariableRemover.optimize(child.normalizeForOptimization(variableGenerator),
                aggregationNode.getLocallyRequiredVariables(), variableGenerator);

        if (shrunkChild.isDeclaredAsEmpty()) {
            return normalizeEmptyChild(aggregationNode, normalizedTreeCache);
        }

        QueryNode rootNode = shrunkChild.getRootNode();

        // State after lifting the bindings
        AggregationNormalizationState stateAfterLiftingBindings = Optional.of(rootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(n -> normalizeWithChildConstructionNode(aggregationNode, n,
                        ((UnaryIQTree) shrunkChild).getChild(), variableGenerator))
                .orElseGet(() -> new AggregationNormalizationState(aggregationNode, null,
                        shrunkChild, variableGenerator, null));

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

        ConstructionNode constructionNode = iqFactory.createConstructionNode(projectedVariables, newSubstitution);

        return iqFactory.createUnaryIQTree(constructionNode, iqFactory.createTrueNode(), normalizedTreeCache);
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
        return new AggregationNormalizationState(aggregationNode, childConstructionNode, grandChild, variableGenerator, null)
                .propagateNonGroupingBindingsIntoToAggregationSubstitution()
                .liftGroupingBindings()
                .simplifyAggregationSubstitution();
    }


    protected class AggregationNormalizationState {

        private static final int MAX_ITERATIONS = 1000;
        private final ImmutableSet<Variable> groupingVariables;
        // NB: may not be always normalized (e.g. not starting with aggregation functional terms)
        private final Substitution<ImmutableFunctionalTerm> aggregationSubstitution;

        @Nullable
        private final FilterNode sampleFilter;
        @Nullable
        private final ConstructionNode childConstructionNode;
        private final IQTree grandChild;
        private final VariableGenerator variableGenerator;
        // The oldest ancestor is first
        private final ImmutableList<ConstructionNode> ancestors;

        /**
         * Initial state
         */
        protected AggregationNormalizationState(AggregationNode aggregationNode, @Nonnull ConstructionNode childConstructionNode,
                                                IQTree grandChild, VariableGenerator variableGenerator,
                                                @Nullable FilterNode sampleFilter) {

            this.groupingVariables = aggregationNode.getGroupingVariables();
            this.aggregationSubstitution = aggregationNode.getSubstitution();
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
            this.variableGenerator = variableGenerator;
            this.ancestors = ImmutableList.of();
            this.sampleFilter = sampleFilter;
        }

        private AggregationNormalizationState(ImmutableList<ConstructionNode> ancestors,
                                              ImmutableSet<Variable> groupingVariables,
                                              Substitution<ImmutableFunctionalTerm> aggregationSubstitution,
                                              @Nullable ConstructionNode childConstructionNode,
                                              IQTree grandChild, VariableGenerator variableGenerator,
                                              @Nullable FilterNode sampleFilter) {
            this.ancestors = ancestors;
            this.groupingVariables = groupingVariables;
            this.aggregationSubstitution = aggregationSubstitution;
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
            this.variableGenerator = variableGenerator;
            this.sampleFilter = sampleFilter;
        }

        /**
         * All the bindings of non-grouping variables in the child construction node are propagated to
         * the aggregation substitution
         */
        public AggregationNormalizationState propagateNonGroupingBindingsIntoToAggregationSubstitution() {
            if (childConstructionNode == null)
                return this;

            // NB: non grouping variables that are USED by the aggregation node (we can safely ignore the non-used ones)
            Sets.SetView<Variable> nonGroupingVariables = Sets.difference(
                    extractChildVariables(groupingVariables, aggregationSubstitution),
                    groupingVariables);

            Substitution<ImmutableTerm> nonGroupingSubstitution = childConstructionNode.getSubstitution()
                    .restrictDomainTo(nonGroupingVariables);

            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution =
                            nonGroupingSubstitution.compose(aggregationSubstitution).builder()
                                    .restrictDomainTo(aggregationSubstitution.getDomain())
                                    .transform(t -> (ImmutableFunctionalTerm)t)
                                    .build();

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(
                    groupingVariables,
                    newAggregationSubstitution);

            // Nullable
            ConstructionNode newChildConstructionNode = Optional.of(childConstructionNode.getSubstitution().restrictDomainTo(groupingVariables))
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(newAggregationNode.getChildVariables(), s))
                    .orElse(null);

            return new AggregationNormalizationState(ancestors, groupingVariables, newAggregationSubstitution,
                    newChildConstructionNode, grandChild, variableGenerator, sampleFilter);
        }

        /**
         * Lifts (fragments of) bindings that are injective.
         *
         * propagateNonGroupingBindingsIntoToAggregationSubstitution() is expected to have been called before
         *
         */
        public AggregationNormalizationState liftGroupingBindings() {
            if (childConstructionNode == null)
                return this;

            Substitution<ImmutableTerm> substitution = childConstructionNode.getSubstitution();

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

            ImmutableList<ConstructionNode> subStateAncestors = subState.getAncestors();

            ImmutableList<ConstructionNode> newAncestors = Stream.concat(
                    // Already defined
                    ancestors.stream(),
                    // Ancestors of the sub-state modified so as to project the aggregation variables
                    subStateAncestors.stream()
                            .map(a -> iqFactory.createConstructionNode(
                                    Sets.union(a.getVariables(), aggregationSubstitution.getDomain()).immutableCopy(),
                                    a.getSubstitution())))
                    .collect(ImmutableCollectors.toList());

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
                            subStateAncestors.get(subStateAncestors.size() - 1).getChildVariables(),
                            newAggregationSubstitution.getDomain())
                    .immutableCopy();

            Optional<Variable> sampleVariable = newGroupingVariables.isEmpty() && !groupingVariables.isEmpty()
                    ? Optional.of(variableGenerator.generateNewVariable("aggv"))
                    : Optional.empty();

            Substitution<ImmutableFunctionalTerm> finalAggregationSubstitution = sampleVariable.map(
                    s -> newAggregationSubstitution.compose(substitutionFactory.getSubstitution(sampleVariable.get(), termFactory.getDBSample(termFactory.getDBIntegerConstant(1), termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType())))
                            .builder()
                            .transform(t -> (ImmutableFunctionalTerm)t)
                            .build()
            ).orElse(newAggregationSubstitution);

            // Nullable
            // Is created if, either, the node includes a substitution, or a sample variable is required.
            ConstructionNode newChildConstructionNode = subState.getChildConstructionNode()
                    // Only keeps the child construction node if it has a substitution
                    .filter(n -> !n.getSubstitution().isEmpty())
                    .map(n -> iqFactory.createConstructionNode(
                            extractChildVariables(newGroupingVariables, finalAggregationSubstitution),
                            n.getSubstitution()))
                    .orElse(null);

            // Creates a filter over the sample variable so that only rows that have a non-null value in it are kept.
            Optional<FilterNode> newFilter = sampleVariable.map(s -> iqFactory.createFilterNode(termFactory.getDBIsNotNull(s)));

            return new AggregationNormalizationState(newAncestors, newGroupingVariables, finalAggregationSubstitution,
                    newChildConstructionNode,
                    subState.getGrandChildTree(), variableGenerator, newFilter.orElse(null));
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
            VariableNullability variableNullability = Optional.ofNullable(childConstructionNode)
                    .<IQTree>map(c -> iqFactory.createUnaryIQTree(c, grandChild,
                            iqFactory.createIQTreeCache(true)))
                    .orElse(grandChild)
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
                return new AggregationNormalizationState(
                        ancestors,
                        groupingVariables, newAggregationSubstitution,
                        childConstructionNode, grandChild, variableGenerator, sampleFilter);

            ConstructionNode liftedConstructionNode = iqFactory.createConstructionNode(
                    Sets.union(groupingVariables, aggregationSubstitution.getDomain()).immutableCopy(),
                   liftedSubstitution);

            ImmutableSet<Variable> newGroupingVariables = Sets.difference(liftedConstructionNode.getChildVariables(),
                    newAggregationSubstitution.getDomain()).immutableCopy();

            ImmutableList<ConstructionNode> newAncestors = Stream.concat(ancestors.stream(), Stream.of(liftedConstructionNode))
                            .collect(ImmutableCollectors.toList());

            return new AggregationNormalizationState(newAncestors, newGroupingVariables, newAggregationSubstitution,
                    childConstructionNode, grandChild, variableGenerator, sampleFilter);
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

            Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> optional = decomposeFunctionalTerm(arg);
            // Injective functional sub-term
            if (optional.isPresent())
                return optional.get();

            // Otherwise a fresh variable
            Variable var = variableGenerator.generateNewVariable();
            return termFactory.getFunctionalTermDecomposition(var, substitutionFactory.getSubstitution(var, arg));
        }


        protected IQTree createNormalizedTree(IQTreeCache normalizedTreeCache) {
            IQTree newChildTree = Optional.ofNullable(childConstructionNode)
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChild,
                            iqFactory.createIQTreeCache(true)))
                    .orElse(grandChild);

            AggregationNode aggregationNode = iqFactory.createAggregationNode(groupingVariables, aggregationSubstitution);

            UnaryIQTree aggregationTree = iqFactory.createUnaryIQTree(aggregationNode, newChildTree, normalizedTreeCache);

            IQTree baseTree = Optional.ofNullable(sampleFilter)
                    .map(f -> iqFactory.createUnaryIQTree(f, aggregationTree))
                    .orElse(aggregationTree);

            return ancestors.reverse().stream()
                    .reduce(baseTree,
                            (t, a) -> iqFactory.createUnaryIQTree(a, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); })
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }
    }

}
