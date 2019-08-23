package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;

/**
 * For mutable Function (yes, it exists...)
 * This practice should be stopped in the future.
 */
public interface ListenableFunction extends Function, ListListener {

    @Override
    EventGeneratingList<Term> getTerms();
}
