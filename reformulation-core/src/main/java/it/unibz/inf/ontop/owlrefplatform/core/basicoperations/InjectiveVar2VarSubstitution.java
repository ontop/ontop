package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Var2VarSubstitution;

import java.util.Optional;

/**
 * Var2VarSubstitution that is injective
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveVar2VarSubstitution extends Var2VarSubstitution {

    /**
     * Applies it (the Var2VarSubstitution) on the keys and values of the given substitution.
     */
    ImmutableSubstitution<ImmutableTerm> applyRenaming(ImmutableSubstitution<? extends ImmutableTerm> substitutionToRename);

    /**
     * (this o otherSubstitution)
     *
     *
     * Returns Optional.empty() when the composition is not injective (anymore).
     *
     */
    Optional<InjectiveVar2VarSubstitution> composeWithAndPreserveInjectivity(InjectiveVar2VarSubstitution otherSubstitution);


}
