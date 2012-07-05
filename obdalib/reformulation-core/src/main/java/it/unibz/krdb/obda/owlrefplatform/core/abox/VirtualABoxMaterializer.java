package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.krdb.obda.utils.MappingAnalyzer;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Allows you to work with the virtual triples defined by an OBDA model. In
 * particular you will be able to generate ABox assertions from it and get
 * statistics.
 * 
 * The class works "online", that is, the triples are never kept in memory by
 * this class, instead a connection to the data sources will be established when
 * needed and the data will be streamed or retrieved on request.
 * 
 * In order to compute the SQL queries relevant for each predicate we use the
 * ComplexMapping Query unfolder. This allow us to reuse all the infrastructure
 * for URI generation and to avoid manually processing each mapping. This also
 * means that all features and limitations of the complex unfolder are present
 * here.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class VirtualABoxMaterializer {

	private OBDAModel model;

	private Map<OBDADataSource, Quest> questInstanceMap = new HashMap<OBDADataSource, Quest>();
	private Set<Predicate> vocabulary = new LinkedHashSet<Predicate>();

	private Connection conn;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	/***
	 * Collects the vocabulary from the OBDA model to prepare the ABox creation.
	 * 
	 * @param model
	 * @throws Exception
	 */
	public VirtualABoxMaterializer(OBDAModel model) throws Exception {
		this.model = model;
		
		for (Predicate p: model.getDeclaredPredicates()) {
			vocabulary.add(p);
		}
		
		for (OBDADataSource source : model.getSources()) {
			// For each data source in the model
			URI sourceUri = source.getSourceID();
			ArrayList<OBDAMappingAxiom> mappingList = model.getMappings(sourceUri);

			// Retrieve the connection object to obtain the database metadata
			setupConnection(source);
			
			// Construct the datalog program from the OBDA mappings
			DBMetadata metadata = JDBCConnectionManager.getMetaData(conn);
			MappingAnalyzer analyzer = new MappingAnalyzer(mappingList, metadata);
			DatalogProgram datalog = analyzer.constructDatalogProgram();

			// Insert the data type information
			MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata);
			typeRepair.insertDataTyping(datalog);

			// TODO: Design redundancy!
			// For each data source, construct a new OBDA model!
			OBDAModel newModel = dfac.getOBDAModel();
			newModel.addSource(source);
			newModel.addMappings(sourceUri, mappingList);
			for (Predicate p : model.getDeclaredPredicates()) {
				newModel.declarePredicate(p);
			}

			Quest questInstance = new Quest();
			questInstance.setPreferences(getDefaultPreference());
			Ontology ontology = ofac.createOntology();
			ontology.addEntities(model.getDeclaredPredicates());
			questInstance.loadTBox(ontology);
			questInstance.loadOBDAModel(newModel);
			questInstance.setupRepository();
			
			questInstanceMap.put(source, questInstance);
			
			// Collecting the vocabulary of the mappings
			for (CQIE rule : datalog.getRules()) {
				Predicate predicate = rule.getHead().getPredicate();
				vocabulary.add(predicate);
			}
		}
	}

	private QuestPreferences getDefaultPreference() {
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		return p;
	}

	private void setupConnection(OBDADataSource datasource) throws SQLException {
		// Validate if the connection is already existed or not
		if (conn == null || conn.isClosed()) {
			String url = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
			String username = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
			String password = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
			String driver = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e1) {
				// Does nothing because the SQLException handles this problem
				// also.
			}
			conn = DriverManager.getConnection(url, username, password);
		}
	}

	public Iterator<Assertion> getAssertionIterator() throws Exception {
		return new VirtualTriplePredicateIterator(vocabulary.iterator(), model.getSources(), questInstanceMap);
	}

	public List<Assertion> getAssertionList() throws Exception {
		Iterator<Assertion> it = getAssertionIterator();
		List<Assertion> assertions = new LinkedList<Assertion>();
		while (it.hasNext()) {
			assertions.add(it.next());
		}
		return assertions;
	}

	public Iterator<Assertion> getAssertionIterator(Predicate p) throws Exception {
		return new VirtualTripleIterator(p, this.model.getSources(), questInstanceMap);
	}

	/**
	 * Returns the list of all triples (ABox assertions) generated by the
	 * mappings of the OBDA model. Note that this list is not linked to the DB,
	 * that is, changes to the list do not affect the database.
	 * 
	 * This is only a convenience method since, internally, this method simply
	 * calls the getAssertionIterator method.
	 * 
	 * @param p
	 * @return
	 * @throws Exception
	 */
	public List<Assertion> getAssertionList(Predicate p) throws Exception {
		VirtualTripleIterator it = new VirtualTripleIterator(p, this.model.getSources(), questInstanceMap);
		List<Assertion> assertions = new LinkedList<Assertion>();
		while (it.hasNext()) {
			assertions.add(it.next());
		}
		return assertions;
	}

	public int getTripleCount() throws Exception {
		Iterator<Assertion> it = getAssertionIterator();

		int tripleCount = 0;
		while (it.hasNext()) {
			it.next();
			tripleCount += 1;
		}
		return tripleCount;

	}

	/***
	 * Counts the number of triples generated by the mappings for this
	 * predicate. Note, this method will actually execute queries and iterate
	 * over them to count the results, this might take a long time depending on
	 * the size of the DB.
	 * 
	 * @param pred
	 * @return
	 * @throws Exception
	 */
	public int getTripleCount(Predicate pred) throws Exception {
		Iterator<Assertion> it = getAssertionIterator(pred);

		int tripleCount = 0;
		while (it.hasNext()) {
			it.next();
			tripleCount += 1;
		}
		return tripleCount;
	}

	/**
	 * This iterator iterates through all the vocabulary (i.e., the predicates)
	 * that the OBDA mappings have.
	 */
	public class VirtualTriplePredicateIterator implements Iterator<Assertion> {

		private Iterator<Predicate> predicates;
		private Collection<OBDADataSource> sources;
		private Map<OBDADataSource, Quest> questInstances;

		private Predicate currentPredicate = null;
		private VirtualTripleIterator currentIterator = null;

		public VirtualTriplePredicateIterator(Iterator<Predicate> predicates, Collection<OBDADataSource> sources,
				Map<OBDADataSource, Quest> questInstances) throws SQLException {
			this.predicates = predicates;
			this.sources = sources;
			this.questInstances = questInstances;
			try {
				advanceToNextPredicate();
			} catch (NoSuchElementException e) {
				// NO-OP
			}
		}

		/**
		 * Iterate to the next predicate to be unfolded and generated the SQL
		 * string
		 */
		private void advanceToNextPredicate() throws NoSuchElementException, SQLException {
			try {
				currentIterator.disconnect();
			} catch (Exception e) {
				// NO-OP
			}
			currentPredicate = predicates.next();
			currentIterator = new VirtualTripleIterator(currentPredicate, sources, questInstances);
		}

		/**
		 * Release the connection resources in the VirtualTripleIterator.
		 */
		public void disconnect() {
			try {
				currentIterator.disconnect();
			} catch (Exception e) {

			}
		}

		@Override
		public boolean hasNext() {
			if (currentIterator == null) {
				return false;
			}
			boolean hasnext = currentIterator.hasNext(); // Check if there is
															// more assertions
			while (!hasnext) {
				try {
					advanceToNextPredicate(); // if not, let's check the next
												// predicate
					hasnext = currentIterator.hasNext();
				} catch (Exception e) {
					return false;
				}
			}
			return hasnext;
		}

		@Override
		public Assertion next() {
			if (currentIterator == null) {
				throw new NoSuchElementException();
			}
			boolean hasnext = currentIterator.hasNext(); // Check if there is
															// more assertions
			while (!hasnext) {
				try {
					advanceToNextPredicate(); // if not, let's check the next
												// predicate
					hasnext = currentIterator.hasNext();
				} catch (Exception e) {
					throw new NoSuchElementException();
				}
			}
			return currentIterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/***
	 * An iterator that will dynamically construct ABox assertions for the given
	 * predicate based on the results of executing the mappings for the
	 * predicate in each data source.
	 * 
	 * @author Mariano Rodriguez Muro
	 * 
	 */
	public class VirtualTripleIterator implements Iterator<Assertion> {

		/*
		 * Indicates that we have peeked to see if there are more rows and that
		 * a call to next should not invoke res.next
		 */
		private boolean peeked = false;
		private boolean hasnext = false;

		private OBDADataSource currentSource;
		private String currentQuery;

		private Predicate predicate;

		private OBDAResultSet currentResults;
		private Iterator<OBDADataSource> sourceIterator;
		private Map<OBDADataSource, Quest> questInstances;
		private Queue<DatalogProgram> queryQueue = new LinkedList<DatalogProgram>();

		private QuestConnection conn;
		private QuestStatement st;

		private Logger log = LoggerFactory.getLogger(VirtualTripleIterator.class);

		public VirtualTripleIterator(Predicate predicate, Collection<OBDADataSource> sources, Map<OBDADataSource, Quest> questInstances)
				throws SQLException {
			this.predicate = predicate;
			this.questInstances = questInstances;

			currentQuery = createQuery();
			sourceIterator = sources.iterator();

			try {
				advanceToNextSource(); // look into the current data source
				advanceToNextQuery(); // look into the current query
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// SPARQL query generated from the predicate
		private String createQuery() {
			StringBuffer sb = new StringBuffer();
			sb.append("PREFIX :	<" + model.getPrefixManager().getDefaultPrefix() + ">\n");

			if (predicate.getArity() == 1) {
				sb.append("SELECT $x WHERE { $x a <" + predicate.getName() + "> . }");
			} else {
				sb.append("SELECT $x $y WHERE { $x <" + predicate.getName() + "> $y . }");
			}

			return sb.toString();
		}

		/**
		 * Releases all the connection resources
		 */
		public void disconnect() {
			if (st != null) {
				try {
					st.close();
				} catch (Exception e) {
					// NO-OP
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					// NO-OP
				}
			}
		}

		/***
		 * Advances to the next sources, configures an unfolder using the
		 * mappings for the source and executes the query related to the
		 * predicate
		 * 
		 * @throws NoSuchElementException
		 * @throws Exception
		 */
		private void advanceToNextSource() throws Exception {
			disconnect(); // disconnect from the current source

			currentSource = sourceIterator.next();
			Quest questInstance = questInstances.get(currentSource);

			conn = questInstance.getConnection();
			st = conn.createStatement();
		}

		/**
		 * Advances to the next query rule such that it generates the SQL query
		 * and produces a result set that contains database records.
		 * 
		 * @throws OBDAException
		 * @throws SQLException
		 */
		private void advanceToNextQuery() throws OBDAException, SQLException {
			currentResults = st.execute(currentQuery);
		}

		@Override
		public boolean hasNext() {
			try {
				if (peeked) {
					return hasnext;
				} else {
					peeked = true;
					hasnext = currentResults.nextRow();
					while (!hasnext) {
						try {
							if (queryQueue.peek() == null) {
								advanceToNextSource();
							}
							advanceToNextQuery();
							hasnext = currentResults.nextRow();
						} catch (NoSuchElementException e) {
							return false;
						} catch (Exception e) {
							log.error(e.getMessage());
							return false;
						}
					}
				}
				return hasnext;
			} catch (OBDAException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Assertion next() {
			try {
				if (peeked) {
					peeked = false;
					return constructAssertion();
				} else {
					boolean hasnext = currentResults.nextRow();
					while (!hasnext) {
						try {
							if (queryQueue.peek() == null) {
								advanceToNextSource();
							}
							advanceToNextQuery();
							hasnext = currentResults.nextRow();
						} catch (NoSuchElementException e) {
							throw e;
						} catch (Exception e) {
							log.error(e.getMessage());
							throw new NoSuchElementException();
						}
					}
					return constructAssertion();
				}
			} catch (OBDAException e) {
				throw new RuntimeException(e);
			}
		}

		/***
		 * Constructs an ABox assertion with the data from the current result
		 * set.
		 * 
		 * @return
		 * @throws URISyntaxException
		 */
		private Assertion constructAssertion() throws OBDAException {
			Assertion assertion = null;
			int arity = predicate.getArity();
			if (arity == 1) {
				Constant value = currentResults.getConstant(1);
				if (value instanceof URIConstant) {
					URIConstant c = (URIConstant) value;
					assertion = ofac.createClassAssertion(predicate, c);
				} else {
					// Ignore - NO-OP
				}
			} else {
				COL_TYPE type = predicate.getType(1);
				if (type == COL_TYPE.OBJECT) {
					Constant value1 = currentResults.getConstant(1);
					Constant value2 = currentResults.getConstant(2);
					if (value1 instanceof URIConstant && value2 instanceof URIConstant) {
						URIConstant o1 = (URIConstant) value1;
						URIConstant o2 = (URIConstant) value2;
						assertion = ofac.createObjectPropertyAssertion(predicate, o1, o2);
					} else {
						// Ignore - NO-OP
					}
				} else {
					Constant value = currentResults.getConstant(1);
					if (value instanceof URIConstant) {
						URIConstant o = (URIConstant) value;
						ValueConstant c = (ValueConstant) currentResults.getConstant(2);
						assertion = ofac.createDataPropertyAssertion(predicate, o, c);
					} else {
						// Ignore - NO-OP
					}
				}
			}
			return assertion;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
