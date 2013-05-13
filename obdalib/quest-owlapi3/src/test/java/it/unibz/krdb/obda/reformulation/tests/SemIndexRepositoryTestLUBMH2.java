package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3VocabularyExtractor;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemIndexRepositoryTestLUBMH2 extends TestCase {

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	Logger log = LoggerFactory.getLogger(SemIndexRepositoryTestLUBMH2.class);

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDBCreationFromString() throws Exception {

		String owlfile = "src/test/resources/test/lubm-ex-20-uni1/University0-imports.owl";

		String owlfile1 = "src/test/resources/test/lubm-ex-20-uni1/University0_0.owl";
		String owlfile2 = "src/test/resources/test/lubm-ex-20-uni1/University0_1.owl";
		String owlfile3 = "src/test/resources/test/lubm-ex-20-uni1/University0_2.owl";
		String owlfile4 = "src/test/resources/test/lubm-ex-20-uni1/University0_3.owl";
		String owlfile5 = "src/test/resources/test/lubm-ex-20-uni1/University0_4.owl";
		String owlfile6 = "src/test/resources/test/lubm-ex-20-uni1/University0_5.owl";
		String owlfile7 = "src/test/resources/test/lubm-ex-20-uni1/University0_6.owl";
		String owlfile8 = "src/test/resources/test/lubm-ex-20-uni1/University0_7.owl";
		String owlfile9 = "src/test/resources/test/lubm-ex-20-uni1/University0_8.owl";
		String owlfile10 = "src/test/resources/test/lubm-ex-20-uni1/University0_9.owl";
		String owlfile11 = "src/test/resources/test/lubm-ex-20-uni1/University0_10.owl";
		String owlfile12 = "src/test/resources/test/lubm-ex-20-uni1/University0_11.owl";
		String owlfile13 = "src/test/resources/test/lubm-ex-20-uni1/University0_12.owl";
		String owlfile14 = "src/test/resources/test/lubm-ex-20-uni1/University0_13.owl";
		String owlfile15 = "src/test/resources/test/lubm-ex-20-uni1/University0_14.owl";

		// Loading the OWL file
		OWLAPI3Translator trans = new OWLAPI3Translator();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology1 = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Set<OWLOntology> clousure = manager.getImportsClosure(ontology1);
		Ontology translatedOntologyMerge = trans.mergeTranslateOntologies(clousure);

		OWLAPI3VocabularyExtractor ext = new OWLAPI3VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(clousure);

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
		dbman.setTBox(translatedOntologyMerge);

		Statement st = conn.createStatement();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dbman.getTablesDDL(out);
		// //System.out.println(out.toString());
		st.executeUpdate(out.toString());
		out.reset();

		dbman.getMetadataSQLInserts(out);
		// //System.out.println(out.toString());
		st.executeUpdate(out.toString());
		out.reset();

		OWLAPI3ABoxIterator ait = new OWLAPI3ABoxIterator(clousure);
		dbman.insertData(conn, ait, 50000, 5000);
		dbman.getSQLInserts(ait, out);
		// //System.out.println(out.toString());
//		st.executeUpdate(out.toString());
//		out.reset();

		
		dbman.createIndexes(conn);
		
//		dbman.getIndexDDL(out);
//		st.executeUpdate(out.toString());
		// //System.out.println(out.toString());
//		out.reset();

//		conn.commit();

		OBDAModel model = fac.getOBDAModel();

		// for (OWLClass c : ontology.getClassesInSignature()) {
		// model.declareClass(fac.getClassPredicate(c.getIRI().toString()));
		// }
		// for (OWLDataProperty a : ontology.getDataPropertiesInSignature()) {
		// model.declareDataProperty(fac.getDataPropertyPredicate(a.getIRI().toString()));
		// }
		// for (OWLObjectProperty r : ontology.getObjectPropertiesInSignature())
		// {
		// model.declareObjectProperty(fac.getObjectPropertyPredicate(r.getIRI().toString()));
		// }

		for (Predicate p : translatedOntologyMerge.getVocabulary()) {
			model.declarePredicate(p);

		}

		model.addSource(source);
		model.addMappings(source.getSourceID(), dbman.getMappings());

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> list = materializer.getAssertionList();

		// System.out.println("###########################");

		int count = 0;
		for (Assertion ass : list) {
			System.out.println(ass.toString());
			count += 1;
		}
		assertEquals(9, count);

		// System.out.println("###########################");

		count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 9);

		// dbman.getDropDDL(out);

		conn.close();
	}

	public void disabledtestRestore() throws Exception {
		String owlfile = "src/test/resources/test/lubm-ex-20-uni1/University0_0.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
		OWLAPI3Translator trans = new OWLAPI3Translator();
		OWLAPI3VocabularyExtractor ext = new OWLAPI3VocabularyExtractor();
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

		dbman.createDBSchema(conn, false);
		OWLAPI3ABoxIterator ait = new OWLAPI3ABoxIterator(ontology);
		dbman.insertMetadata(conn);
		dbman.insertData(conn, ait, 50000, 5000);
		dbman.createIndexes(conn);
		conn.commit();

		/*
		 * Reseting the manager
		 */
		dbman = new RDBMSSIRepositoryManager();
		assertTrue(dbman.checkMetadata(conn));
		dbman.loadMetadata(conn);

		OBDAModel model = fac.getOBDAModel();

		for (OWLClass c : ontology.getClassesInSignature()) {
			model.declareClass(fac.getClassPredicate(c.getIRI().toString()));
		}
		for (OWLDataProperty a : ontology.getDataPropertiesInSignature()) {
			model.declareDataProperty(fac.getDataPropertyPredicate(a.getIRI().toString()));
		}
		for (OWLObjectProperty r : ontology.getObjectPropertiesInSignature()) {
			model.declareObjectProperty(fac.getObjectPropertyPredicate(r.getIRI().toString()));
		}
		model.addSource(source);
		model.addMappings(source.getSourceID(), dbman.getMappings());

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> list = materializer.getAssertionList();

		// System.out.println("###########################");

		int count = 0;
		for (Assertion ass : list) {
			// System.out.println(ass.toString());
			count += 1;
		}
		assertEquals(count, 9);

		// System.out.println("###########################");

		count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 9);

		conn.close();
	}

	public class ABoxAssertionGeneratorIterator implements Iterator<Assertion> {

		final int MAX_ASSERTIONS;
		int currentassertion = 0;
		final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		List<Predicate> vocab = new LinkedList<Predicate>();
		final int size;

		final Random rand;

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
				assertion = ofac.createClassAssertion(pred, fac.getURIConstant(OBDADataFactoryImpl.getIRI("1")));
			} else if (pred.getType(1) == COL_TYPE.OBJECT) {
				assertion = ofac.createObjectPropertyAssertion(pred, fac.getURIConstant(OBDADataFactoryImpl.getIRI("1")),
						fac.getURIConstant(OBDADataFactoryImpl.getIRI("2")));
			} else {
				assertion = ofac.createDataPropertyAssertion(pred, fac.getURIConstant(OBDADataFactoryImpl.getIRI("1")),
						fac.getValueConstant("22", COL_TYPE.INTEGER));
			}
			return assertion;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

}
