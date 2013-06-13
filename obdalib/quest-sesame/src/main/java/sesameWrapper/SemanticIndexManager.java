package sesameWrapper;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.EquivalenceTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.sesame.SesameRDFIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * An utility to setup and maintain a semantic index repository independently
 * from a Quest instance.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexManager {

	Connection conn = null;

	Ontology ontologyClosure = null;

	Ontology optimizedOntology = null;

	private Map<Predicate, Description> equivalenceMaps;

	private RDBMSSIRepositoryManager dataRepository = null;

	Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexManager(OWLOntology tbox, Connection connection) throws Exception {
		conn = connection;
		ontologyClosure = QuestOWL.loadOntologies(tbox);

		EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(ontologyClosure);
		equiOptimizer.optimize();

		/* This generates a new TBox with a simpler vocabulary */
		optimizedOntology = equiOptimizer.getOptimalTBox();

		/*
		 * This is used to simplify the vocabulary of ABox assertions and
		 * mappings
		 */
		equivalenceMaps = equiOptimizer.getEquivalenceMap();

		dataRepository = new RDBMSSIRepositoryManager(optimizedOntology.getVocabulary());
		dataRepository.setTBox(optimizedOntology);

		log.debug("TBox has been processed. Ready to ");

	}

	public void restoreRepository() throws SQLException {
		dataRepository.loadMetadata(conn);

		log.debug("Semantic Index metadata was found and restored from the DB");
	}

	public void setupRepository(boolean drop) throws SQLException {

		dataRepository.createDBSchema(conn, drop);
		dataRepository.insertMetadata(conn);

		log.debug("Semantic Index repository has been setup.");

	}

	public void updateMetadata() throws SQLException {
		dataRepository.insertMetadata(conn);
		log.debug("Updated metadata in the repository");
	}

	public int insertData(OWLOntology ontology, int commitInterval, int batchSize) throws SQLException {

		OWLAPI3ABoxIterator aBoxIter = new OWLAPI3ABoxIterator(ontology.getOWLOntologyManager().getImportsClosure(ontology),
				equivalenceMaps);

		EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(aBoxIter, equivalenceMaps);
		int result = dataRepository.insertData(conn, newData, commitInterval, batchSize);

		log.info("Loaded {} items into the DB.", result);

		return result;
	}

	public int insertDataNTriple(final String ntripleFile, final String baseURI, final int commitInterval, final int batchSize)
			throws SQLException, RDFParseException, RDFHandlerException, FileNotFoundException, IOException {

		final TurtleParser parser = new TurtleParser();
		// NTriplesParser parser = new NTriplesParser();

		final SesameRDFIterator aBoxIter = new SesameRDFIterator();
		
		parser.setRDFHandler(aBoxIter);
		
		final EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(aBoxIter, equivalenceMaps);


		Thread t = new Thread() {
			@Override
			public void run() {

				try {
					parser.parse(new BufferedReader(new FileReader(ntripleFile)), baseURI);
				} catch (RDFParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RDFHandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		final int[] val = new int[1];

		Thread t2 = new Thread() {

			int result;

			@Override
			public void run() {
				try {
					val[0] = dataRepository.insertData(conn, newData, commitInterval, batchSize);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				log.info("Loaded {} items into the DB.", result);

			}

		};

		t.start();
		t2.start();
		try {
			t.join();

			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// EquivalentTriplePredicateIterator newData = new
		// EquivalentTriplePredicateIterator(aBoxIter, equivalenceMaps);

		return val[0];
	}

}
