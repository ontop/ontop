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

    ConstructionSubstitutionNormalization normalizeSubstitution(
            Substitution<? extends ImmutableTerm> ascendingSubstitution,
            ImmutableSet<Variable> projectedVariables);


    interface ConstructionSubstitutionNormalization {

        IQTree applyDownRenamingSubstitution(IQTree tree);

        ImmutableExpression applyDownRenamingSubstitution(ImmutableExpression expression);

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
