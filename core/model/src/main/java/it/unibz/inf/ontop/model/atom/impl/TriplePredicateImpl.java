package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.type.TermType;

public class TriplePredicateImpl extends RDFAtomPredicateImpl implements TriplePredicate {

    protected TriplePredicateImpl(ImmutableList<TermType> expectedBaseTypes) {
        super("triple", 3, expectedBaseTypes,1, 2);
    }
}
