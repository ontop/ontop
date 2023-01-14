package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Var2VarSubstitution that is injective
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveVar2VarSubstitution extends ImmutableSubstitution<Variable> {

    /**
     * Applies it (the Var2VarSubstitution) on the keys and values of the given substitution.
     */
    <T extends ImmutableTerm> ImmutableSubstitution<T> applyRenaming(ImmutableSubstitution<T> substitutionToRename);

    ImmutableList<Variable> applyToVariableArguments(ImmutableList<Variable> arguments) throws ConversionException;

    <T extends ImmutableTerm> T applyToTerm(T term);

    @Override // more specific return type
    default Variable applyToVariable(Variable variable) {
        Variable r = get(variable);
        return r == null ? variable : r;
    }

    @Override
    InjectiveVar2VarSubstitution restrictDomainTo(ImmutableSet<Variable> set);

    ImmutableSet<Variable> getRangeSet();
}