package org.semanticweb.ontop.model;

import org.semanticweb.ontop.utils.EventGeneratingList;
import org.semanticweb.ontop.utils.ListListener;

/**
 * For mutable Function (yes, it exists...)
 * This practice should be stopped in the future.
 */
public interface ListenableFunction extends Function, ListListener {

    @Override
    EventGeneratingList<Term> getTerms();
}
