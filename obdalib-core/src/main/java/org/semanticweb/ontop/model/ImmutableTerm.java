package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableSet;

/**
 * Term that is guaranteed to be immutable.
 *
 * In the future, every term should be immutable
 */
public interface ImmutableTerm extends Term {

    /**
     * Now is trivial to implement
     */
    @Override
    ImmutableTerm clone();
}
