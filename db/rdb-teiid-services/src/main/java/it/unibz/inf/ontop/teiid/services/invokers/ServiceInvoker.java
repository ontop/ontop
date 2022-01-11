package it.unibz.inf.ontop.teiid.services.invokers;

import java.util.Iterator;

import it.unibz.inf.ontop.teiid.services.model.Service;
import it.unibz.inf.ontop.teiid.services.model.Tuple;

public interface ServiceInvoker {

    Service getService();

    Iterator<Tuple> invoke(final Tuple tuple);

    Iterator<Tuple> invokeBatch(final Iterable<Tuple> tuples);

}
