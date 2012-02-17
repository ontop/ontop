package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

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

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private OBDADataSource datasource;
	
	private DatalogProgram datalog;
	
	private Connection conn;	
	
	private JDBCUtility utility;
	
	private DatalogUnfolder unfolder;
	
	private SQLGenerator sqlgen;

	/***
	 * Collects the vocabulary of this OBDA model and initializes unfodlers and
	 * SQL generators for the model.
	 * 
	 * @param model
	 * @throws Exception
	 */
	public VirtualABoxMaterializer(OBDAModel model) throws Exception {

	    List<OBDADataSource> datasources = model.getSources();        
        if (datasources.size() <= 0) {
            throw new OBDAException("Cannot find the datasource!");
        }
        
        // NOTE: Currently the system only supports one data source.
        datasource = datasources.get(0);
        
	    setupConnection();
		
		URI sourceUri = datasource.getSourceID();
		ArrayList<OBDAMappingAxiom> mappingList = model.getMappings(sourceUri);
		
		DBMetadata metadata = JDBCConnectionManager.getMetaData(conn);
        MappingAnalyzer analyzer = new MappingAnalyzer(mappingList, metadata);
        datalog = analyzer.constructDatalogProgram();
        
        MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata);
        typeRepair.insertDataTyping(datalog);
        
        unfolder = new DatalogUnfolder(datalog, metadata);
        sqlgen = new SQLGenerator(metadata);
        sqlgen.setDatabaseSystem(datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
	}

	private void setupConnection() throws SQLException {
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
        List<CQIE> rules = datalog.getRules();
		return new VirtualTriplePredicateIterator(rules.iterator(), sqlgen, conn);
	}

	public List<Assertion> getAssertionList() throws Exception {
		Iterator<Assertion> it = getAssertionIterator();
		List<Assertion> assertions = new LinkedList<Assertion>();
		while (it.hasNext()) {
		    Assertion assertion = it.next();
		    assertions.add(assertion);
		}
		return assertions;
	}

	public Iterator<Assertion> getAssertionIterator(CQIE rule) throws Exception {
		return new VirtualTripleIterator(rule, sqlgen, conn);
	}

	/***
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
	public List<Assertion> getAssertionList(CQIE rule) throws Exception {
		VirtualTripleIterator it = new VirtualTripleIterator(rule, sqlgen, conn);

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
	public int getTripleCount(CQIE rule) throws Exception {

		Iterator<Assertion> it = getAssertionIterator(rule);

		int tripleCount = 0;
		while (it.hasNext()) {
			it.next();
			tripleCount += 1;
		}

		return tripleCount;
	}
	
	public void disconnect() throws SQLException {
	    if (conn != null) {
	        conn.close();
	    }
	}

	public class VirtualTriplePredicateIterator implements Iterator<Assertion> {

		private Iterator<CQIE> rules;
		private SQLGenerator sqlgen;
		private Connection conn;
		
		private CQIE currentRule;
		private VirtualTripleIterator currentIterator;

		public VirtualTriplePredicateIterator(Iterator<CQIE> rules, SQLGenerator sqlgen, Connection conn)
				throws SQLException, OBDAException {
		    this.rules = rules;
			this.sqlgen = sqlgen;
			this.conn = conn;
			
			try {
				advanceToNextRule();
			} catch (NoSuchElementException e) {
			    // NO-OP
			}
		}

		private void advanceToNextRule() throws NoSuchElementException {
		    
			currentRule = rules.next();
			currentIterator = new VirtualTripleIterator(currentRule, sqlgen, conn);
		}

		@Override
		public boolean hasNext() {
		    boolean hasNext = currentIterator.hasNext();
			if (hasNext) {
			    return true;
			} else {
			    try {
                    advanceToNextRule();
                    return hasNext();
                } catch (NoSuchElementException e) {
                    return false;
                }
			}
		}

		@Override
		public Assertion next() {
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

		private DatalogProgram query = null;

		private Iterator<OBDADataSource> sourceIterator = null;

		private ResultSet currentResults = null;

		private List<String> signature;

		private CQIE rule;

		private SQLGenerator sqlgen;

		private Logger log = LoggerFactory.getLogger(VirtualTripleIterator.class);
		
		private Connection conn = null;

		private Statement st;
		
		public VirtualTripleIterator(CQIE rule, SQLGenerator sqlgen, Connection conn) {

		    this.rule = rule;
			this.sqlgen = sqlgen;
			this.conn = conn;

			try {
    			query = dfac.getDatalogProgram(rule);			
    			signature = getSignature(query);
    			
    			String sql = sqlgen.generateSourceQuery(query, signature).trim();
                
                st = conn.createStatement();            
                currentResults = st.executeQuery(sql);
			} catch (SQLException e) {
			    // NO-OP
			} catch (OBDAException e) {
			    // NO-OP
			}
		}
		
		/**
	     * Extracts the signature of a CQ query given as a DatalogProgram. Only
	     * variables are accepted in the signature of queries.
	     */
	    private List<String> getSignature(DatalogProgram datalog) throws OBDAException {	        
	        List<CQIE> rules = datalog.getRules();
	        if (rules.size() < 1) {
	            throw new OBDAException("Invalid query");
	        }
	        List<String> signature = new LinkedList<String>();
	        for (Term term : rules.get(0).getHead().getTerms()) {
	            if (term instanceof Variable) {
	                signature.add(((Variable) term).getName());
	            } else if (term instanceof Function) { 
	                term = ((Function) term).getTerms().get(0);
	                if (term instanceof Variable) {
	                    signature.add(((Variable) term).getName());
	                } else {
	                    throw new OBDAException("Only variables are allowed in the head of queries");
	                }
	            } else {
	                throw new OBDAException("Only variables and functions are allowed in the head of queries");
	            }
	        }
	        return signature;
	    }

		@Override
		public boolean hasNext() {
			try {
                return currentResults.next();
            } catch (SQLException e) {
                return false;
            }			
		}

		@Override
		public Assertion next() {
			try {
                return constructAssertion();
            } catch (SQLException e) {
                return null;
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
			Predicate predicate = rule.getHead().getPredicate();
			if (predicate.getArity() == 1) {
				URIConstant c = dfac.getURIConstant(URI.create(currentResults.getString(1)));
				assertion = ofac.createClassAssertion(predicate, c);
			} else if (predicate.getArity() == 2) {
			    Term objectTerm = rule.getHead().getTerm(1);
			    if (objectTerm != null) {
    			    if (objectTerm instanceof Variable) {
    			        URIConstant c1 = dfac.getURIConstant(URI.create(currentResults.getString(1)));
        				URIConstant c2 = dfac.getURIConstant(URI.create(currentResults.getString(2)));
       					assertion = ofac.createObjectPropertyAssertion(predicate, c1, c2);
        			} else if (objectTerm instanceof Function) {
        				URIConstant c1 = dfac.getURIConstant(URI.create(currentResults.getString(1)));
        				Predicate functionSymbol = ((Function) objectTerm).getFunctionSymbol();
        				ValueConstant c2 = dfac.getValueConstant(currentResults.getString(2), functionSymbol.getType(0));
        				assertion = ofac.createDataPropertyAssertion(predicate, c1, c2);
        			}
			    } else {
	                throw new RuntimeException("ERROR, The object shouldn't be a constant: " + predicate.toString());
	            }
			} else {
				throw new RuntimeException("ERROR, Wrongly typed predicate: " + predicate.toString());
			}
			return assertion;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
