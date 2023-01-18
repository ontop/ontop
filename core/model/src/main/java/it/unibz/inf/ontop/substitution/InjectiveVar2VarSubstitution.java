package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Set;

/**
 * Var2VarSubstitution that is injective
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveVar2VarSubstitution extends ImmutableSubstitution<Variable> {

    /**
     * Applies it (the Var2VarSubstitution) on the keys and values of the given substitution.
     */
    <T extends ImmutableTerm> ImmutableSubstitution<T> applyRenaming(ImmutableSubstitution<T> substitutionToRename);

    <T extends ImmutableTerm> ImmutableList<T> applyToList(ImmutableList<T> arguments);

    <T extends ImmutableTerm> T applyToTerm(T term);

    @Override // more specific return type
    Variable applyToVariable(Variable variable);

    @Override
    InjectiveVar2VarSubstitution restrictDomainTo(Set<Variable> set);
}