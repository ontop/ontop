package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

public interface TripleRefNestedSubjectPredicate extends TripleRefPredicate, TripleNestedSubjectPredicate {
    <T extends ImmutableTerm> T getTripleReference(ImmutableList<T> atomArguments);

}
