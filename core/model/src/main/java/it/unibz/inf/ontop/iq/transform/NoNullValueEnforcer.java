package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.iq.IQ;

public interface NoNullValueEnforcer {

    IQ transform(IQ originalQuery) throws QueryTransformationException;
}
