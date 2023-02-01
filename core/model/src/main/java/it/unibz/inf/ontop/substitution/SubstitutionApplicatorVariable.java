package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public interface SubstitutionApplicatorVariable {
    static Variable apply(ImmutableSubstitution<Variable> substitution, Variable variable) {
        return Optional.ofNullable(substitution.get(variable)).orElse(variable);
    }
    static ImmutableSet<Variable> apply(ImmutableSubstitution<Variable> substitution, ImmutableSet<Variable> terms) {
        return terms.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toSet());
    }
    static ImmutableList<Variable> apply(ImmutableSubstitution<Variable> substitution, ImmutableList<Variable> terms) {
        return terms.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toList());
    }
}
