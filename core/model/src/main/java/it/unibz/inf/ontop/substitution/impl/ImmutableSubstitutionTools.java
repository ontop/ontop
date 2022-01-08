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

        ImmutableMultimap<Variable, Variable> renamingMultimap = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> priorityVariables.contains(e.getKey())
                        && (e.getValue() instanceof Variable)
                        && (!priorityVariables.contains(e.getValue())))
                .collect(ImmutableCollectors.toMultimap(
                        e -> (Variable) e.getValue(),
                        Map.Entry::getKey));

        if (renamingMultimap.isEmpty())
            return substitution;

        ImmutableMap<Variable, Variable> renamingMap = renamingMultimap.asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().iterator().next()));
        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(renamingMap);

        // TODO: refactor
        return (ImmutableSubstitution<T>) renamingSubstitution.composeWith(substitution);
    }
}
