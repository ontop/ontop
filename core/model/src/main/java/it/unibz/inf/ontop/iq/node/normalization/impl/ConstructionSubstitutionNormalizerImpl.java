package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

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

    @Override
    public ConstructionSubstitutionNormalization normalizeSubstitution(Substitution<?> ascendingSubstitution, ImmutableSet<Variable> projectedVariables) {

        Substitution<?> reducedAscendingSubstitution = ascendingSubstitution.restrictDomainTo(projectedVariables);
        InjectiveSubstitution<Variable> downRenamingSubstitution = substitutionFactory.extractInverseSubstitution(
                        reducedAscendingSubstitution.stream(),
                        projectedVariables)
                .injective();

        Substitution<ImmutableTerm> newAscendingSubstitution = substitutionFactory.rename(downRenamingSubstitution, reducedAscendingSubstitution)
                .transform(ImmutableTerm::simplify);

        return new ConstructionSubstitutionNormalizationImpl(newAscendingSubstitution, projectedVariables, downRenamingSubstitution);
    }

    @Override
    public IQTree createNormalizedConstructionTree(Substitution<? extends ImmutableTerm> substitution, ImmutableSet<Variable> projectedVariables, IQTree child) {
        var normalization = normalizeSubstitution(substitution, projectedVariables);
        return iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                .append(normalization.createOptionalConstructionNode())
                .build(iqTreeTools.applyDownPropagation(normalization.getDownRenamingSubstitution(), child));
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
        public InjectiveSubstitution<Variable> getDownRenamingSubstitution() {
            return downRenamingSubstitution;
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
