package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public interface SubstitutionApplicatorImmutableTerm {
    static  ImmutableTerm apply(ImmutableSubstitution<ImmutableTerm> substitution, Variable variable) {
        return Optional.ofNullable(substitution.get(variable)).orElse(variable);
    }
    static ImmutableList<ImmutableTerm> apply(ImmutableSubstitution<ImmutableTerm> substitution, ImmutableList<Variable> terms) {
        return terms.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toList());
    }
}
