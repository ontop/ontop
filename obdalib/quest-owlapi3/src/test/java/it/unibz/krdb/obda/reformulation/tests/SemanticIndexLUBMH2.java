package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Tests if QuestOWL can be initialized on top of an existing semantic index
 * created by the SemanticIndexManager.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexLUBMH2 extends TestCase {

	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost/lubmex2050?sessionVariables=sql_mode='ANSI'";
	String username = "root";
	String password = "";

	String owlfile = "src/test/resources/test/lubm-ex-20-uni1/University0-imports.owl";

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OBDADataSource source;

	Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexLUBMH2() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/test/lubm-ex-20-uni1/LUBM-ex-20.owl"));

		source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

	}

	public void test3InitializingQuest() throws Exception {

		QuestOWLFactory fac = new QuestOWLFactory();

		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		fac.setPreferenceHolder(pref);

		QuestOWL quest = (QuestOWL) fac.createReasoner(ontology);

		QuestOWLConnection qconn = (QuestOWLConnection) quest.getConnection();

		QuestOWLStatement st = (QuestOWLStatement) qconn.createStatement();

		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/merge.owl"), 50000, 5000);

		try {
			st.createIndexes();
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}

		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("src/test/resources/test/treewitness/LUBM-ex-20.q");

		for (QueryControllerEntity e : qc.getElements()) {
			if (!(e instanceof QueryControllerQuery)) {
				continue;
			}
			QueryControllerQuery query = (QueryControllerQuery) e;
			log.debug("Executing query: {}", query.getID());
			log.debug("Query: \n{}", query.getQuery());

			// String query =
			// "PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
			// + " SELECT ?student ?staff ?course" + " WHERE { "
			// + "	?student a lubm:Student ." +
			// "	?student lubm:advisor ?staff ." + "	?staff a lubm:Faculty ."
			// + "	?student lubm:takesCourse ?course ." +
			// "	?staff lubm:teacherOf ?course ." + "	?course a lubm:Course . "
			// + " }";

			long start = System.nanoTime();
			QuestOWLResultSet res = (QuestOWLResultSet) st.executeTuple(query.getQuery());
			long end = System.nanoTime();

			double time = (end - start) / 1000;

			int count = 0;
			while (res.nextRow()) {
				count += 1;
			}
			log.debug("Total result: {}", count);
			log.debug("Elapsed time: {} ms", time);
		}
		// }
	}

}
