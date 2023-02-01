package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

public interface SubstitutionApplicatorVariableOrGroundTerm {

    static VariableOrGroundTerm apply(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
        if (t instanceof Variable)
            return Optional.<VariableOrGroundTerm>ofNullable(substitution.get((Variable) t)).orElse(t);

        return t;
    }

    static ImmutableList<VariableOrGroundTerm> apply(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, ImmutableList<? extends VariableOrGroundTerm> terms) {
        return terms.stream()
                .map(t -> apply(substitution, t))
                .collect(ImmutableCollectors.toList());
    }

    static ImmutableMap<Integer, VariableOrGroundTerm> apply(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {
        return argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> apply(substitution, e.getValue())));
    }
}
