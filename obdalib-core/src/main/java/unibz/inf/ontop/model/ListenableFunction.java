package unibz.inf.ontop.model;

import unibz.inf.ontop.utils.EventGeneratingList;
import unibz.inf.ontop.utils.ListListener;

/**
 * For mutable Function (yes, it exists...)
 * This practice should be stopped in the future.
 */
public interface ListenableFunction extends Function, ListListener {

    @Override
    EventGeneratingList<Term> getTerms();
}
