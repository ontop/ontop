package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinSameTermIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class SelfJoinSameTermIQOptimizerImpl implements SelfJoinSameTermIQOptimizer {

    private final IQTreeTransformer lookForDistinctTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected SelfJoinSameTermIQOptimizerImpl(OptimizationSingletons optimizationSingletons, IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.lookForDistinctTransformer = new LookForDistinctTransformerImpl(
                SameTermSelfJoinTransformer::new,
                optimizationSingletons);
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = lookForDistinctTransformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                    .normalizeForOptimization();
    }

    /**
     * TODO: explain
     */
    protected static class SameTermSelfJoinTransformer extends AbstractDiscardedVariablesTransformer {

        private final IQTreeTransformer lookForDistinctTransformer;
        private final OptimizationSingletons optimizationSingletons;
        private final IntermediateQueryFactory iqFactory;
        private final TermFactory termFactory;
        private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;

        protected SameTermSelfJoinTransformer(ImmutableSet<Variable> discardedVariables,
                                              IQTreeTransformer lookForDistinctTransformer,
                                              OptimizationSingletons optimizationSingletons) {
            super(discardedVariables, lookForDistinctTransformer, optimizationSingletons.getCoreSingletons());
            this.lookForDistinctTransformer = lookForDistinctTransformer;
            this.optimizationSingletons = optimizationSingletons;
            CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
            iqFactory = coreSingletons.getIQFactory();
            termFactory = coreSingletons.getTermFactory();
            requiredExtensionalDataNodeExtractor = optimizationSingletons.getRequiredExtensionalDataNodeExtractor();
        }

        @Override
        protected AbstractDiscardedVariablesTransformer update(ImmutableSet<Variable> newDiscardedVariables) {
            return new SameTermSelfJoinTransformer(newDiscardedVariables, lookForDistinctTransformer, optimizationSingletons);
        }

        /**
         * TODO: explain
         *
         * Only removes some children that are extensional data nodes
         */
        @Override
        protected Optional<IQTree> furtherSimplifyInnerJoinChildren(ImmutableList<ImmutableSet<Variable>> discardedVariablesPerChild,
                                                                    Optional<ImmutableExpression> optionalFilterCondition,
                                                                    ImmutableList<IQTree> partiallySimplifiedChildren) {
            //Mutable
            final List<IQTree> currentChildren = Lists.newArrayList(partiallySimplifiedChildren);
            IntStream.range(0, partiallySimplifiedChildren.size())
                    .boxed()
                    .filter(i -> isDetectedAsRedundant(
                            currentChildren.get(i),
                            discardedVariablesPerChild.get(i),
                            IntStream.range(0, partiallySimplifiedChildren.size())
                                    .filter(j -> j!= i)
                                    .boxed()
                                    .map(currentChildren::get)))
                    // SIDE-EFFECT
                    .forEach(i -> currentChildren.set(i, iqFactory.createTrueNode()));

            ImmutableSet<Variable> variablesToFilterNulls = IntStream.range(0, partiallySimplifiedChildren.size())
                    .filter(i -> currentChildren.get(i).getRootNode() instanceof TrueNode)
                    .boxed()
                    .map(i -> Sets.difference(partiallySimplifiedChildren.get(i).getVariables(),
                            discardedVariablesPerChild.get(i)))
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());

            return Optional.of(variablesToFilterNulls)
                    // If no variable to filter, no change, returns empty
                    .filter(vs -> !vs.isEmpty())
                    .map(vs -> vs.stream()
                            .map(termFactory::getDBIsNotNull))
                    .map(s -> optionalFilterCondition
                            .map(f -> Stream.concat(Stream.of(f), s))
                            .orElse(s))
                    .flatMap(termFactory::getConjunction)
                    .map(iqFactory::createInnerJoinNode)
                    // NB: will be normalized later on
                    .map(n -> iqFactory.createNaryIQTree(n, ImmutableList.copyOf(currentChildren)));
        }

        /**
         * Should not return any false positive
         */
        boolean isDetectedAsRedundant(IQTree child, ImmutableSet<Variable> discardedVariables, Stream<IQTree> otherChildren) {
            return Optional.of(child)
                    .filter(c -> c instanceof ExtensionalDataNode)
                    .map(c -> (ExtensionalDataNode) c)
                    .filter(d1 -> otherChildren
                            .flatMap(t -> t.acceptVisitor(requiredExtensionalDataNodeExtractor))
                            .anyMatch(d2 -> isDetectedAsRedundant(d1, d2, discardedVariables)))
                    .isPresent();
        }

        private boolean isDetectedAsRedundant(ExtensionalDataNode dataNode, ExtensionalDataNode otherDataNode,
                                              ImmutableSet<Variable> discardedVariables) {
            if (!dataNode.getRelationDefinition().equals(otherDataNode.getRelationDefinition()))
                return false;

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = dataNode.getArgumentMap();
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> otherArgumentMap = otherDataNode.getArgumentMap();

            ImmutableSet<Integer> firstIndexes = argumentMap.keySet();
            ImmutableSet<Integer> otherIndexes = otherArgumentMap.keySet();

            Sets.SetView<Integer> allIndexes = Sets.union(firstIndexes, otherIndexes);
            Sets.SetView<Integer> commonIndexes = Sets.intersection(firstIndexes, otherIndexes);

            ImmutableList<? extends VariableOrGroundTerm> differentArguments = allIndexes.stream()
                    .filter(i -> !(commonIndexes.contains(i) && argumentMap.get(i).equals(otherArgumentMap.get(i))))
                    .flatMap(i -> Optional.ofNullable(argumentMap.get(i))
                            .map(Stream::of)
                            .orElseGet(Stream::empty))
                    .collect(ImmutableCollectors.toList());

            // There must be at least one match
            if (differentArguments.size() == allIndexes.size())
                return false;

            /*
             * All the non-matching arguments of the atom must be discarded variables
             */
            return discardedVariables.containsAll(differentArguments);
        }
    }
}