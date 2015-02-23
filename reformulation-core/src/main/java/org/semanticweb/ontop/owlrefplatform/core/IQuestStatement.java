package org.semanticweb.ontop.owlrefplatform.core;

import org.openrdf.query.parser.ParsedQuery;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAStatement;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.ontology.Assertion;

import java.util.Iterator;
import java.util.List;

/**
 * TODO: describe.
 */
public interface IQuestStatement extends OBDAStatement {
    IQuest getQuestInstance();

    String getRewriting(ParsedQuery query, List<String> signature) throws OBDAException;

    TargetQuery unfoldAndGenerateTargetQuery(String sparqlQuery) throws OBDAException;

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

    /**
     * Not always supported (for instance, write mode is not yet supported for the virtual mode).
     */
    int insertData(Iterator<Assertion> data, int commit, int batch) throws OBDAException;

    /***
     * Inserts a stream of ABox assertions into the repository.
     *
     * @param data

     *            Indicates if indexes (if any) should be dropped before
     *            inserting the tuples and recreated afterwards. Note, if no
     *            index existed before the insert no drop will be done and no
     *            new index will be created.

     * @throws java.sql.SQLException
     */
    public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws OBDAException;
}
