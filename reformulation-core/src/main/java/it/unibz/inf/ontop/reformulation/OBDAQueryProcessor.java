package it.unibz.inf.ontop.reformulation;


import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public interface OBDAQueryProcessor {
    ParsedQuery getParsedQuery(String sparql) throws MalformedQueryException;

    ExecutableQuery translateIntoNativeQuery(ParsedQuery pq,
                                             Optional<SesameConstructTemplate> optionalConstructTemplate)
            throws OBDAException;

    String getRewriting(ParsedQuery query) throws OBDAException;

    String getSPARQLRewriting(String sparql) throws OBDAException;

    boolean hasDistinctResultSet();

    DBMetadata getDBMetadata();

    Optional<IRIDictionary> getIRIDictionary();
}
