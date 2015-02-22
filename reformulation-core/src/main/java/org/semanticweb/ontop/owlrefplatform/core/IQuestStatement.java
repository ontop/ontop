package org.semanticweb.ontop.owlrefplatform.core;

import org.openrdf.query.parser.ParsedQuery;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAStatement;
import org.semanticweb.ontop.model.TupleResultSet;

import java.util.List;

/**
 * TODO: describe.
 */
public interface IQuestStatement extends OBDAStatement {
    IQuest getQuestInstance();

    String getRewriting(ParsedQuery query, List<String> signature) throws Exception;

    TargetQuery unfoldAndGenerateTargetQuery(String sparqlQuery) throws Exception;

    boolean isCanceled();

    /**
     * Methods for getting the benchmark parameters
     */

    long getQueryProcessingTime();

    long getRewritingTime();

    long getUnfoldingTime();

    int getUCQSizeAfterRewriting();

    int getUCQSizeAfterUnfolding();

    /**
     * TODO: understand and implement correctly.
     */
    public TupleResultSet getResultSet() throws OBDAException;
}
