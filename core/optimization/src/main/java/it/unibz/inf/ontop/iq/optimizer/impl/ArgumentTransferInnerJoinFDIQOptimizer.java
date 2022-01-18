package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
public class ArgumentTransferInnerJoinFDIQOptimizer implements InnerJoinIQOptimizer {

    private final ArgumentTransferJoinTransformer transformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected ArgumentTransferInnerJoinFDIQOptimizer(CoreSingletons coreSingletons) {
        SelfJoinFDSimplifier simplifier = new SelfJoinFDSimplifier(coreSingletons);
        this.transformer = new ArgumentTransferJoinTransformer(simplifier, coreSingletons);
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = transformer.transform(initialTree);
        return (newTree == initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    protected static class ArgumentTransferJoinTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final SelfJoinFDSimplifier simplifier;

        protected ArgumentTransferJoinTransformer(SelfJoinFDSimplifier simplifier, CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.simplifier = simplifier;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return simplifier.transformInnerJoin(rootNode, children, tree.getVariables())
                    .orElse(tree);
        }
    }

    protected static class SelfJoinFDSimplifier extends AbstractSelfJoinSimplifier<FunctionalDependency> {

        protected SelfJoinFDSimplifier(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        protected boolean canEliminateNodes() {
            return false;
        }

        @Override
        protected boolean hasConstraint(ExtensionalDataNode node) {
            return !node.getRelationDefinition().getOtherFunctionalDependencies().isEmpty();
        }

        @Override
        protected Stream<FunctionalDependency> extractConstraints(RelationDefinition relationDefinition) {
            return relationDefinition.getOtherFunctionalDependencies().stream();
        }

        @Override
        protected Optional<DeterminantGroupEvaluation> evaluateDeterminantGroup(
                ImmutableList<VariableOrGroundTerm> determinants, Collection<ExtensionalDataNode> dataNodes,
                FunctionalDependency constraint) {

            if (dataNodes.size() < 2)
                throw new IllegalArgumentException("At least two nodes");

            NormalizationBeforeUnification normalization = normalizeDataNodes(dataNodes, constraint);

            ExtensionalDataNode targetDataNode = selectTargetDataNode(normalization.dataNodes, constraint);

            ImmutableSet<Integer> dependentIndexes = constraint.getDependents().stream()
                    .map(a -> a.getIndex() - 1)
                    .collect(ImmutableCollectors.toSet());

            ImmutableSet<ImmutableExpression> expressions = extractExpressions(dataNodes, normalization.equalities, dependentIndexes);

            return unifyDataNodes(normalization.dataNodes.stream(), n -> extractDependentArgumentMap(n, dependentIndexes))
                    .map(u -> convertIntoDeterminantGroupEvaluation(u, targetDataNode,
                            ImmutableList.copyOf(normalization.dataNodes),
                            expressions, dependentIndexes));
        }

        /**
         * Selects as target the node with largest number of external arguments.
         *
         * Why? Partially arbitrary, but such a target is more likely that it cannot be eliminated by the optimization
         * based on the same terms.
         */
        protected ExtensionalDataNode selectTargetDataNode(Collection<ExtensionalDataNode> dataNodes, FunctionalDependency constraint) {
            ImmutableSet<Attribute> dependentAttributes = constraint.getDependents();
            ImmutableSet<Attribute> determinantAttributes = constraint.getDeterminants();

            RelationDefinition relationDefinition = dataNodes.iterator().next().getRelationDefinition();

            ImmutableList<Integer> externalArgumentIndexes = relationDefinition.getAttributes().stream()
                    .filter(a -> !dependentAttributes.contains(a))
                    .filter(a -> !determinantAttributes.contains(a))
                    .map(a -> a.getIndex() - 1)
                    .collect(ImmutableCollectors.toList());

            ImmutableMap<ExtensionalDataNode, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> nodeExternalArgumentMap =
                    dataNodes.stream()
                            .distinct()
                            .collect(ImmutableCollectors.toMap(
                                    n -> n,
                                    n -> n.getArgumentMap().entrySet().stream()
                                            .filter(e -> externalArgumentIndexes.contains(e.getKey()))
                                            .collect(ImmutableCollectors.toMap())));

            return dataNodes.stream()
                    .max(Comparator.comparingInt(n -> nodeExternalArgumentMap.get(n).values().size()))
                    .orElseThrow(() -> new MinorOntopInternalBugException("Non empty collection expected"));

        }

        private static ImmutableMap<Integer, ? extends VariableOrGroundTerm> extractDependentArgumentMap(
                ExtensionalDataNode node, ImmutableSet<Integer> dependentIndexes) {
            return node.getArgumentMap().entrySet().stream()
                    .filter(e -> dependentIndexes.contains(e.getKey()))
                    .collect(ImmutableCollectors.toMap());
        }

        private DeterminantGroupEvaluation convertIntoDeterminantGroupEvaluation(
                ImmutableUnificationTools.ArgumentMapUnification argumentMapUnification, ExtensionalDataNode targetDataNode,
                ImmutableList<ExtensionalDataNode> dataNodes, ImmutableSet<ImmutableExpression> expressions, ImmutableSet<Integer> dependentIndexes) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArgumentMap = targetDataNode.getArgumentMap();
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newTargetArgumentMap = Sets.union(
                        argumentMapUnification.argumentMap.keySet(), targetArgumentMap.keySet()).stream()
                    // For better readability
                    .sorted()
                    .collect(ImmutableCollectors.toMap(
                            i -> i,
                            i -> Optional.ofNullable((VariableOrGroundTerm) argumentMapUnification.argumentMap.get(i))
                                    .orElseGet(() -> targetArgumentMap.get(i))));

            // Here we only consider the first occurrence of the node! Important for not wrongly introducing implicit equalities
            int targetIndex = dataNodes.indexOf(targetDataNode);

            ImmutableList<ExtensionalDataNode> newNodes = IntStream.range(0, dataNodes.size())
                    .mapToObj(i -> i == targetIndex
                            ? iqFactory.createExtensionalDataNode(targetDataNode.getRelationDefinition(), newTargetArgumentMap)
                            : removeDependentArguments(dataNodes.get(i), dependentIndexes))
                    .collect(ImmutableCollectors.toList());

            return new DeterminantGroupEvaluation(expressions, newNodes, argumentMapUnification.substitution);
        }

        private ExtensionalDataNode removeDependentArguments(ExtensionalDataNode extensionalDataNode,
                                                             ImmutableSet<Integer> dependentIndexes) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = extensionalDataNode.getArgumentMap().entrySet().stream()
                    .filter(e -> !dependentIndexes.contains(e.getKey()))
                    .collect(ImmutableCollectors.toMap());

            return iqFactory.createExtensionalDataNode(extensionalDataNode.getRelationDefinition(), newArgumentMap);
        }

        private ImmutableSet<ImmutableExpression> extractExpressions(Collection<ExtensionalDataNode> dataNodes,
                                                                     ImmutableSet<ImmutableExpression> equalities,
                                                                     ImmutableSet<Integer> dependentIndexes) {
            ImmutableMultiset<Variable> dependentVariableOccurrences = dataNodes.stream()
                    .flatMap(n -> n.getArgumentMap().entrySet().stream())
                    .filter(e -> !dependentIndexes.contains(e.getKey()))
                    .map(Map.Entry::getValue)
                    .filter(d -> d instanceof Variable)
                    .map(d -> (Variable) d)
                    .collect(ImmutableCollectors.toMultiset());

            return Stream.concat(
                    dependentVariableOccurrences.entrySet().stream()
                            // Co-occurring variables
                            .filter(e -> e.getCount() > 1)
                            .map(Multiset.Entry::getElement)
                            .map(termFactory::getDBIsNotNull),
                    equalities.stream())
                    .collect(ImmutableCollectors.toSet());
        }
    }

}
