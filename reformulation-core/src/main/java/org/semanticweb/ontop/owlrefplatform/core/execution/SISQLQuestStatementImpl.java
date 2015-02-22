package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.IQuest;
import org.semanticweb.ontop.owlrefplatform.core.IQuestConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestConnection;
import org.semanticweb.ontop.owlrefplatform.core.SQLQuestStatement;
import org.semanticweb.ontop.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import org.semanticweb.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

/**
 * Contains additional methods required by the Semantic Index mode.
 */
public class SISQLQuestStatementImpl extends SQLQuestStatement implements SIQuestStatement {

    private final Connection sqlConnection;
    private final RDBMSSIRepositoryManager siRepository;
    private static Logger log = LoggerFactory.getLogger(SISQLQuestStatementImpl.class);

    public SISQLQuestStatementImpl(IQuest questInstance, QuestConnection questConnection, Statement sqlStatement) {
        super(questInstance, questConnection, sqlStatement);
        sqlConnection = questConnection.getSQLConnection();
        siRepository = questInstance.getSemanticIndexRepository();
        if (siRepository == null) {
            throw new IllegalArgumentException(getClass().getCanonicalName() + " requires the quest instance to have a Semantic Index repository.");
        }
    }

    /***
     * As before, but using recreateIndexes = false.
     *
     * @param data
     * @throws java.sql.SQLException
     */
    public int insertData(Iterator<Assertion> data, int commit, int batch) throws SQLException {
        return insertData(data, false, commit, batch);
    }

    public void createIndexes() throws Exception {
        siRepository.createIndexes(sqlConnection);
    }

    public void dropIndexes() throws Exception {
        siRepository.dropIndexes(sqlConnection);
    }

    public boolean isIndexed() {
        return siRepository.isIndexed(sqlConnection);
    }

    public void dropRepository() throws SQLException {
        siRepository.dropDBSchema(sqlConnection);
    }

    /***
     * In an ABox store (classic) this methods triggers the generation of the
     * schema and the insertion of the metadata.
     *
     * @throws SQLException
     */
    public void createDB() throws SQLException {
        siRepository.createDBSchema(sqlConnection, false);
        siRepository.insertMetadata(sqlConnection);
    }

    public void analyze() throws Exception {
        siRepository.collectStatistics(sqlConnection);
    }

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
    public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws SQLException {
        int result = -1;

        EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(data, getQuestInstance().getReasoner());

        if (!useFile) {

            result = siRepository.insertData(sqlConnection, newData, commit, batch);
        } else {
            try {
                // File temporalFile = new File("quest-copy.tmp");
                // FileOutputStream os = new FileOutputStream(temporalFile);
                result = (int) siRepository.loadWithFile(sqlConnection, newData);
                // os.close();

            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        try {
            getQuestInstance().updateSemanticIndexMappings();
            getTranslator().setTemplateMatcher(getQuestInstance().getUriTemplateMatcher());

        } catch (Exception e) {
            log.error("Error updating semantic index mappings after insert.", e);
        }

        return result;
    }
}
