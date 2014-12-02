package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;

import java.sql.SQLException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestStatementSIRepository {
	
	private static final Logger log = LoggerFactory.getLogger(QuestStatementSIRepository.class);
	
	private final Quest questInstance;
	private final QuestConnection conn;
	private final SparqlAlgebraToDatalogTranslator translator;
	private final RDBMSSIRepositoryManager si;
	
	public QuestStatementSIRepository(Quest questInstance, QuestConnection conn, SparqlAlgebraToDatalogTranslator translator) {
		this.questInstance = questInstance;
		this.conn = conn;
		this.translator = translator;
		this.si = questInstance.getSemanticIndexRepository();
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
	public int insertData(Iterator<Assertion> data,  int commit, int batch) throws SQLException {
		int result = -1;

		EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(data, questInstance.getReasoner());

//		if (!useFile) {

			result = si.insertData(conn.getConnection(), newData, commit, batch);
//		} else {
			//try {
				// File temporalFile = new File("quest-copy.tmp");
				// FileOutputStream os = new FileOutputStream(temporalFile);
				// ROMAN: this called DOES NOTHING
				// result = (int) questInstance.getSemanticIndexRepository().loadWithFile(conn.conn, newData);
				// os.close();

			//} catch (IOException e) {
			//	log.error(e.getMessage());
			//}
//		}

		try {
			questInstance.updateSemanticIndexMappings();
			translator.setTemplateMatcher(questInstance.getUriTemplateMatcher());

		} catch (Exception e) {
			log.error("Error updating semantic index mappings after insert.", e);
		}

		return result;
	}


	public void createIndexes() throws Exception {
		si.createIndexes(conn.getConnection());
	}

	public void dropIndexes() throws Exception {
		si.dropIndexes(conn.getConnection());
	}

	public boolean isIndexed() {
		if (si == null)
			return false;
		return si.isIndexed(conn.getConnection());
	}

	public void dropRepository() throws SQLException {
		if (si == null)
			return;
		si.dropDBSchema(conn.getConnection());
	}

	/***
	 * In an ABox store (classic) this methods triggers the generation of the
	 * schema and the insertion of the metadata.
	 * 
	 * @throws SQLException
	 */
	public void createRepository() throws SQLException {
		si.createDBSchemaAndInsertMetadata(conn.getConnection());
	}

}
