package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.AggregationNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AggregationNormalizerImpl implements AggregationNormalizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected AggregationNormalizerImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    /**
     * Blocks distinct. May block some bindings and some filter conditions.
     *
     * TODO: enable lifting some filter conditions
     * TODO: we may consider remove distincts in the sub-tree when cardinality does not affect the substitution definitions
     */
    @Override
    public IQTree normalizeForOptimization(AggregationNode aggregationNode, IQTree child,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);

        QueryNode rootNode = normalizedChild.getRootNode();

        // State after lifting the bindings
        Optional<AggregationNormalizationState> state = Optional.of(rootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(n -> normalizeChildConstructionNode(aggregationNode, n,
                        ((UnaryIQTree) normalizedChild).getChild(), variableGenerator));


        IQProperties normalizedProperties = currentIQProperties.declareNormalizedForOptimization();
        // TODO: consider filters

        return state
                .map(s -> s.createNormalizedTree(normalizedProperties))
                .orElseGet(() -> iqFactory.createUnaryIQTree(aggregationNode, normalizedChild));
    }

    private AggregationNormalizationState normalizeChildConstructionNode(AggregationNode aggregationNode,
                                                                         ConstructionNode childConstructionNode,
                                                                         IQTree grandChild,
                                                                         VariableGenerator variableGenerator) {
        return new AggregationNormalizationState(aggregationNode, childConstructionNode, grandChild, variableGenerator)
                .propagateNonGroupingBindingsIntoToAggregationSubstitution()
                .liftGroupingBindings()
                .simplifyAggregationSubstitution();

    }


    protected class AggregationNormalizationState {

        private static final int MAX_ITERATIONS = 1000;
        private final AggregationNode aggregationNode;
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
                                                IQTree grandChild, VariableGenerator variableGenerator) {

            this.aggregationNode = aggregationNode;
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
            this.variableGenerator = variableGenerator;
            this.ancestors = ImmutableList.of();
        }

        private AggregationNormalizationState(ImmutableList<ConstructionNode> ancestors,
                                              AggregationNode aggregationNode, @Nullable ConstructionNode childConstructionNode,
                                              IQTree grandChild, VariableGenerator variableGenerator) {
            this.ancestors = ancestors;
            this.aggregationNode = aggregationNode;
            this.childConstructionNode = childConstructionNode;
            this.grandChild = grandChild;
            this.variableGenerator = variableGenerator;
        }

        /**
         * All the bindings of non-grouping variables in the child construction node are propagated to
         * the aggregation substitution
         */
        public AggregationNormalizationState propagateNonGroupingBindingsIntoToAggregationSubstitution() {
            if (childConstructionNode == null)
                return this;

            ImmutableSet<Variable> groupingVariables = aggregationNode.getGroupingVariables();
            // NB: non grouping variables that are USED by the aggregation node (we can safely ignore the non-used ones)
            ImmutableSet<Variable> nonGroupingVariables = Sets.difference(
                    aggregationNode.getChildVariables(),
                    groupingVariables).immutableCopy();

            ImmutableSubstitution<ImmutableTerm> nonGroupingSubstitution = childConstructionNode.getSubstitution()
                    .reduceDomainToIntersectionWith(nonGroupingVariables);

            ImmutableSubstitution<ImmutableFunctionalTerm> newAggregationSubstitution =
                    (ImmutableSubstitution<ImmutableFunctionalTerm>) (ImmutableSubstitution<?>)
                    nonGroupingSubstitution.composeWith(aggregationNode.getSubstitution());

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(
                    aggregationNode.getGroupingVariables(),
                    newAggregationSubstitution);

            // Nullable
            ConstructionNode newChildConstructionNode = Optional.of(childConstructionNode.getSubstitution()
                    .reduceDomainToIntersectionWith(groupingVariables))
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(newAggregationNode.getChildVariables(), s))
                    .orElse(null);

            return new AggregationNormalizationState(ancestors, newAggregationNode, newChildConstructionNode, grandChild,
                    variableGenerator);
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

            ImmutableSubstitution<ImmutableTerm> substitution = childConstructionNode.getSubstitution();
            ImmutableSet<Variable> groupingVariables = aggregationNode.getGroupingVariables();

            if (substitution.isEmpty())
                return this;
            if (!groupingVariables.containsAll(substitution.getDomain())) {
                throw new MinorOntopInternalBugException("Was expecting all the non-grouping bindings to be lifted");
            }

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
            ImmutableSet<Variable> aggregateVariables = aggregationNode.getSubstitution().getDomain();

            ImmutableList<ConstructionNode> subStateAncestors = subState.getAncestors();

            ImmutableList<ConstructionNode> newAncestors = Stream.concat(
                    // Already defined
                    ancestors.stream(),
                    // Ancestors of the sub-state modified so as to project the aggregation variables
                    subStateAncestors.stream()
                            .map(a -> iqFactory.createConstructionNode(Sets.union(a.getVariables(),
                                    aggregateVariables).immutableCopy(), a.getSubstitution())))
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Variable> groupingVariables = aggregationNode.getGroupingVariables();

            // The closest parent informs us about the new grouping variables
            ImmutableSet<Variable> newGroupingVariables = subStateAncestors.isEmpty()
                    ? groupingVariables
                    : subStateAncestors.get(subStateAncestors.size() - 1).getChildVariables();

            // Applies all the substitutions of the ancestors to the substitution of the aggregation node
            // Needed when some grouping variables are also used in the aggregates
            ImmutableSubstitution<ImmutableFunctionalTerm> newAggregationSubstitution = subStateAncestors.stream()
                    .reduce(aggregationNode.getSubstitution(),
                            (s, a) -> (ImmutableSubstitution<ImmutableFunctionalTerm>) (ImmutableSubstitution<?>)
                                    a.getSubstitution().composeWith(s),
                            (s1, s2) -> {
                                throw new MinorOntopInternalBugException("Substitution merging was not expected");
                            });

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(newGroupingVariables, newAggregationSubstitution);

            // Nullable
            ConstructionNode newChildConstructionNode = subState.getChildConstructionNode()
                    // Only keeps the child construction node if it has a substitution
                    .filter(n -> !n.getSubstitution().isEmpty())
                    .map(n -> iqFactory.createConstructionNode(newAggregationNode.getChildVariables(), n.getSubstitution()))
                    .orElse(null);


            return new AggregationNormalizationState(newAncestors, newAggregationNode, newChildConstructionNode,
                    subState.getGrandChildTree(), variableGenerator);
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
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChild,
                            iqFactory.createIQProperties().declareNormalizedForOptimization()))
                    .orElse(grandChild)
                    .getVariableNullability();

            // The simplification may do the "lifting" inside the functional term (having a non-aggregation
            // functional term above the aggregation one)
            ImmutableSubstitution<ImmutableTerm> simplifiedSubstitution = aggregationNode.getSubstitution()
                    .simplifyValues(variableNullability);

            ImmutableMap<Variable, Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition>> decompositionMap =
                    simplifiedSubstitution.getImmutableMap().entrySet().stream()
                            .filter(e -> e.getValue() instanceof ImmutableFunctionalTerm)
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> decomposeFunctionalTerm((ImmutableFunctionalTerm) e.getValue())));

            ImmutableMap<Variable, ImmutableTerm> liftedSubstitutionMap = Stream.concat(
                    // All variables and constants
                    simplifiedSubstitution.getImmutableMap().entrySet().stream()
                            .filter(e -> e.getValue() instanceof NonFunctionalTerm),
                    // (Possibly decomposed) functional terms
                    decompositionMap.entrySet().stream()
                            .filter(e -> e.getValue().isPresent())
                            .map(e -> Maps.immutableEntry(e.getKey(),
                                    (ImmutableTerm) e.getValue().get().getLiftableTerm())))
                    .collect(ImmutableCollectors.toMap());

            if (liftedSubstitutionMap.isEmpty())
                return this;

            ConstructionNode liftedConstructionNode = iqFactory.createConstructionNode(
                    aggregationNode.getVariables(), substitutionFactory.getSubstitution(liftedSubstitutionMap));

            ImmutableMap<Variable, ImmutableFunctionalTerm> newAggregationSubstitutionMap =
                    decompositionMap.entrySet().stream()
                            .flatMap(e -> e.getValue()
                                    // Sub-term substitution entries from decompositions
                                    .map(d -> d.getSubTermSubstitutionMap()
                                            // (PARTIAL LIFT CASE)
                                            .map(s -> s.entrySet().stream())
                                            // FULL LIFT
                                            .orElseGet(Stream::empty))
                                    // Non-decomposable entries
                                    .orElseGet(() -> Stream.of(Maps.immutableEntry(
                                            e.getKey(),
                                            (ImmutableFunctionalTerm) simplifiedSubstitution.get(e.getKey())))))
                            .collect(ImmutableCollectors.toMap());

            AggregationNode newAggregationNode = iqFactory.createAggregationNode(liftedConstructionNode.getChildVariables(),
                    substitutionFactory.getSubstitution(newAggregationSubstitutionMap));

            ImmutableList<ConstructionNode> newAncestors = Stream.concat(ancestors.stream(), Stream.of(liftedConstructionNode))
                            .collect(ImmutableCollectors.toList());

            return new AggregationNormalizationState(newAncestors, newAggregationNode, childConstructionNode, grandChild,
                    variableGenerator);
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
            ImmutableMap<Integer, Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition>> subTermDecompositions =
                    IntStream.range(0, arguments.size())
                    .filter(i -> arguments.get(i) instanceof ImmutableFunctionalTerm)
                    .boxed()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            // Recursive
                            i -> decomposeFunctionalTerm((ImmutableFunctionalTerm) arguments.get(i))));

            ImmutableList<ImmutableTerm> newArguments = IntStream.range(0, arguments.size())
                    .boxed()
                    .map(i -> Optional.ofNullable(subTermDecompositions.get(i))
                            // Functional term
                            .map(optionalDecomposition -> optionalDecomposition
                                    // Injective functional sub-term
                                    .map(ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                                    .map(t -> (ImmutableTerm) t)
                                    // Otherwise a fresh variable
                                    .orElseGet(variableGenerator::generateNewVariable))
                            // Previous argument when non-functional
                            .orElseGet(() -> arguments.get(i)))
                    .collect(ImmutableCollectors.toList());

            ImmutableMap<Variable, ImmutableFunctionalTerm> subTermSubstitutionMap = subTermDecompositions.entrySet().stream()
                    .flatMap(e -> e.getValue()
                            // Decomposition case
                            .map(d -> d.getSubTermSubstitutionMap()
                                    .map(s -> s.entrySet().stream())
                                    .orElseGet(Stream::empty))
                            // Not decomposed: new entry (new variable -> functional term)
                            .orElseGet(() -> Stream.of(Maps.immutableEntry(
                                    (Variable) newArguments.get(e.getKey()),
                                    (ImmutableFunctionalTerm) arguments.get(e.getKey())))))
                    .collect(ImmutableCollectors.toMap());

            ImmutableFunctionalTerm newFunctionalTerm = termFactory.getImmutableFunctionalTerm(functionSymbol, newArguments);

            return subTermSubstitutionMap.isEmpty()
                    ? Optional.of(termFactory.getFunctionalTermDecomposition(newFunctionalTerm))
                    : Optional.of(termFactory.getFunctionalTermDecomposition(newFunctionalTerm, subTermSubstitutionMap));
        }


        protected IQTree createNormalizedTree(IQProperties normalizedProperties) {
            IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

            IQTree newChildTree = Optional.ofNullable(childConstructionNode)
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChild,
                            iqFactory.createIQProperties().declareNormalizedForOptimization()))
                    .orElse(grandChild);

            IQTree aggregationTree = iqFactory.createUnaryIQTree(aggregationNode, newChildTree,
                    normalizedProperties);

            return ancestors.reverse().stream()
                    .reduce(aggregationTree,
                            (t, a) -> iqFactory.createUnaryIQTree(a, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); })
                    // Recursive (for merging top construction nodes)
                    .normalizeForOptimization(variableGenerator);
        }
    }

}
