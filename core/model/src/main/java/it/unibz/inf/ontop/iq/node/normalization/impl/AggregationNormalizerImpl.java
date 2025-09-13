package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.AggregationFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class AggregationNormalizerImpl implements AggregationNormalizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected AggregationNormalizerImpl(CoreSingletons coreSingletons,
                                        NotRequiredVariableRemover notRequiredVariableRemover) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
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

        if (aggregationNode.getSubstitution().isEmpty()) {
            if (aggregationNode.getGroupingVariables().isEmpty()) {
                return iqFactory.createTrueNode();
            }
            else {
                IQTree newTree = iqTreeTools.unaryIQTreeBuilder()
                        .append(iqFactory.createDistinctNode())
                        .append(iqFactory.createConstructionNode(aggregationNode.getGroupingVariables()))
                        .build(child);

                return newTree.normalizeForOptimization(variableGenerator);
            }
        }

        IQTreeCache normalizedTreeCache = treeCache.declareAsNormalizedForOptimizationWithEffect();

        IQTree shrunkChild = notRequiredVariableRemover.optimize(
                child.normalizeForOptimization(variableGenerator),
                aggregationNode.getLocallyRequiredVariables(), variableGenerator);

        if (shrunkChild.isDeclaredAsEmpty()) {
            if (!aggregationNode.getGroupingVariables().isEmpty())
                return iqFactory.createEmptyNode(aggregationNode.getVariables());

            Substitution<ImmutableTerm> newSubstitution = aggregationNode.getSubstitution()
                    .transform(this::simplifyEmptyAggregate);

            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(aggregationNode.getVariables(), newSubstitution),
                    iqFactory.createTrueNode(),
                    normalizedTreeCache);
        }

        return new Context(variableGenerator, normalizedTreeCache)
                .normalize(shrunkChild, aggregationNode);
    }

    private ImmutableTerm simplifyEmptyAggregate(ImmutableFunctionalTerm aggregateTerm) {
        FunctionSymbol functionSymbol = aggregateTerm.getFunctionSymbol();
        if (functionSymbol instanceof AggregationFunctionSymbol) {
            return ((AggregationFunctionSymbol) functionSymbol).evaluateEmptyBag(termFactory);
        }
        throw new MinorOntopInternalBugException("Was expecting an AggregationFunctionSymbol");
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class AggregationSubTree {
        private final Optional<FilterNode> sampleFilter;

        private final ImmutableSet<Variable> groupingVariables;
        // NB: may not be always normalized (e.g. not starting with aggregation functional terms)
        private final Substitution<ImmutableFunctionalTerm> aggregationSubstitution;

        private final Optional<ConstructionNode> childConstructionNode;
        private final IQTree grandChild;

        AggregationSubTree(Optional<FilterNode> sampleFilter, ImmutableSet<Variable> groupingVariables, Substitution<ImmutableFunctionalTerm> aggregationSubstitution, Optional<ConstructionNode> childConstructionNode, IQTree grandChild) {
            this.sampleFilter = sampleFilter;
            this.groupingVariables = groupingVariables;
            this.aggregationSubstitution = aggregationSubstitution;
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
        }

        /**
         * Initial state
         */
        AggregationSubTree(AggregationNode aggregationNode, Optional<ConstructionNode> childConstructionNode, IQTree grandChild) {
            this(Optional.empty(), aggregationNode.getGroupingVariables(), aggregationNode.getSubstitution(),
                    childConstructionNode, grandChild);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof AggregationSubTree) {
                AggregationSubTree other = (AggregationSubTree) o;
                return sampleFilter.equals(other.sampleFilter)
                        && groupingVariables.equals(other.groupingVariables)
                        && aggregationSubstitution.equals(other.aggregationSubstitution)
                        && childConstructionNode.equals(other.childConstructionNode)
                        && grandChild.equals(other.grandChild);
            }
            return false;
        }
    }

    private class Context extends InjectiveBindingLiftContext {

        private static final int MAX_ITERATIONS = 1000;

        public Context(VariableGenerator variableGenerator, IQTreeCache normalizedTreeCache) {
            super(variableGenerator, coreSingletons, normalizedTreeCache);
        }

        IQTree normalize(IQTree shrunkChild, AggregationNode aggregationNode) {
            var construction = UnaryIQTreeDecomposition.of(shrunkChild, ConstructionNode.class);
            State<ConstructionNode, AggregationSubTree> stateAfterLiftingBindings;
            if (construction.isPresent()) {
                stateAfterLiftingBindings = liftGroupingBindings(
                        propagateNonGroupingBindingsIntoToAggregationSubstitution(
                                new State<>(new AggregationSubTree(aggregationNode, construction.getOptionalNode(), construction.getChild()))));
            }
            else {
                stateAfterLiftingBindings = new State<>(new AggregationSubTree(aggregationNode, Optional.empty(), shrunkChild));
            }

            State<ConstructionNode, AggregationSubTree> finalState = simplifyAggregationSubstitution(stateAfterLiftingBindings);
            // TODO: consider filters

            return asIQTree(finalState);
        }


        /**
         * All the bindings of non-grouping variables in the child construction node are propagated to
         * the aggregation substitution
         */
        State<ConstructionNode, AggregationSubTree> propagateNonGroupingBindingsIntoToAggregationSubstitution(State<ConstructionNode, AggregationSubTree> state) {
            if (state.getSubTree().childConstructionNode.isEmpty())
                return state;

            // NB: non-grouping variables that are USED by the aggregation node (we can safely ignore the non-used ones)
            Set<Variable> nonGroupingVariables = Sets.difference(
                    iqTreeTools.extractChildVariables(state.getSubTree().groupingVariables, state.getSubTree().aggregationSubstitution),
                    state.getSubTree().groupingVariables);

            Substitution<ImmutableTerm> substitution = state.getSubTree().childConstructionNode.get().getSubstitution();
            Substitution<ImmutableTerm> nonGroupingSubstitution = substitution.restrictDomainTo(nonGroupingVariables);

            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution =
                    nonGroupingSubstitution.compose(state.getSubTree().aggregationSubstitution).builder()
                            .restrictDomainTo(state.getSubTree().aggregationSubstitution.getDomain())
                            .transform(t -> (ImmutableFunctionalTerm) t)
                            .build();

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(
                    state.getSubTree().groupingVariables,
                    newAggregationSubstitution);

            Optional<ConstructionNode> newChildConstructionNode = iqTreeTools.createOptionalConstructionNode(
                    newAggregationNode::getChildVariables, substitution.restrictDomainTo(state.getSubTree().groupingVariables));

            return state.of(new AggregationSubTree(state.getSubTree().sampleFilter, state.getSubTree().groupingVariables,
                    newAggregationSubstitution, newChildConstructionNode, state.getSubTree().grandChild));
        }

        /**
         * Lifts (fragments of) bindings that are injective.
         * <p>
         * propagateNonGroupingBindingsIntoToAggregationSubstitution() is expected to have been called before
         */
        State<ConstructionNode, AggregationSubTree> liftGroupingBindings(State<ConstructionNode, AggregationSubTree> state) {
            if (state.getSubTree().childConstructionNode.isEmpty())
                return state;

            Substitution<ImmutableTerm> substitution = state.getSubTree().childConstructionNode.get().getSubstitution();
            if (substitution.isEmpty())
                return state;

            if (!state.getSubTree().groupingVariables.containsAll(substitution.getDomain()))
                throw new MinorOntopInternalBugException("Was expecting all the non-grouping bindings to be lifted");

            // Only projecting grouping variables
            // (mimicking the special case when GROUP BY reduces itself to a DISTINCT and a projection)
            ConstructionNode groupingConstructionNode = iqFactory.createConstructionNode(state.getSubTree().groupingVariables, substitution);

            State<ConstructionNode, ConstructionSubTree> subState = IQStateOptionalTransformer.reachFinalState(
                    new State<>(new ConstructionSubTree(groupingConstructionNode, state.getSubTree().grandChild)),
                    this::liftBindings,
                    MAX_ITERATIONS);

            UnaryOperatorSequence<ConstructionNode> subStateAncestors = subState.getAncestors();

            UnaryOperatorSequence<ConstructionNode> newAncestors = state.getAncestors().append(
                    // Ancestors of the sub-state modified so as to project the aggregation variables
                    subStateAncestors.stream()
                            .map(a -> iqFactory.createConstructionNode(
                                    Sets.union(a.getVariables(), state.getSubTree().aggregationSubstitution.getDomain()).immutableCopy(),
                                    a.getSubstitution())));

            // Applies all the substitutions of the ancestors to the substitution of the aggregation node
            // Needed when some grouping variables are also used in the aggregates
            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = subStateAncestors.stream()
                    .reduce(state.getSubTree().aggregationSubstitution,
                            (s, a) -> a.getSubstitution()
                                    .compose(s)
                                    .builder()
                                    .restrictDomainTo(state.getSubTree().aggregationSubstitution.getDomain())
                                    .transform(t -> (ImmutableFunctionalTerm) t)
                                    .build(),
                            (s1, s2) -> {
                                throw new MinorOntopInternalBugException("Substitution merging was not expected");
                            });

            // The closest parent informs us about the new grouping variables
            ImmutableSet<Variable> newGroupingVariables = subStateAncestors.isEmpty()
                    ? state.getSubTree().groupingVariables
                    : Sets.difference(
                            subStateAncestors.getLast().getChildVariables(),
                            newAggregationSubstitution.getDomain())
                    .immutableCopy();

            Optional<Variable> sampleVariable = newGroupingVariables.isEmpty() && !state.getSubTree().groupingVariables.isEmpty()
                    ? Optional.of(variableGenerator.generateNewVariable("aggv"))
                    : Optional.empty();

            Substitution<ImmutableFunctionalTerm> finalAggregationSubstitution = sampleVariable.map(
                    s -> substitutionFactory.onImmutableFunctionalTerms().compose(newAggregationSubstitution,
                            substitutionFactory.getSubstitution(sampleVariable.get(),
                                    termFactory.getDBSample(termFactory.getDBIntegerConstant(1),
                                            termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType())))
            ).orElse(newAggregationSubstitution);

            // Is created if, either, the node includes a substitution, or a sample variable is required.
            Optional<ConstructionNode> newChildConstructionNode = subState.getSubTree().getOptionalConstructionNode()
                    // Only keeps the child construction node if it has a substitution
                    .flatMap(n -> iqTreeTools.createOptionalConstructionNode(
                            () -> iqTreeTools.extractChildVariables(newGroupingVariables, finalAggregationSubstitution),
                            n.getSubstitution()));

            // Creates a filter over the sample variable so that only rows that have a non-null value in it are kept.
            Optional<FilterNode> newFilter = iqTreeTools.createOptionalFilterNode(sampleVariable.map(termFactory::getDBIsNotNull));

            return new State<>(newAncestors, new AggregationSubTree(newFilter, newGroupingVariables,
                    finalAggregationSubstitution, newChildConstructionNode, subState.getSubTree().getChild()));
        }

        /**
         * Simplifies the substitution of the aggregation node and partially lift some bindings
         * so as to guarantee that all the values of the substitution are functional terms using
         * an aggregation function symbol.
         */
        State<ConstructionNode, AggregationSubTree> simplifyAggregationSubstitution(State<ConstructionNode, AggregationSubTree> state) {
            // NB: use ImmutableSubstitution.simplifyValues()
            // NB: look at FunctionSymbol.isAggregation()

            // Taken from the child sub-tree
            VariableNullability variableNullability = iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getSubTree().childConstructionNode)
                    .build(state.getSubTree().grandChild)
                    .getVariableNullability();

            // The simplification may do the "lifting" inside the functional term (having a non-aggregation
            // functional term above the aggregation one)
            Substitution<ImmutableTerm> simplifiedSubstitution = state.getSubTree().aggregationSubstitution
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
                return state.of(new AggregationSubTree(state.getSubTree().sampleFilter, state.getSubTree().groupingVariables,
                        newAggregationSubstitution, state.getSubTree().childConstructionNode, state.getSubTree().grandChild));

            ConstructionNode liftedConstructionNode = iqFactory.createConstructionNode(
                    Sets.union(state.getSubTree().groupingVariables, state.getSubTree().aggregationSubstitution.getDomain()).immutableCopy(),
                    liftedSubstitution);

            ImmutableSet<Variable> newGroupingVariables = Sets.difference(liftedConstructionNode.getChildVariables(),
                    newAggregationSubstitution.getDomain()).immutableCopy();

            return state.of(liftedConstructionNode, new AggregationSubTree(state.getSubTree().sampleFilter, newGroupingVariables,
                    newAggregationSubstitution, state.getSubTree().childConstructionNode, state.getSubTree().grandChild));
        }

        protected IQTree asIQTree(State<ConstructionNode, AggregationSubTree> state) {
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getAncestors())
                    .append(state.getSubTree().sampleFilter)
                    .append(iqFactory.createAggregationNode(state.getSubTree().groupingVariables, state.getSubTree().aggregationSubstitution),
                            treeCache)
                    .append(state.getSubTree().childConstructionNode)
                    .build(state.getSubTree().grandChild)
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }


        /**
         * Decomposes functional terms so as to lift non-aggregation function symbols above and block
         * the aggregation functional terms
         */
        private Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> decomposeFunctionalTerm(
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

        private ImmutableFunctionalTerm.FunctionalTermDecomposition getFunctionalTermDecomposition(ImmutableFunctionalTerm arg) {
            return decomposeFunctionalTerm(arg)
                    .orElseGet(() -> {
                        Variable var = variableGenerator.generateNewVariable();
                        return termFactory.getFunctionalTermDecomposition(var, substitutionFactory.getSubstitution(var, arg));
                    });
        }
    }
}
