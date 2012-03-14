package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.obda.utils.MappingAnalyzer;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	private Map<Predicate, Description> equivalenceMap;

	private Map<OBDADataSource, DatalogUnfolder> unfoldersMap = new HashMap<OBDADataSource, DatalogUnfolder>();
	private Map<OBDADataSource, SQLGenerator> sqlgeneratorsMap = new HashMap<OBDADataSource, SQLGenerator>();
	private Set<Predicate> vocabulary = new LinkedHashSet<Predicate>();

	private Connection conn;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	/***
	 * Collects the vocabulary of this OBDA model and initializes unfodlers and
	 * SQL generators for the model.
	 * 
	 * @param model
	 * @throws Exception
	 */
	public VirtualABoxMaterializer(OBDAModel model, Map<Predicate, Description> equivalenceMap) throws Exception {
		this.model = model;
		this.equivalenceMap = equivalenceMap;

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
	        
	        // Setup the unfolder
	        DatalogUnfolder unfolder = new DatalogUnfolder(datalog, metadata);
	        
	        // Setup the SQL generator
			JDBCUtility util = new JDBCUtility(source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
	        SQLGenerator sqlgen = new SQLGenerator(metadata, util);
	        
			unfoldersMap.put(source, unfolder);
			sqlgeneratorsMap.put(source, sqlgen);

			// Collecting the vocabulary of the mappings
			for (CQIE rule : datalog.getRules()) {
				Predicate predicate = rule.getHead().getPredicate();
				vocabulary.add(predicate);
			}
		}
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
                // Does nothing because the SQLException handles this problem also.
            }
            conn = DriverManager.getConnection(url, username, password);
	    }      
    }

	public Iterator<Assertion> getAssertionIterator() throws Exception {
		return new VirtualTriplePredicateIterator(vocabulary.iterator(), model.getSources(), unfoldersMap, sqlgeneratorsMap);
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
		return new VirtualTripleIterator(p, this.model.getSources(), this.unfoldersMap, this.sqlgeneratorsMap);
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
		VirtualTripleIterator it = new VirtualTripleIterator(p, this.model.getSources(), this.unfoldersMap, this.sqlgeneratorsMap);
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
	 * This iterator iterates through all the vocabulary (i.e., the predicates) that the OBDA mappings have.
	 */
	public class VirtualTriplePredicateIterator implements Iterator<Assertion> {

		private Iterator<Predicate> predicates;
		private Collection<OBDADataSource> sources;
		private Map<OBDADataSource, DatalogUnfolder> unfolders;
		private Map<OBDADataSource, SQLGenerator> sqlgens;

		private Predicate currentPredicate = null;
		private VirtualTripleIterator currentIterator = null;

		public VirtualTriplePredicateIterator(Iterator<Predicate> predicates, Collection<OBDADataSource> sources,
				Map<OBDADataSource, DatalogUnfolder> unfolders, Map<OBDADataSource, SQLGenerator> sqlgens)
				throws SQLException {
			this.predicates = predicates;
			this.sources = sources;
			this.unfolders = unfolders;
			this.sqlgens = sqlgens;
			try {
				advanceToNextPredicate();
			} catch (NoSuchElementException e) {
				// NO-OP
			}
		}

		/** 
		 * Iterate to the next predicate to be unfolded and generated the SQL string 
		 */
		private void advanceToNextPredicate() throws NoSuchElementException, SQLException {
			try {
				currentIterator.disconnect();
			} catch (Exception e) {
				// NO-OP
			}
			currentPredicate = predicates.next();
			currentIterator = new VirtualTripleIterator(currentPredicate, sources, unfolders, sqlgens);
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
			boolean hasnext = currentIterator.hasNext(); // Check if there is more assertions
			while (!hasnext) {
				try {
					advanceToNextPredicate(); // if not, let's check the next predicate
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
			boolean hasnext = currentIterator.hasNext(); // Check if there is more assertions
			while (!hasnext) {
				try {
					advanceToNextPredicate(); // if not, let's check the next predicate
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
		private DatalogProgram currentQuery;  // A query object before unfolded
		private DatalogProgram currentUnfoldedQuery;  // A query object after unfolded

		private Predicate pred;

		private ResultSet currentResults;
		private LinkedList<String> signature;
		private Iterator<OBDADataSource> sourceIterator;
		private Map<OBDADataSource, DatalogUnfolder> unfolders;
		private Map<OBDADataSource, SQLGenerator> sqlgens;
		private Queue<DatalogProgram> queryQueue = new LinkedList<DatalogProgram>();

		private Connection conn;
		private Statement st;
		
		private Logger log = LoggerFactory.getLogger(VirtualTripleIterator.class);
		
		public VirtualTripleIterator(Predicate p, Collection<OBDADataSource> sources,
				Map<OBDADataSource, DatalogUnfolder> unfolders, Map<OBDADataSource, SQLGenerator> sqlgens)
				throws SQLException {
			this.pred = p;
			this.unfolders = unfolders;
			this.sqlgens = sqlgens;

			// Generating the query that as for the content of this predicate
			Atom head = null;
			Atom body = null;
			signature = new LinkedList<String>();
			if (p.getArity() == 1) {
				head = dfac.getAtom(dfac.getPredicate(OBDALibConstants.QUERY_HEAD_URI, 1), dfac.getVariable("col1"));
				body = dfac.getAtom(p, dfac.getVariable("col1"));
				signature.add("col1");
			} else if (p.getArity() == 2) {
				head = dfac.getAtom(dfac.getPredicate(OBDALibConstants.QUERY_HEAD_URI, 2), dfac.getVariable("col1"), dfac.getVariable("col2"));
				body = dfac.getAtom(p, dfac.getVariable("col1"), dfac.getVariable("col2"));
				signature.add("col1");
				signature.add("col2");
			} else {
				throw new IllegalArgumentException("Invalid arity " + p.getArity() + " " + p.toString());
			}
			currentQuery = dfac.getDatalogProgram(dfac.getCQIE(head, body));
			sourceIterator = sources.iterator();

			try {
				advanceToNextSource(); // look into the current data source
				advanceToNextQuery(); // look into the current query
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
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
		 * @throws OBDAException 
		 * @throws SQLException 
		 * 
		 * @throws NoSuchElementException
		 * @throws Exception
		 */
		private void advanceToNextSource() throws OBDAException, SQLException {
			disconnect(); // disconnect from the current source
			
			currentSource = sourceIterator.next();
			DatalogUnfolder unfolder = unfolders.get(currentSource);
			DatalogProgram unfolding = unfolder.unfold(currentQuery);
			
			List<CQIE> rules = unfolding.getRules();
			if (rules.size() != 0) {
				for (CQIE query : rules) {
					DatalogProgram unfoldedQuery = dfac.getDatalogProgram(query);
					queryQueue.add(unfoldedQuery);  // for each new unfolded query, put it on a queue for per individual processing.
				}
			} else {
				advanceToNextSource();
			}
			
			String url = currentSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
			String username = currentSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
			String password = currentSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
			
			conn = DriverManager.getConnection(url, username, password);
			st = conn.createStatement();
		}
		
		/**
		 * Advances to the next query rule such that it generates the SQL query and produces
		 * a result set that contains database records.
		 * 
		 * @throws OBDAException
		 * @throws SQLException
		 */
		private void advanceToNextQuery() throws OBDAException, SQLException {
			
			currentUnfoldedQuery = queryQueue.poll(); // fetch the first query rule in the queue
			
			SQLGenerator sqlgen = sqlgens.get(currentSource);
			String sql = sqlgen.generateSourceQuery(currentUnfoldedQuery, signature).trim();
			
			currentResults = st.executeQuery(sql);
		}
		
		@Override
		public boolean hasNext() {
			try {
				if (peeked) {
					return hasnext;
				} else {
					peeked = true;
					hasnext = currentResults.next();
					while (!hasnext) {
						try {
							if (queryQueue.peek() == null) {
								advanceToNextSource();
							}
							advanceToNextQuery();
							hasnext = currentResults.next();
						} catch (NoSuchElementException e) {
							return false;
						} catch (Exception e) {
							log.error(e.getMessage());
							return false;
						}
					}
				}
				return hasnext;
			} catch (SQLException e) {
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
					boolean hasnext = currentResults.next();
					while (!hasnext) {
						try {
							if (queryQueue.peek() == null) {
								advanceToNextSource();
							}
							advanceToNextQuery();
							hasnext = currentResults.next();
						} catch (NoSuchElementException e) {
							throw e;
						} catch (Exception e) {
							log.error(e.getMessage());
							throw new NoSuchElementException();
						}
					}
					return constructAssertion();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		/***
		 * Constructs an ABox assertion with the data from the current result
		 * set.
		 * 
		 * @return
		 */
		private Assertion constructAssertion() throws SQLException {
			Assertion assertion = null;

			Description replacementDescription = equivalenceMap.get(pred);
			Atom queryHead = currentUnfoldedQuery.getRules().get(0).getHead();
			Predicate queryPredicate = queryHead.getPredicate();
			
			/* If a Class object */
			if (queryPredicate.getArity() == 1) {
				URIConstant c = dfac.getURIConstant(URI.create(currentResults.getString(1)));
				if (replacementDescription == null) {
					assertion = ofac.createClassAssertion(pred, c);
				} else {
					OClass replacementc = (OClass) replacementDescription;
					assertion = ofac.createClassAssertion(replacementc.getPredicate(), c);
				}
			/* If a Property object */
			} else if (queryPredicate.getArity() == 2) {
				Term object = queryHead.getTerm(1); // get the second term
				if (object instanceof Function) {
					Function function = (Function) object;
					if (isLiteralDataProperty(function)) {
						URIConstant c1 = dfac.getURIConstant(URI.create(currentResults.getString(1)));
						ValueConstant languageTag = (ValueConstant) function.getTerms().get(1); // The language tag is on the second term
						String languageTagString = (languageTag == null) ? "" : languageTag.getValue();
						ValueConstant c2 = dfac.getValueConstant(currentResults.getString(2), languageTagString);
						if (replacementDescription == null) {
							assertion = ofac.createDataPropertyAssertion(pred, c1, c2);
						} else {
							Property replacementp = (Property) replacementDescription;
							assertion = ofac.createDataPropertyAssertion(replacementp.getPredicate(), c1, c2);
						}
					} else if (isDataTypeFunction(function)) {
						URIConstant c1 = dfac.getURIConstant(URI.create(currentResults.getString(1)));
						ValueConstant c2 = dfac.getValueConstant(currentResults.getString(2), function.getFunctionSymbol().getType(0));
						if (replacementDescription == null) {
							assertion = ofac.createDataPropertyAssertion(pred, c1, c2);
						} else {
							Property replacementp = (Property) replacementDescription;
							assertion = ofac.createDataPropertyAssertion(replacementp.getPredicate(), c1, c2);
						}
					} else {
						URIConstant c1 = dfac.getURIConstant(URI.create(currentResults.getString(1)));
						URIConstant c2 = dfac.getURIConstant(URI.create(currentResults.getString(2)));
						if (replacementDescription == null) {
							assertion = ofac.createObjectPropertyAssertion(pred, c1, c2);
						} else {
							Property replacementp = (Property) replacementDescription;
							if (!replacementp.isInverse()) {
								assertion = ofac.createObjectPropertyAssertion(replacementp.getPredicate(), c1, c2);
							} else {
								assertion = ofac.createObjectPropertyAssertion(replacementp.getPredicate(), c2, c1);
							}
						}
					}
				} else {
					// TODO We should do something if the object is not an instance of function.
				}
			} else {
				throw new RuntimeException("ERROR, Wrongly typed predicate: " + pred.toString());
			}
			return assertion;
		}		
		
		/**
		 * Determines if the given function is a special data type function or not.
		 */
		private boolean isDataTypeFunction(Function function) {
			Predicate functionSymbol = function.getFunctionSymbol();
			if (functionSymbol.equals(OBDAVocabulary.XSD_STRING)
					|| functionSymbol.equals(OBDAVocabulary.XSD_INTEGER)
					|| functionSymbol.equals(OBDAVocabulary.XSD_DECIMAL)
					|| functionSymbol.equals(OBDAVocabulary.XSD_DOUBLE)
					|| functionSymbol.equals(OBDAVocabulary.XSD_DATETIME)
					|| functionSymbol.equals(OBDAVocabulary.XSD_BOOLEAN)) {
				return true;
			}
			return false;
		}
		
		private boolean isLiteralDataProperty(Function function) {
			Predicate functionSymbol = function.getFunctionSymbol();
			return functionSymbol.equals(OBDAVocabulary.RDFS_LITERAL);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
