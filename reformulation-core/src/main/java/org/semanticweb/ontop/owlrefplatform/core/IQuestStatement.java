package org.semanticweb.ontop.owlrefplatform.core;

import org.openrdf.query.parser.ParsedQuery;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAStatement;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.ontology.Assertion;

import java.util.Iterator;

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
    IQuest getQuestInstance();

    String getRewriting(ParsedQuery query) throws OBDAException;

    ExecutableQuery unfoldAndGenerateTargetQuery(String sparqlQuery) throws OBDAException;

    boolean isCanceled();

    //------------------
    // Methods for getting the benchmark parameters
    //------------------

    long getQueryProcessingTime();

    long getRewritingTime();

    long getUnfoldingTime();

    int getUCQSizeAfterRewriting();

    int getUCQSizeAfterUnfolding();

    /**
     * TODO: understand and implement correctly.
     */
    public TupleResultSet getResultSet() throws OBDAException;

    /**
     * Not always supported (for instance, write mode is not yet supported for the virtual mode).
     */
    int insertData(Iterator<Assertion> data, int commit, int batch) throws OBDAException;
}
