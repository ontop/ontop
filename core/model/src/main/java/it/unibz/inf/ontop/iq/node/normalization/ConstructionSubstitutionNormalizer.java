package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;

public interface ConstructionSubstitutionNormalizer {

    ConstructionSubstitutionNormalization normalizeSubstitution(
            Substitution<? extends ImmutableTerm> ascendingSubstitution,
            ImmutableSet<Variable> projectedVariables);


    interface ConstructionSubstitutionNormalization {

        Substitution<ImmutableTerm> getNormalizedSubstitution();

        ImmutableSet<Variable> getProjectedVariables();

        InjectiveSubstitution<Variable> getDownRenamingSubstitution();
    }
}
