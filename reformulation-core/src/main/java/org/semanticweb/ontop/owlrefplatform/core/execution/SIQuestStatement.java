package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.IQuestStatement;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Special QuestStatement interface for Semantic Index repositories.
 *
 * TODO: Replace SQLException by TargetQueryExecutionException
 * to make this interface SQL-independent.
 */
public interface SIQuestStatement extends IQuestStatement {

    int insertData(Iterator<Assertion> data, int commit, int batch) throws SQLException;

    void createIndexes() throws Exception;

    void dropIndexes() throws Exception;

    boolean isIndexed();

    void dropRepository() throws SQLException;

    public void createDB() throws SQLException;
    public void analyze() throws Exception;
    /***
     * Inserts a stream of ABox assertions into the repository.
     *
     * @param data

     *            Indicates if indexes (if any) should be dropped before
     *            inserting the tuples and recreated afterwards. Note, if no
     *            index existed before the insert no drop will be done and no
     *            new index will be created.

     * @throws SQLException
     */
    public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws SQLException ;
}
