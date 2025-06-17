package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
public class ArgumentTransferInnerJoinFDIQOptimizer extends AbstractIQOptimizer implements InnerJoinIQOptimizer {

    private final SelfJoinFDSimplifier simplifier;

    @Inject
    protected ArgumentTransferInnerJoinFDIQOptimizer(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), NORMALIZE_FOR_OPTIMIZATION);
        this.simplifier = new SelfJoinFDSimplifier(coreSingletons);
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(new ArgumentTransferJoinTransformer(variableGenerator));
    }

    protected class ArgumentTransferJoinTransformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        protected ArgumentTransferJoinTransformer(VariableGenerator variableGenerator) {
            super(ArgumentTransferInnerJoinFDIQOptimizer.this.iqFactory, variableGenerator);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return simplifier.transformInnerJoin(rootNode, children, tree.getVariables(), variableGenerator)
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

            return normalization.dataNodes.stream().map(n -> ExtensionalDataNode.restrictTo(n.getArgumentMap(), dependentIndexes::contains))
                    .collect(substitutionFactory.onVariableOrGroundTerms().toArgumentMapUnifier())
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

            ImmutableSet<Integer> externalArgumentIndexes = relationDefinition.getAttributes().stream()
                    .filter(a -> !dependentAttributes.contains(a))
                    .filter(a -> !determinantAttributes.contains(a))
                    .map(a -> a.getIndex() - 1)
                    .collect(ImmutableCollectors.toSet());

            ImmutableMap<ExtensionalDataNode, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> nodeExternalArgumentMap =
                    dataNodes.stream()
                            .distinct()
                            .collect(ImmutableCollectors.toMap(
                                    n -> n,
                                    n -> ExtensionalDataNode.restrictTo(n.getArgumentMap(), externalArgumentIndexes::contains)));

            return dataNodes.stream()
                    .max(Comparator.comparingInt(n -> nodeExternalArgumentMap.get(n).values().size()))
                    .orElseThrow(() -> new MinorOntopInternalBugException("Non empty collection expected"));

        }

        private DeterminantGroupEvaluation convertIntoDeterminantGroupEvaluation(
                SubstitutionOperations.ArgumentMapUnifier<VariableOrGroundTerm> argumentMapUnification, ExtensionalDataNode targetDataNode,
                ImmutableList<ExtensionalDataNode> dataNodes, ImmutableSet<ImmutableExpression> expressions, ImmutableSet<Integer> dependentIndexes) {

            // Here we only consider the first occurrence of the node! Important for not wrongly introducing implicit equalities
            int targetIndex = dataNodes.indexOf(targetDataNode);

            ImmutableList<ExtensionalDataNode> newNodes = IntStream.range(0, dataNodes.size())
                    .mapToObj(i -> (i == targetIndex)
                        ? iqFactory.createExtensionalDataNode(targetDataNode.getRelationDefinition(),
                            ExtensionalDataNode.union(argumentMapUnification.getArgumentMap(), targetDataNode.getArgumentMap()))
                        : iqFactory.createExtensionalDataNode(dataNodes.get(i).getRelationDefinition(),
                            ExtensionalDataNode.restrictTo(dataNodes.get(i).getArgumentMap(), idx -> !dependentIndexes.contains(idx))))
                    .collect(ImmutableCollectors.toList());

            return new DeterminantGroupEvaluation(expressions, newNodes, argumentMapUnification.getSubstitution());
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
