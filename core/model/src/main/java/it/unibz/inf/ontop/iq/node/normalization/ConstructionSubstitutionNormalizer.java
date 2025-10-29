package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Optional;

public interface ConstructionSubstitutionNormalizer {

    /**
     * Prevents creating construction nodes out of ascending substitutions
     *
     * Splits the ascendingSubstitution into the renaming part of the form "p -> x" and
     * the proper CONSTRUCT node substitutions of the form "p -> f(y)" or "p -> a".
     * Note, however, that "p -> x, q -> x" would still retain one of the two components
     * transformed into "p -> q" or "q -> p", respectively, while the other component
     * is moved to the renaming part.
     *
     * The renaming part maps *some* variables from the range of the ascendingSubstitution
     * to its domain.
     *
     * Here, variable nullability is not considered due to the complexity induced by the descending substitution
     *
     */

    ConstructionSubstitutionNormalization normalizeSubstitution(
            Substitution<? extends ImmutableTerm> ascendingSubstitution,
            ImmutableSet<Variable> projectedVariables);

    IQTree createNormalizedConstructionTree(Substitution<? extends ImmutableTerm> substitution, ImmutableSet<Variable> projectedVariables, IQTree child);

    interface ConstructionSubstitutionNormalization {

        InjectiveSubstitution<Variable> getDownRenamingSubstitution();

        /**
         * Can be called only for non-empty normalized substitution
         * @return
         */
        ConstructionNode createConstructionNode();

        /**
         * Assumes that the child of the construction node has the same projected variables.
         * @return
         */

        Optional<ConstructionNode> createOptionalConstructionNode();
    }
}
