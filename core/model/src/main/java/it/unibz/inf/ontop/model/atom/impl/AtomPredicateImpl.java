package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.type.TermType;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {

    private final ImmutableList<TermType> baseTypesForValidation;

    protected AtomPredicateImpl(String name, ImmutableList<TermType> baseTypesForValidation) {
        super(name, baseTypesForValidation.size());
        this.baseTypesForValidation = baseTypesForValidation;
    }

    @Override
    public ImmutableList<TermType> getBaseTypesForValidation() {
        return baseTypesForValidation;
    }
}
