package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinUCIQOptimizer extends DelegatingIQTreeVariableGeneratorTransformer implements IQTreeVariableGeneratorTransformer {

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private SelfJoinUCIQOptimizer(CoreSingletons coreSingletons) {
        this.transformer = IQTreeVariableGeneratorTransformer.of(
                vg -> new DefaultRecursiveIQTreeVisitingInnerJoinTransformer(
                        coreSingletons.getIQFactory(),
                        new SelfJoinUCSimplifier(coreSingletons, vg)));
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    private static class SelfJoinUCSimplifier extends AbstractSelfJoinSimplifier<UniqueConstraint> {

        SelfJoinUCSimplifier(CoreSingletons coreSingletons, VariableGenerator variableGenerator) {
            super(coreSingletons, variableGenerator);
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
