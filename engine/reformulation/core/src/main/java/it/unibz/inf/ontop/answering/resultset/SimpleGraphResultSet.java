package it.unibz.inf.ontop.answering.resultset;

import org.eclipse.rdf4j.model.Statement;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopConnectionIterationException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;

public interface SimpleGraphResultSet extends GraphResultSet<OntopResultConversionException, OntopConnectionIterationException> {

    int getFetchSize() throws OntopConnectionException;

    /**
     * TODO: remove this hack
     */
    @Deprecated
    void addNewResult(Statement statement);

    void addStatementClosable(AutoCloseable sqlStatement);
}
