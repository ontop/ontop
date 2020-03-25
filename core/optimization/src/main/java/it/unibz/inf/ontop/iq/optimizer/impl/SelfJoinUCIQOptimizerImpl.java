package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinUCIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinUCIQOptimizerImpl implements SelfJoinUCIQOptimizer {

    private final SelfJoinUCTransformer selfJoinUCTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    public SelfJoinUCIQOptimizerImpl(IntermediateQueryFactory iqFactory,
                                     SelfJoinUCTransformer selfJoinUCTransformer) {
        this.iqFactory = iqFactory;
        this.selfJoinUCTransformer = selfJoinUCTransformer;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = selfJoinUCTransformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }



    private static class SelfJoinUCTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final SelfJoinUCSimplifier simplifier;

        @Inject
        protected SelfJoinUCTransformer(CoreSingletons coreSingletons,
                                        SelfJoinUCSimplifier simplifier) {
            super(coreSingletons);
            this.simplifier = simplifier;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            ImmutableList<IQTree> liftedChildren = children.stream()
                    .map(t -> t.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            return simplifier.transformInnerJoin(rootNode, liftedChildren, tree.getVariables());
        }
    }

    protected static class SelfJoinUCSimplifier extends AbstractSelfJoinSimplifier<UniqueConstraint> {

        @Inject
        protected SelfJoinUCSimplifier(CoreSingletons coreSingletons) {
            super(coreSingletons);
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


            ImmutableSet<ImmutableExpression> expressions = Stream.concat(
                    determinants.stream()
                            .filter(d -> d instanceof Variable)
                            .map(d -> (Variable) d)
                            .map(termFactory::getDBIsNotNull),
                    normalization.equalities.stream())
                    .collect(ImmutableCollectors.toSet());

            return unifyDataNodes(normalization.dataNodes.stream())
                    .map(u -> new DeterminantGroupEvaluation(
                            expressions,
                            ImmutableList.of(
                                    iqFactory.createExtensionalDataNode(
                                            dataNodes.iterator().next().getRelationDefinition(),
                                            u.argumentMap)),
                            u.substitution));
        }
    }

}
