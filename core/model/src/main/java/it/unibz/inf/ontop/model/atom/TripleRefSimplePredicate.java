package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

public interface TripleRefSimplePredicate extends TripleRefPredicate {
    <T extends ImmutableTerm> T getTripleReference(ImmutableList<T> atomArguments);

}
