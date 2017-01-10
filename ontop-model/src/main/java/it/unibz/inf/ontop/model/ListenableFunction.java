package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.utils.EventGeneratingList;
import it.unibz.inf.ontop.utils.ListListener;

/**
 * For mutable Function (yes, it exists...)
 * This practice should be stopped in the future.
 */
public interface ListenableFunction extends Function, ListListener {

    @Override
    EventGeneratingList<Term> getTerms();
}
