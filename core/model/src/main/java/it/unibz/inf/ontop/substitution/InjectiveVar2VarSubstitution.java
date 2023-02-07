package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Set;

/**
 * Var2VarSubstitution that is injective
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveVar2VarSubstitution extends ImmutableSubstitution<Variable> {


    @Override
    InjectiveVar2VarSubstitution restrictDomainTo(Set<Variable> set);
//    @Override
//    InjectiveVar2VarSubstitution removeFromDomain(Set<Variable> set);
}