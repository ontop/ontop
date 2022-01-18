package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Var2VarSubstitution that is injective
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveVar2VarSubstitution extends Var2VarSubstitution {

    /**
     * Applies it (the Var2VarSubstitution) on the keys and values of the given substitution.
     */
    <T extends ImmutableTerm> ImmutableSubstitution<T> applyRenaming(ImmutableSubstitution<T> substitutionToRename);

    ImmutableList<Variable> applyToVariableArguments(ImmutableList<Variable> arguments) throws ConversionException;

    /**
     * { (x,y) | (x,y) \in (this o otherSubstitution), x not excluded }
     *
     *
     * Returns Optional.empty() when the resulting substitution is not injective (anymore).
     *
     * Variables to exclude from the domain are typically fresh temporary variables that can be ignored.
     * Ignoring them is sufficient in many cases to guarantee that the substitution is injective.
     *
     */
    Optional<InjectiveVar2VarSubstitution> composeWithAndPreserveInjectivity(InjectiveVar2VarSubstitution otherSubstitution,
                                                                             Set<Variable> variablesToExcludeFromTheDomain);

    @Override
    InjectiveVar2VarSubstitution filter(Predicate<Variable> filter);
}
