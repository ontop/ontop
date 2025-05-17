package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * TODO: find a better name
 */
public class ConstructionSubstitutionNormalizerImpl implements ConstructionSubstitutionNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private ConstructionSubstitutionNormalizerImpl(IntermediateQueryFactory iqFactory,
                                                   SubstitutionFactory substitutionFactory,
                                                   IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
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

        return new ConstructionSubstitutionNormalizationImpl(newAscendingSubstitution, downRenamingSubstitution);
    }


    public class ConstructionSubstitutionNormalizationImpl implements ConstructionSubstitutionNormalization {

        private final Substitution<ImmutableTerm> normalizedSubstitution;
        private final InjectiveSubstitution<Variable> downRenamingSubstitution;

        private ConstructionSubstitutionNormalizationImpl(Substitution<ImmutableTerm> normalizedSubstitution,
                                                          InjectiveSubstitution<Variable> downRenamingSubstitution) {
            this.normalizedSubstitution = normalizedSubstitution;
            this.downRenamingSubstitution = downRenamingSubstitution;
        }

        @Override
        public IQTree updateChild(IQTree child, VariableGenerator variableGenerator) {
            DownPropagation dc = new DownPropagation(ImmutableSet.of());
            return dc.applyDescendingSubstitution(child, downRenamingSubstitution, variableGenerator);
        }

        @Override
        public ImmutableExpression updateExpression(ImmutableExpression expression) {
            return downRenamingSubstitution.apply(expression);
        }

        @Override
        public Substitution<ImmutableTerm> getNormalizedSubstitution() {
            return normalizedSubstitution;
        }
    }
}
