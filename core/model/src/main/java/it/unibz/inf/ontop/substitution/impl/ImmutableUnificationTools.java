package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;

import java.util.*;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    public ImmutableUnificationTools(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    public Optional<ImmutableSubstitution<ImmutableTerm>> computeMGU(ImmutableTerm args1, ImmutableTerm args2) {
        return substitutionFactory.onImmutableTerms().unifierBuilder().unify(args1, args2).build();
    }





    public InjectiveVar2VarSubstitution getPrioritizingRenaming(ImmutableSubstitution<?> substitution, ImmutableSet<Variable> priorityVariables) {
        ImmutableSubstitution<Variable> renaming = substitution.builder()
                .restrictDomainTo(priorityVariables)
                .restrictRangeTo(Variable.class)
                .restrictRange(t -> !priorityVariables.contains(t))
                .build();

        return substitutionFactory.extractAnInjectiveVar2VarSubstitutionFromInverseOf(renaming);
    }


}
