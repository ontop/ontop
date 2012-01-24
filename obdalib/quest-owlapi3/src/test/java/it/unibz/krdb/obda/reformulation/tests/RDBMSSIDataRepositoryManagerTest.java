package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDBMSSIDataRepositoryManagerTest extends TestCase {

	OBDADataFactory	fac	= OBDADataFactoryImpl.getInstance();

	Logger			log	= LoggerFactory.getLogger(RDBMSSIDataRepositoryManagerTest.class);

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDBCreationFromString() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
		OWLAPI2Translator trans = new OWLAPI2Translator();
		OWLAPI2VocabularyExtractor ext = new OWLAPI2VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(ontology);

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdumptestx1";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);

		RDBMSDataRepositoryManager dbman = new RDBMSSIRepositoryManager();
		dbman.setVocabulary(preds);
		dbman.setTBox(trans.translate(ontology));

		Statement st = conn.createStatement();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dbman.getTablesDDL(out);
//		//System.out.println(out.toString());
		st.executeUpdate(out.toString());
		out.reset();

		dbman.getMetadataSQLInserts(out);
//		//System.out.println(out.toString());
		st.executeUpdate(out.toString());
		out.reset();

		OWLAPI2ABoxIterator ait = new OWLAPI2ABoxIterator(ontology);
		dbman.getSQLInserts(ait, out);
//		//System.out.println(out.toString());
		st.executeUpdate(out.toString());
		out.reset();

		dbman.getIndexDDL(out);
		st.executeUpdate(out.toString());
//		//System.out.println(out.toString());
		out.reset();

		conn.commit();

		OBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMappings(source.getSourceID(), dbman.getMappings());

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model, new HashMap<Predicate,Description>());

		List<Assertion> list = materializer.getAssertionList();

		//System.out.println("###########################");

		int count = 0;
		for (Assertion ass : list) {
			//System.out.println(ass.toString());
			count += 1;
		}
		assertTrue("count: " + count, count == 9);

		//System.out.println("###########################");

		count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 9);

		// dbman.getDropDDL(out);

		conn.close();
	}

	public void testDBCreation() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
		OWLAPI2Translator trans = new OWLAPI2Translator();
		OWLAPI2VocabularyExtractor ext = new OWLAPI2VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(ontology);

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdumptestx2";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx2"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		RDBMSDataRepositoryManager dbman = new RDBMSSIRepositoryManager();
		dbman.setVocabulary(preds);

		log.debug("Test ontology: {}", trans.translate(ontology));

		dbman.setTBox(trans.translate(ontology));

		
		Statement st = conn.createStatement();

		dbman.createDBSchema(conn,false);
		OWLAPI2ABoxIterator ait = new OWLAPI2ABoxIterator(ontology);
		dbman.insertMetadata(conn);
		dbman.insertData(conn,ait,50000,5000);
		dbman.createIndexes(conn);
		conn.commit();

		OBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMappings(source.getSourceID(), dbman.getMappings());

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model, new HashMap<Predicate,Description>());

		List<Assertion> list = materializer.getAssertionList();

//		//System.out.println("###########################");

		int count = 0;
		for (Assertion ass : list) {
//			//System.out.println(ass.toString());
			count += 1;
		}
		assertTrue("count: " + count, count == 9);

//		//System.out.println("###########################");

		count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 9);

		conn.close();
	}

	public void testDBCreationBIGDATA() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
		OWLAPI2Translator trans = new OWLAPI2Translator();
		OWLAPI2VocabularyExtractor ext = new OWLAPI2VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(ontology);

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdumptestx3";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx3"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		RDBMSDataRepositoryManager dbman = new RDBMSSIRepositoryManager();
		dbman.setVocabulary(preds);
		dbman.setTBox(trans.translate(ontology));

		Statement st = conn.createStatement();

		log.debug("Creating schema and loading data...");

		dbman.createDBSchema(conn,false);
		ABoxAssertionGeneratorIterator ait = new ABoxAssertionGeneratorIterator(100000, preds);
		dbman.insertMetadata(conn);
		dbman.insertData(conn,ait,50000,5000);
		dbman.createIndexes(conn);

		conn.commit();

		log.debug("Executing tests...");

		OBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMappings(source.getSourceID(), dbman.getMappings());

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model, new HashMap<Predicate,Description>());

		List<Assertion> list = materializer.getAssertionList();

		// //System.out.println("###########################");

		int count = 0;
		for (Assertion ass : list) {
			// //System.out.println(ass.toString());
			count += 1;
		}
		assertTrue("count: " + count, count == 100000);

		// //System.out.println("###########################");

		count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 100000);

		conn.close();
	}

	public void disabledtestRestore() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
		OWLAPI2Translator trans = new OWLAPI2Translator();
		OWLAPI2VocabularyExtractor ext = new OWLAPI2VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(ontology);

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdumptestx4";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx4"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		RDBMSDataRepositoryManager dbman = new RDBMSSIRepositoryManager();
		dbman.setVocabulary(preds);
		dbman.setTBox(trans.translate(ontology));

		Statement st = conn.createStatement();

		dbman.createDBSchema(conn,false);
		OWLAPI2ABoxIterator ait = new OWLAPI2ABoxIterator(ontology);
		dbman.insertMetadata(conn);
		dbman.insertData(conn,ait,50000,5000);
		dbman.createIndexes(conn);
		conn.commit();

		/*
		 * Reseting the manager
		 */
		dbman = new RDBMSSIRepositoryManager();
		assertTrue(dbman.checkMetadata(conn));
		dbman.loadMetadata(conn);

		OBDAModel model = fac.getOBDAModel();
		model.addSource(source);

		model.addMappings(source.getSourceID(), dbman.getMappings());

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model, new HashMap<Predicate,Description>());

		List<Assertion> list = materializer.getAssertionList();

		//System.out.println("###########################");

		int count = 0;
		for (Assertion ass : list) {
			//System.out.println(ass.toString());
			count += 1;
		}
		assertEquals(count, 9);

		//System.out.println("###########################");

		count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 9);

		conn.close();
	}

	public class ABoxAssertionGeneratorIterator implements Iterator<Assertion> {

		final int				MAX_ASSERTIONS;
		int						currentassertion	= 0;
		final OBDADataFactory	fac					= OBDADataFactoryImpl.getInstance();
		List<Predicate>			vocab				= new LinkedList<Predicate>();
		final int				size;

		final Random			rand;

		public ABoxAssertionGeneratorIterator(int numberofassertions, Collection<Predicate> vocabulary) {
			MAX_ASSERTIONS = numberofassertions;

			for (Predicate pred : vocabulary) {
				vocab.add(pred);
			}
			size = vocabulary.size();
			rand = new Random();

		}

		@Override
		public boolean hasNext() {
			if (currentassertion < MAX_ASSERTIONS)
				return true;
			return false;
		}

		@Override
		public Assertion next() {
			OntologyFactory ofac = OntologyFactoryImpl.getInstance();
			if (currentassertion >= MAX_ASSERTIONS)
				throw new NoSuchElementException();

			currentassertion += 1;
			int pos = rand.nextInt(size);
			Predicate pred = vocab.get(pos);
			Assertion assertion = null;

			if (pred.getArity() == 1) {
				assertion = ofac.createClassAssertion(pred, fac.getURIConstant(URI.create("1")));
			} else if (pred.getType(1) == COL_TYPE.OBJECT) {
				assertion = ofac.createObjectPropertyAssertion(pred, fac.getURIConstant(URI.create("1")),
						fac.getURIConstant(URI.create("2")));
			} else {
				assertion = ofac.createDataPropertyAssertion(pred, fac.getURIConstant(URI.create("1")), fac.getValueConstant("x"));
			}
			return assertion;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

}
