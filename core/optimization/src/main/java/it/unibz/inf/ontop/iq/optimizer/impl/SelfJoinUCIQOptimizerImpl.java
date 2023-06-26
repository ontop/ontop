package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinUCIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinUCIQOptimizerImpl implements SelfJoinUCIQOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final SelfJoinUCSimplifier simplifier;
    private final CoreSingletons coreSingletons;

    @Inject
    public SelfJoinUCIQOptimizerImpl(IntermediateQueryFactory iqFactory,
                                     SelfJoinUCSimplifier simplifier,
                                     CoreSingletons coreSingletons) {
        this.iqFactory = iqFactory;
        this.simplifier = simplifier;
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = new SelfJoinUCTransformer(coreSingletons, simplifier, query.getVariableGenerator()).transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }



    private static class SelfJoinUCTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final SelfJoinUCSimplifier simplifier;
        private final VariableGenerator variableGenerator;

        protected SelfJoinUCTransformer(CoreSingletons coreSingletons,
                                        SelfJoinUCSimplifier simplifier,
                                        VariableGenerator variableGenerator) {
            super(coreSingletons);
            this.simplifier = simplifier;
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            ImmutableList<IQTree> liftedChildren = children.stream()
                    .map(t -> t.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            return simplifier.transformInnerJoin(rootNode, liftedChildren, tree.getVariables(), variableGenerator)
                    .orElseGet(() -> children.equals(liftedChildren)
                            ? tree
                            : iqFactory.createNaryIQTree(rootNode, liftedChildren));
        }
    }

    protected static class SelfJoinUCSimplifier extends AbstractSelfJoinSimplifier<UniqueConstraint> {

        @Inject
        protected SelfJoinUCSimplifier(CoreSingletons coreSingletons, IQTreeTools iqTreeTools) {
            super(coreSingletons, iqTreeTools);
        }

        @Override
        protected boolean canEliminateNodes() {
            return true;
        }

        @Override
        protected boolean hasConstraint(ExtensionalDataNode node) {
            return !node.getRelationDefinition().getUniqueConstraints().isEmpty();
        }

        @Override
        protected Stream<UniqueConstraint> extractConstraints(RelationDefinition relationDefinition) {
            return relationDefinition.getUniqueConstraints().stream();
        }

        @Override
        protected Optional<DeterminantGroupEvaluation> evaluateDeterminantGroup(ImmutableList<VariableOrGroundTerm> determinants,
                                                                                Collection<ExtensionalDataNode> dataNodes,
                                                                                UniqueConstraint constraint) {
            if (dataNodes.size() < 2)
                throw new IllegalArgumentException("At least two nodes");

            NormalizationBeforeUnification normalization = normalizeDataNodes(dataNodes, constraint);

            ImmutableMultiset<Variable> variableOccurrences = dataNodes.stream()
                    .flatMap(n -> n.getArgumentMap().values().stream())
                    .filter(d -> d instanceof Variable)
                    .map(d -> (Variable) d)
                    .collect(ImmutableCollectors.toMultiset());

            ImmutableSet<ImmutableExpression> expressions = Stream.concat(
                            variableOccurrences.entrySet().stream()
                                    // Co-occurring terms
                                    .filter(e -> e.getCount() > 1)
                                    .map(Multiset.Entry::getElement)
                                    .map(termFactory::getDBIsNotNull),
                            normalization.equalities.stream())
                    .collect(ImmutableCollectors.toSet());

            return normalization.dataNodes.stream().map(ExtensionalDataNode::getArgumentMap)
                    .collect(substitutionFactory.onVariableOrGroundTerms().toArgumentMapUnifier())
                    .map(u -> new DeterminantGroupEvaluation(
                            expressions,
                            ImmutableList.of(
                                    iqFactory.createExtensionalDataNode(
                                            dataNodes.iterator().next().getRelationDefinition(),
                                            u.getArgumentMap())),
                            u.getSubstitution()));
        }
    }

}
