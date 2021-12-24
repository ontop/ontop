package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;

public interface IQEqualityCheck {

    default boolean equal(IQ iq1, IQ iq2) { return iq1.equals(iq2); }
}
