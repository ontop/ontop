package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

/**
 * TODO: try to get rid of RDF4J interfaces here
 *
 */
public interface OntopQueryReformulator {

    ParsedQuery getParsedQuery(String sparql) throws OntopInvalidInputQueryException;

    ExecutableQuery translateIntoNativeQuery(ParsedQuery pq,
                                             Optional<SesameConstructTemplate> optionalConstructTemplate)
            throws OntopReformulationException;

    String getRewriting(ParsedQuery query) throws OntopReformulationException;

    boolean hasDistinctResultSet();

    DBMetadata getDBMetadata();
}
