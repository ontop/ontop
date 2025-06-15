package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinUCIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinUCIQOptimizerImpl extends AbstractIQOptimizer implements SelfJoinUCIQOptimizer {

    private final SelfJoinUCSimplifier simplifier;

    @Inject
    private SelfJoinUCIQOptimizerImpl(SelfJoinUCSimplifier simplifier,
                                     CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), NORMALIZE_FOR_OPTIMIZATION);
        this.simplifier = simplifier;
    }

    @Override
    protected IQVisitor<IQTree> getTransformer(IQ query) {
        return new SelfJoinUCTransformer(query.getVariableGenerator());
    }

    private class SelfJoinUCTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;

        protected SelfJoinUCTransformer(VariableGenerator variableGenerator) {
            super(SelfJoinUCIQOptimizerImpl.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            ImmutableList<IQTree> liftedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);

            return simplifier.transformInnerJoin(rootNode, liftedChildren, tree.getVariables(), variableGenerator)
                    .orElseGet(() -> children.equals(liftedChildren)
                            ? tree
                            : iqFactory.createNaryIQTree(rootNode, liftedChildren));
        }
    }

    private static class SelfJoinUCSimplifier extends AbstractSelfJoinSimplifier<UniqueConstraint> {

        @Inject
        protected SelfJoinUCSimplifier(CoreSingletons coreSingletons) {
            super(coreSingletons);
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

            ImmutableSet<ImmutableExpression> expressions = Stream.concat(
                            NaryIQTreeTools.coOccurringVariablesStream(dataNodes)
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
