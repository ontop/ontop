package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 */
public interface ImmutableSubstitution extends LocallyImmutableSubstitution {

    ImmutableMap<VariableImpl, ImmutableTerm> getImmutableMap();

    boolean isDefining(VariableImpl variable);

    @Override
    ImmutableTerm get(VariableImpl variable);

   /**
    * Applies the substitution to an immutable term.
    */
    ImmutableTerm apply(ImmutableTerm term);

    ImmutableTerm applyToVariable(VariableImpl variable);

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);
}
