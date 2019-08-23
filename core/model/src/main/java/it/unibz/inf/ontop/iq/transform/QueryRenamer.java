package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQ;

public interface QueryRenamer {

    IQ transform(IQ originalQuery);
}
