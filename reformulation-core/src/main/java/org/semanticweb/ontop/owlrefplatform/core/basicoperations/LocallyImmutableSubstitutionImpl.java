package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Only implements the non supported methods
 */
public abstract class LocallyImmutableSubstitutionImpl implements LocallyImmutableSubstitution {

    @Override
    public boolean compose(Substitution s) {
        throw new UnsupportedOperationException("Mutable operations are not supported.");
    }

    @Override
    public boolean composeTerms(Term term1, Term term2) {
        throw new UnsupportedOperationException("Mutable operations are not supported.");
    }

    @Override
    public boolean composeFunctions(Function term1, Function term2) {
        throw new UnsupportedOperationException("Mutable operations are not supported.");
    }
}
