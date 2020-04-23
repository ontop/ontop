package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;

public interface NoNullValueEnforcer {

    IQ transform(IQ originalQuery);

    IQTree transform(IQTree tree);
}
