package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

public interface SubstitutionApplicatorNonFunctionalTerm {
    static NonFunctionalTerm apply(ImmutableSubstitution<? extends NonFunctionalTerm> substitution, NonFunctionalTerm t) {
        if (t instanceof Variable)
            return Optional.<NonFunctionalTerm>ofNullable(substitution.get((Variable) t)).orElse(t);

        return t;
    }
}
