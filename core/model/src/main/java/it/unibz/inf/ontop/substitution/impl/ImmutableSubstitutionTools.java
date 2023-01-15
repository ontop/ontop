package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;

/**
 * Tools for the new generation of (immutable) substitutions
 */

public class ImmutableSubstitutionTools {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    private ImmutableSubstitutionTools(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Prevents priority variables to be renamed into non-priority variables.
     *
     * When applied to a MGU, it is expected to return another "equivalent" MGU.
     *
     */
    public <T extends ImmutableTerm> ImmutableSubstitution<T> prioritizeRenaming(
            ImmutableSubstitution<T> substitution, ImmutableSet<Variable> priorityVariables) {

        ImmutableSubstitution<Variable> renaming = substitution.builder()
                .restrictDomainTo(priorityVariables)
                .restrictRangeTo(Variable.class)
                .restrict((v, t) -> !priorityVariables.contains(t))
                .build();

        if (renaming.isEmpty())
            return substitution;

        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.extractAnInjectiveVar2VarSubstitutionFromInverse(renaming);

        // TODO: refactor
        return (ImmutableSubstitution<T>) substitutionFactory.compose(renamingSubstitution, substitution);
    }
}
