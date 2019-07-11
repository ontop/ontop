package it.unibz.inf.ontop.substitution.impl;

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.substitution.LocallyImmutableSubstitution;


/**
 * Only implements the non supported methods
 */
public abstract class LocallyImmutableSubstitutionImpl implements LocallyImmutableSubstitution {


    @Deprecated
    @Override
    public boolean composeTerms(Term term1, Term term2) {
        throw new UnsupportedOperationException("Mutable operations are not supported.");
    }

    @Deprecated
    @Override
    public boolean composeFunctions(Function term1, Function term2) {
        throw new UnsupportedOperationException("Mutable operations are not supported.");
    }
}
