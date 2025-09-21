package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.AggregationFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class AggregationNormalizerImpl implements AggregationNormalizer {

    private final CoreSingletons coreSingletons;
    private final TermFactory termFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;

    @Inject
    protected AggregationNormalizerImpl(CoreSingletons coreSingletons,
                                        NotRequiredVariableRemover notRequiredVariableRemover) {
        this.coreSingletons = coreSingletons;
        this.termFactory = coreSingletons.getTermFactory();
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

        Context context = new Context(variableGenerator, treeCache);
        return context.normalize(aggregationNode, child);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class AggregationSubTree {
        private final Optional<FilterNode> sampleFilter;

        private final AggregationNode aggregationNode;

        private final Optional<ConstructionNode> childConstructionNode;
        private final IQTree grandChild;

        AggregationSubTree(Optional<FilterNode> sampleFilter, AggregationNode aggregationNode, Optional<ConstructionNode> childConstructionNode, IQTree grandChild) {
            this.sampleFilter = sampleFilter;
            this.aggregationNode = aggregationNode;
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
        }

        ImmutableSet<Variable> groupingVariables() {
            return aggregationNode.getGroupingVariables();
        }

        Substitution<ImmutableFunctionalTerm> aggregationSubstitution() {
            return aggregationNode.getSubstitution();
        }

        AggregationNode aggregationNode() {
            return aggregationNode;
        }

        Optional<ConstructionNode> childConstructionNode() {
            return childConstructionNode;
        }

        IQTree grandChild() {
            return grandChild;
        }

        Optional<FilterNode> sampleFilter() {
            return sampleFilter;
        }

        static AggregationSubTree of(AggregationNode aggregationNode, Optional<ConstructionNode> childConstructionNode, IQTree grandChild) {
            return new AggregationSubTree(Optional.empty(), aggregationNode, childConstructionNode, grandChild);
        }

        AggregationSubTree replace(AggregationNode aggregationNode, Optional<ConstructionNode> childConstructionNode) {
            return new AggregationSubTree(this.sampleFilter, aggregationNode, childConstructionNode, this.grandChild);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof AggregationSubTree) {
                AggregationSubTree other = (AggregationSubTree) o;
                return sampleFilter.equals(other.sampleFilter)
                        && aggregationNode.equals(other.aggregationNode)
                        && childConstructionNode.equals(other.childConstructionNode)
                        && grandChild.equals(other.grandChild);
            }
            return false;
        }
    }

    private class Context extends InjectiveBindingLiftContext {

        private static final int MAX_ITERATIONS = 1000;

        Context(VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator, coreSingletons, treeCache);
        }

        IQTreeCache getNormalizedTreeCache() {
            return treeCache.declareAsNormalizedForOptimizationWithEffect();
        }

        IQTree normalize(AggregationNode aggregationNode, IQTree child) {
            if (aggregationNode.getSubstitution().isEmpty()) {
                if (aggregationNode.getGroupingVariables().isEmpty())
                    return iqFactory.createTrueNode();

                return normalizeSubTreeRecursively(
                        iqTreeTools.unaryIQTreeBuilder()
                                .append(iqFactory.createDistinctNode())
                                .append(iqFactory.createConstructionNode(aggregationNode.getVariables()))
                                .build(child));
            }

            IQTree shrunkChild = notRequiredVariableRemover.optimize(
                    normalizeSubTreeRecursively(child),
                    aggregationNode.getLocallyRequiredVariables(), variableGenerator);

            if (shrunkChild.isDeclaredAsEmpty()) {
                if (!aggregationNode.getGroupingVariables().isEmpty())
                    return iqFactory.createEmptyNode(aggregationNode.getVariables());

                Substitution<ImmutableTerm> newSubstitution = aggregationNode.getSubstitution()
                        .transform(this::simplifyEmptyAggregate);

                return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(aggregationNode.getVariables(), newSubstitution),
                        iqFactory.createTrueNode(),
                        getNormalizedTreeCache());
            }

            var construction = UnaryIQTreeDecomposition.of(shrunkChild, ConstructionNode.class);
            var initial = State.<ConstructionNode, AggregationSubTree>initial(AggregationSubTree.of(aggregationNode, construction.getOptionalNode(), construction.getTail()));

            var finalState =
                    simplifyAggregationSubstitution(
                            liftGroupingBindings(
                                    propagateNonGroupingBindingsIntoAggregationSubstitution(
                                            initial)));

            // TODO: consider filters
            return asIQTree(finalState);
        }

        private ImmutableTerm simplifyEmptyAggregate(ImmutableFunctionalTerm aggregateTerm) {
            FunctionSymbol functionSymbol = aggregateTerm.getFunctionSymbol();
            if (functionSymbol instanceof AggregationFunctionSymbol) {
                return ((AggregationFunctionSymbol) functionSymbol).evaluateEmptyBag(termFactory);
            }
            throw new MinorOntopInternalBugException("Was expecting an AggregationFunctionSymbol");
        }

        /**
         * All the bindings of non-grouping variables in the child construction node are propagated to
         * the aggregation substitution
         */
        State<ConstructionNode, AggregationSubTree> propagateNonGroupingBindingsIntoAggregationSubstitution(State<ConstructionNode, AggregationSubTree> state) {
            AggregationSubTree subTree = state.getSubTree();
            if (subTree.childConstructionNode().isEmpty())
                return state;

            Substitution<ImmutableTerm> substitution = subTree.childConstructionNode().get().getSubstitution();

            AggregationNode aggregationNode = subTree.aggregationNode();
            ImmutableSet<Variable> groupingVariables = aggregationNode.getGroupingVariables();

            // NB: non-grouping variables that are USED by the aggregation node (we can safely ignore the non-used ones)
            Substitution<ImmutableTerm> nonGroupingSubstitution = substitution.restrictDomainTo(
                    Sets.difference(aggregationNode.getChildVariables(), groupingVariables));

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(
                    groupingVariables,
                    applySubstitution(nonGroupingSubstitution, aggregationNode.getSubstitution()));

            Optional<ConstructionNode> newChildConstructionNode = iqTreeTools.createOptionalConstructionNode(
                    newAggregationNode::getChildVariables,
                    substitution.restrictDomainTo(groupingVariables));

            return state.replace(subTree.replace(newAggregationNode, newChildConstructionNode));
        }

        private Substitution<ImmutableFunctionalTerm> applySubstitution(Substitution<ImmutableTerm> substitution,  Substitution<ImmutableFunctionalTerm> aggregation) {
            return substitution.compose(aggregation).builder()
                    .restrictDomainTo(aggregation.getDomain())
                    .transform(t -> (ImmutableFunctionalTerm) t)
                    .build();
        }

        /**
         * Lifts (fragments of) bindings that are injective.
         * <p>
         * propagateNonGroupingBindingsIntoToAggregationSubstitution() is expected to have been called before
         */
        State<ConstructionNode, AggregationSubTree> liftGroupingBindings(State<ConstructionNode, AggregationSubTree> state) {
            AggregationSubTree subTree = state.getSubTree();
            if (subTree.childConstructionNode().isEmpty())
                return state;

            Substitution<ImmutableTerm> substitution = subTree.childConstructionNode().get().getSubstitution();
            if (substitution.isEmpty())
                return state;

            if (!subTree.groupingVariables().containsAll(substitution.getDomain()))
                throw new MinorOntopInternalBugException("Was expecting all the non-grouping bindings to be lifted");

            // Only projecting grouping variables
            // (mimicking the special case when GROUP BY reduces itself to a DISTINCT and a projection)
            ConstructionNode groupingConstructionNode = iqFactory.createConstructionNode(subTree.groupingVariables(), substitution);

            State<ConstructionNode, UnarySubTree<ConstructionNode>> subState =
                    State.<ConstructionNode, UnarySubTree<ConstructionNode>>initial(
                            UnarySubTree.of(groupingConstructionNode, subTree.grandChild()))
                            .reachFinal(MAX_ITERATIONS, this::liftBindings);

            UnaryOperatorSequence<ConstructionNode> newAncestors = state.getAncestors().append(
                    // Ancestors of the sub-state modified so as to project the aggregation variables
                    subState.getAncestors().stream()
                            .map(a -> iqFactory.createConstructionNode(
                                    Sets.union(a.getVariables(), subTree.aggregationSubstitution().getDomain()).immutableCopy(),
                                    a.getSubstitution())));

            // Applies all the substitutions of the ancestors to the substitution of the aggregation node
            // Needed when some grouping variables are also used in the aggregates
            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = subState.getAncestors().stream()
                    .map(ConstructionNode::getSubstitution)
                    .reduce(subTree.aggregationSubstitution(),
                            (s, a) -> applySubstitution(a, s),
                            (s1, s2) -> {
                                throw new MinorOntopInternalBugException("Substitution merging was not expected");
                            });

            // The closest parent informs us about the new grouping variables
            ImmutableSet<Variable> newGroupingVariables = subState.getAncestors().isEmpty()
                    ? subTree.groupingVariables()
                    : Sets.difference(
                            subState.getAncestors().getLast().getChildVariables(),
                            newAggregationSubstitution.getDomain())
                    .immutableCopy();

            Optional<Variable> sampleVariable = newGroupingVariables.isEmpty() && !subTree.groupingVariables().isEmpty()
                    ? Optional.of(variableGenerator.generateNewVariable("aggv"))
                    : Optional.empty();

            Substitution<ImmutableFunctionalTerm> finalAggregationSubstitution =
                    substitutionFactory.union(newAggregationSubstitution, getSampleSubstitution(sampleVariable));

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(newGroupingVariables, finalAggregationSubstitution);

            // Is created if, either, the node includes a substitution, or a sample variable is required.
            Optional<ConstructionNode> newChildConstructionNode = subState.getSubTree().getOptionalNode()
                    // Only keeps the child construction node if it has a substitution
                    .flatMap(n -> iqTreeTools.createOptionalConstructionNode(
                            newAggregationNode::getChildVariables,
                            n.getSubstitution()));

            // Creates a filter over the sample variable so that only rows that have a non-null value in it are kept.
            Optional<FilterNode> newFilter = sampleVariable.map(v -> iqFactory.createFilterNode(termFactory.getDBIsNotNull(v)));

            return new State<>(newAncestors, new AggregationSubTree(newFilter, newAggregationNode, newChildConstructionNode, subState.getSubTree().getChild()));
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        Substitution<ImmutableFunctionalTerm> getSampleSubstitution(Optional<Variable> optionalVariable) {
            return optionalVariable.map(v -> substitutionFactory.getSubstitution(
                            v,
                            termFactory.getDBSample(termFactory.getDBIntegerConstant(1),
                                    termFactory.getTypeFactory().getDBTypeFactory().getDBLargeIntegerType())))
                    .orElseGet(substitutionFactory::getSubstitution);
        }

        /**
         * Simplifies the substitution of the aggregation node and partially lift some bindings
         * so as to guarantee that all the values of the substitution are functional terms using
         * an aggregation function symbol.
         */
        State<ConstructionNode, AggregationSubTree> simplifyAggregationSubstitution(State<ConstructionNode, AggregationSubTree> state) {
            AggregationSubTree subTree = state.getSubTree();
            // NB: use ImmutableSubstitution.simplifyValues()
            // NB: look at FunctionSymbol.isAggregation()

            VariableNullability variableNullability = iqTreeTools.unaryIQTreeBuilder()
                    .append(subTree.childConstructionNode())
                    .build(subTree.grandChild())
                    .getVariableNullability();

            // The simplification may do the "lifting" inside the functional term (having a non-aggregation
            // functional term above the aggregation one)
            Substitution<ImmutableTerm> simplifiedSubstitution = subTree.aggregationSubstitution()
                    .transform(t -> t.simplify(variableNullability));

            SubstitutionSplitter decomposition = new SubstitutionSplitter(simplifiedSubstitution, this::decomposeFunctionalTerm);
            Substitution<ImmutableTerm> liftedSubstitution = decomposition.getLiftedSubstitution();
            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = decomposition.getNonLiftedSubstitution();

            if (liftedSubstitution.isEmpty()) {
                AggregationNode newAggregationNode = iqFactory.createAggregationNode(subTree.groupingVariables(), newAggregationSubstitution);
                return state.replace(subTree.replace(newAggregationNode, subTree.childConstructionNode()));
            }

            ConstructionNode liftedConstructionNode = iqFactory.createConstructionNode(
                    subTree.aggregationNode().getVariables(),
                    liftedSubstitution);

            // so that newAggregationNode.getVariables() coincides with liftedConstructionNode.getChildVariables()
            ImmutableSet<Variable> newGroupingVariables = Sets.difference(
                    liftedConstructionNode.getChildVariables(),
                    newAggregationSubstitution.getDomain()).immutableCopy();

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(newGroupingVariables, newAggregationSubstitution);
            return state.lift(liftedConstructionNode, subTree.replace(newAggregationNode, subTree.childConstructionNode()));
        }

        IQTree asIQTree(State<ConstructionNode, AggregationSubTree> state) {
            AggregationSubTree subTree = state.getSubTree();
            // Recursive (for merging top construction nodes)
            return normalizeSubTreeRecursively(
                    iqTreeTools.unaryIQTreeBuilder()
                            .append(state.getAncestors())
                            .append(subTree.sampleFilter())
                            .append(subTree.aggregationNode(), getNormalizedTreeCache())
                            .append(subTree.childConstructionNode()) // normalized cache?
                            .build(subTree.grandChild())); // from shrunk child - normalized?
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
