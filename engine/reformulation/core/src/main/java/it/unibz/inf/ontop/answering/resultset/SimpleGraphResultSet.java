package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

public interface SimpleGraphResultSet extends GraphResultSet<OntopResultConversionException> {

    int getFetchSize() throws OntopConnectionException;

    /**
     * TODO: remove this hack
     */
	@Deprecated
    void addNewResult(RDFFact assertion);

}
