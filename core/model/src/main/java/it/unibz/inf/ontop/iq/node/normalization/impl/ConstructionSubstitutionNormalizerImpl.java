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

    /**
     * Prevents creating construction nodes out of ascending substitutions
     *
     * Splits the ascendingSubstitution into the renaming part of the form "p -> x" and
     * the proper CONSTRUCT node substitutions of the form "p -> f(y)" or "p -> a".
     * Note, however, that "p -> x, q -> x" would still retain one of the two components
     * transformed into "p -> q" or "q -> p", respectively, while the other component
     * is moved to the renaming part.
     *
     * Here, variable nullability is not considered due to the complexity induced by the descending substitution
     *
     */
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
        return iqTreeTools.unaryIQTreeBuilder()
                .append(normalization.createConstructionNode())
                .build(normalization.applyDownRenamingSubstitution(child));
    }

    @Override
    public IQTree createNormalizedOptionalConstructionTree(Substitution<? extends ImmutableTerm> substitution, ImmutableSet<Variable> projectedVariables, IQTree child) {
        var normalization = normalizeSubstitution(substitution, projectedVariables);
        return iqTreeTools.unaryIQTreeBuilder(projectedVariables)
                .append(normalization.createOptionalConstructionNode())
                .build(normalization.applyDownRenamingSubstitution(child));
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
