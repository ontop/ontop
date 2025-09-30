package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * TODO: find a better name
 */
public class ConstructionSubstitutionNormalizerImpl implements ConstructionSubstitutionNormalizer {

    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private ConstructionSubstitutionNormalizerImpl(SubstitutionFactory substitutionFactory, IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
    }

    /**
     * Prevents creating construction nodes out of ascending substitutions
     *
     * Here, variable nullability is not considered due to the complexity induced by the descending substitution
     *
     */
    @Override
    public ConstructionSubstitutionNormalization normalizeSubstitution(Substitution<?> ascendingSubstitution, ImmutableSet<Variable> projectedVariables) {

        InjectiveSubstitution<Variable> downRenamingSubstitution = substitutionFactory.getPrioritizingRenaming(ascendingSubstitution, projectedVariables);

        Substitution<?> reducedAscendingSubstitution = ascendingSubstitution.restrictDomainTo(projectedVariables);

        Substitution<ImmutableTerm> newAscendingSubstitution = downRenamingSubstitution.compose(reducedAscendingSubstitution).builder()
                .restrictDomainTo(projectedVariables)
                .transform(ImmutableTerm::simplify)
                .build();

        return new ConstructionSubstitutionNormalizationImpl(newAscendingSubstitution, projectedVariables, downRenamingSubstitution);
    }


    private class ConstructionSubstitutionNormalizationImpl implements ConstructionSubstitutionNormalization {

        private final Substitution<ImmutableTerm> normalizedSubstitution;
        private final ImmutableSet<Variable> projectedVariables;
        private final InjectiveSubstitution<Variable> downRenamingSubstitution;

        private ConstructionSubstitutionNormalizationImpl(Substitution<ImmutableTerm> normalizedSubstitution, ImmutableSet<Variable> projectedVariables,
                                                          InjectiveSubstitution<Variable> downRenamingSubstitution) {
            this.normalizedSubstitution = normalizedSubstitution;
            this.projectedVariables = projectedVariables;
            this.downRenamingSubstitution = downRenamingSubstitution;
        }

        @Override
        public IQTree applyDownRenamingSubstitution(IQTree tree) {
            return iqTreeTools.applyDownPropagation(downRenamingSubstitution, tree);
        }

        @Override
        public ImmutableExpression applyDownRenamingSubstitution(ImmutableExpression expression) {
            return downRenamingSubstitution.apply(expression);
        }

        @Override
        public ConstructionNode createConstructionNode() {
            return iqFactory.createConstructionNode(projectedVariables, normalizedSubstitution);
        }

        @Override
        public Optional<ConstructionNode> createOptionalConstructionNode() {
            return iqTreeTools.createOptionalConstructionNode(() -> projectedVariables, normalizedSubstitution);
        }
    }
}
