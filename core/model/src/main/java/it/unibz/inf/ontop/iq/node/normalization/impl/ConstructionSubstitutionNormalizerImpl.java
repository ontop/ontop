package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * TODO: find a better name
 */
public class ConstructionSubstitutionNormalizerImpl implements ConstructionSubstitutionNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private ConstructionSubstitutionNormalizerImpl(IntermediateQueryFactory iqFactory,
                                                   SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Prevents creating construction nodes out of ascending substitutions
     *
     * Here, variable nullability is not considered due to the complexity induced by the descending substitution
     *
     */
    @Override
    public ConstructionSubstitutionNormalization normalizeSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> ascendingSubstitution, ImmutableSet<Variable> projectedVariables) {

        ImmutableSubstitution<? extends ImmutableTerm> reducedAscendingSubstitution = ascendingSubstitution.restrictDomainTo(projectedVariables);

        InjectiveVar2VarSubstitution downRenamingSubstitution = substitutionFactory.extractAnInjectiveVar2VarSubstitutionFromInverseOf(
                reducedAscendingSubstitution.builder()
                        .restrictRangeTo(Variable.class)
                        .restrictRange(t -> !projectedVariables.contains(t))
                        .build());

        ImmutableSubstitution<ImmutableTerm> newAscendingSubstitution = substitutionFactory.compose(downRenamingSubstitution, reducedAscendingSubstitution).builder()
                .restrictDomainTo(projectedVariables)
                .transform(t -> t.simplify())
                .build();

        return new ConstructionSubstitutionNormalizationImpl(newAscendingSubstitution, downRenamingSubstitution,
                projectedVariables);
    }


    public class ConstructionSubstitutionNormalizationImpl implements ConstructionSubstitutionNormalization {

        private final ImmutableSubstitution<ImmutableTerm> normalizedSubstitution;
        private final InjectiveVar2VarSubstitution downRenamingSubstitution;
        private final ImmutableSet<Variable> projectedVariables;

        private ConstructionSubstitutionNormalizationImpl(ImmutableSubstitution<ImmutableTerm> normalizedSubstitution,
                                                          InjectiveVar2VarSubstitution downRenamingSubstitution,
                                                          ImmutableSet<Variable> projectedVariables) {
            this.normalizedSubstitution = normalizedSubstitution;
            this.downRenamingSubstitution = downRenamingSubstitution;
            this.projectedVariables = projectedVariables;
        }

        @Override
        public Optional<ConstructionNode> generateTopConstructionNode() {
            return Optional.of(normalizedSubstitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(projectedVariables, s));

        }

        @Override
        public IQTree updateChild(IQTree child, VariableGenerator variableGenerator) {
            return downRenamingSubstitution.isEmpty()
                    ? child
                    : child.applyDescendingSubstitution(downRenamingSubstitution, Optional.empty(), variableGenerator);
        }

        @Override
        public ImmutableExpression updateExpression(ImmutableExpression expression) {
            return downRenamingSubstitution.apply(expression);
        }

        @Override
        public ImmutableSubstitution<ImmutableTerm> getNormalizedSubstitution() {
            return normalizedSubstitution;
        }
    }
}
