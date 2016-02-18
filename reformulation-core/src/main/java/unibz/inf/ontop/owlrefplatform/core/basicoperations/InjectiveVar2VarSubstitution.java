package unibz.inf.ontop.owlrefplatform.core.basicoperations;

import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.ImmutableTerm;
import unibz.inf.ontop.model.Var2VarSubstitution;

/**
 * Var2VarSubstitution that is injective
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveVar2VarSubstitution extends Var2VarSubstitution {

    /**
     * Applies it (the Var2VarSubstitution) on the keys and values of the given substitution.
     */
    ImmutableSubstitution<ImmutableTerm> applyRenaming(ImmutableSubstitution<? extends ImmutableTerm> substitutionToRename);
}
