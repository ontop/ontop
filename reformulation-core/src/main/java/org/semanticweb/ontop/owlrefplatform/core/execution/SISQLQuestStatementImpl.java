package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.model.OBDAException;
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

    public void createIndexes() throws OBDAException {
        try {
            siRepository.createIndexes(sqlConnection);
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    public void dropIndexes() throws OBDAException {
        try {
            siRepository.dropIndexes(sqlConnection);
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    public boolean isIndexed() {
        return siRepository.isIndexed(sqlConnection);
    }

    public void dropRepository() throws OBDAException {
        try {
            siRepository.dropDBSchema(sqlConnection);
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    /***
     * In an ABox store (classic) this methods triggers the generation of the
     * schema and the insertion of the metadata.
     *
     */
    public void createDB() throws OBDAException {
        try {
            siRepository.createDBSchema(sqlConnection, false);
            siRepository.insertMetadata(sqlConnection);
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    public void analyze() throws OBDAException {
        try {
            siRepository.collectStatistics(sqlConnection);
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    /***
     * Inserts a stream of ABox assertions into the repository.
     *
     * @param data

     *            Indicates if indexes (if any) should be dropped before
     *            inserting the tuples and recreated afterwards. Note, if no
     *            index existed before the insert no drop will be done and no
     *            new index will be created.
     *
     */
    public int insertData(Iterator<Assertion> data, boolean useFile, int commit, int batch) throws OBDAException {
        int result = -1;

        EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(data, getQuestInstance().getReasoner());

        if (!useFile) {

            try {
                result = siRepository.insertData(sqlConnection, newData, commit, batch);
            } catch (SQLException e) {
                throw new OBDAException(e);
            }
        } else {
            try {
                // File temporalFile = new File("quest-copy.tmp");
                // FileOutputStream os = new FileOutputStream(temporalFile);
                result = (int) siRepository.loadWithFile(sqlConnection, newData);
                // os.close();

            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (SQLException e) {
                throw new OBDAException(e);
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

    /***
     * As before, but using recreateIndexes = false.
     */
    public int insertData(Iterator<Assertion> data, int commit, int batch) throws OBDAException {
        return insertData(data, false, commit, batch);
    }
}
