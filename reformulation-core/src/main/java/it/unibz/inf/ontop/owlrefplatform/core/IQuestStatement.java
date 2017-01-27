package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAStatement;
import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import it.unibz.inf.ontop.reformulation.IRIDictionary;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Iterator;
import java.util.Optional;

/**
 * OBDAStatement specific to Quest.
 *
 * This interface gives access to inner steps of the SPARQL answering process for analytical purposes.
 * Also provides some benchmarking information.
 *
 * May also support data insertion (usually in classic mode but maybe (in the future) also in virtual mode).
 *
 */
public interface IQuestStatement extends OBDAStatement {

    /**
     * TODO: understand and implement correctly.
     */
    TupleResultSet getResultSet() throws OBDAException;

    /**
     * Not always supported (for instance, write mode is not yet supported for the virtual mode).
     */
    @Deprecated
    int insertData(Iterator<Assertion> data, int commit, int batch) throws OBDAException;

    int getTupleCount(String sparqlQuery);


    DBMetadata getMetadata();

    @Deprecated
    ParsedQuery getParsedQuery(String query);

    @Deprecated
    String getRewriting(ParsedQuery query);

    @Deprecated
    ExecutableQuery translateIntoNativeQuery(ParsedQuery pq, Optional<SesameConstructTemplate> constructTemplate);

    Optional<IRIDictionary> getIRIDictionary();
}
