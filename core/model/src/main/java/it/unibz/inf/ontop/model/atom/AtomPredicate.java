package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.COL_TYPE;

/**
 * TODO: explain
 *
 *  Most of the time, does not provide any type for its arguments
 */
public interface AtomPredicate extends Predicate {

    @Deprecated
    boolean isClass();

    @Deprecated
    boolean couldBeAnObjectProperty();

    @Deprecated
    boolean couldBeADataProperty();

    @Deprecated
    boolean isTriplePredicate();

}
