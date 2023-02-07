package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Set;

/**
 * An injective substitution
 *    (no value in the substitution map is shared by two keys)
 */
public interface InjectiveSubstitution<T extends ImmutableTerm> extends Substitution<T> {

    @Override
    InjectiveSubstitution<T> restrictDomainTo(Set<Variable> set);

    @Override
    InjectiveSubstitution<T> removeFromDomain(Set<Variable> set);
}