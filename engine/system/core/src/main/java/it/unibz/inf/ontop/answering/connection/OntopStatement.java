package it.unibz.inf.ontop.answering.connection;

import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.iq.IQ;

/**
 * OBDAStatement specific to Ontop.
 *
 * This interface gives access to inner steps of the SPARQL answering process for analytical purposes.
 * Also provides some benchmarking information.
 *
 * It also allows to prepare the IQ from outside. Used for predefined queries.
 *
 */
public interface OntopStatement extends OBDAStatement {

    <R extends OBDAResultSet> int getTupleCount(InputQuery<R> inputQuery) throws OntopReformulationException, OntopQueryEvaluationException, OntopConnectionException;

    <R extends OBDAResultSet> String getRewritingRendering(InputQuery<R> inputQuery) throws OntopReformulationException;

    <R extends OBDAResultSet> IQ getExecutableQuery(InputQuery<R> inputQuery) throws OntopReformulationException;

    TupleResultSet executeSelectQuery(IQ executableQuery, QueryLogger queryLogger)
            throws OntopQueryEvaluationException;

    GraphResultSet executeConstructQuery(ConstructTemplate constructTemplate, IQ executableQuery, QueryLogger queryLogger)
            throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException;

    BooleanResultSet executeBooleanQuery(IQ executableQuery, QueryLogger queryLogger)
            throws OntopQueryEvaluationException;

}
