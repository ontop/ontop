import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.SesameVirtualRepo;

public class RDB2RDFScenarioParent extends TestCase {

	protected final String sqlFileURL;
	protected final String mappingFileURL;
	protected final String outputFileURL;
	protected final String testURI;
	protected final String name;
	protected Repository dataRep;
	private OutputStream output = null;
	private Connection sqlConnection;
	static final Logger logger = LoggerFactory.getLogger(RDB2RDFScenarioParent.class);

	public interface Factory {
		RDB2RDFScenarioParent createRDB2RDFScenarioTest(String testURI, String name, String sqlFileURL, 
				String mappingFileURL, String outputFileURL);
		
		RDB2RDFScenarioParent createRDB2RDFScenarioTest(String testURI, String name, String sqlFileURL, 
				String mappingFileURL, String outputFileURL, String parameterFileURL);
	
		String getMainManifestFile();
	}

	
	public RDB2RDFScenarioParent(String testUri, String name, String sqlFile, String mappingFile, String outputFile) throws FileNotFoundException {
		super(name);
		this.testURI = testUri;
		this.name = name;
		this.sqlFileURL = sqlFile;
		this.mappingFileURL = mappingFile;
		this.outputFileURL =  outputFile;
		if (outputFileURL != null) {
			output = new FileOutputStream(new File(outputFile));
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		//if (!mappingFileURL.isEmpty()) {
			try {
				 sqlConnection= DriverManager.getConnection("jdbc:h2:mem:questrepository","sa", "");
				    java.sql.Statement s = sqlConnection.createStatement();
				  
				    try {
				    	String text = new Scanner( new File(sqlFileURL) ).useDelimiter("\\A").next();
				    	s.execute(text);
				    	//Server.startWebServer(sqlConnection);
				    	 
				    } catch(SQLException sqle) {
				        System.out.println("Exception in creating db from script");
				    }
				   
				    s.close();
				dataRep = createRepository();
			} catch (Exception exc) {
				try {
					tearDown();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				if (output!=null)
					throw exc;
			}	
	}

	protected Repository createRepository() throws Exception {
		try {
			SesameVirtualRepo repo = new SesameVirtualRepo(testURI, mappingFileURL, false, "TreeWitness");
			repo.initialize();
			return repo;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (dataRep != null) {
			dataRep.shutDown();
			dataRep = null;
		}
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				System.out.println("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}

	@Override
	protected void runTest() throws Exception {
		RepositoryConnection con =null;
		try {
			con = dataRep.getConnection();
			String graphq = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
			GraphQuery gquery = con.prepareGraphQuery(QueryLanguage.SPARQL, graphq);
			if (output!= null)
			{
				RDFWriter writer = new NTriplesWriter(output);
				gquery.evaluate(writer);
			}
			else
			{
				RDFWriter writer = new NTriplesWriter(System.out);
				gquery.evaluate(writer);
			}
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (output!= null)
				throw e;
		}
		finally {
			if (output!= null)
				output.close();
		}
	}
	
	public static TestSuite suite(String manifestFileURL, Factory factory) throws Exception {
		return suite(manifestFileURL, factory, true);
	}

	public static TestSuite suite(String manifestFileURL, Factory factory, boolean approvedOnly) throws Exception {
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite(factory.getClass().getName());

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		RDB2RDFManifestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		suite.setName(getManifestName(manifestRep, con, manifestFileURL));

		// Extract test case information from the manifest file. Note that we only
		// select those test cases that are mentioned in the list.
		String query = "PREFIX  rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#>\n" +
				"PREFIX dcterms: <http://purl.org/dc/elements/1.1/> \n" +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
				"SELECT ?title ?id ?sql ?tcase WHERE {" +
				"?s rdf:type rdb2rdftest:DataBase;" +
				"   dcterms:title ?title;" +
				"   dcterms:identifier ?id;" +
				"  rdb2rdftest:sqlScriptFile ?sql;" +
				"  rdb2rdftest:relatedTestCase ?tcase}";
		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
		TupleQueryResultHandler handler = new SPARQLResultsCSVWriter(System.out);
		testCaseQuery.evaluate(handler);
		
		logger.debug("Evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			String testURI =  bindingSet.getValue("id").stringValue();
			String testName = bindingSet.getValue("title").toString();
			String sqlFile = bindingSet.getValue("sql").stringValue();
			String relTestCase = bindingSet.getValue("tcase").toString();
			
			//get direct mapping
			String query2 = "PREFIX  rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#>\n" +
					"PREFIX dcterms: <http://purl.org/dc/elements/1.1/> \n" +
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
						"SELECT ?title ?id ?output WHERE {" +
					"   <"+relTestCase+"> rdf:type rdb2rdftest:DirectMapping; "+
					"   dcterms:title ?title;" +
					"   dcterms:identifier ?id;" +
					"  rdb2rdftest:output ?output}";
			TupleQuery dm = con.prepareTupleQuery(QueryLanguage.SPARQL, query2);
			TupleQueryResult dmres = dm.evaluate();
			while(dmres.hasNext())
			{
				BindingSet bset = dmres.next();
				 testURI = bset.getValue("id").stringValue();
				 testName = bset.getValue("title").toString();
				String outputFile = bset.getValue("output").toString();
				outputFile = outputFile.substring(1, outputFile.length()-1);
				
				logger.debug("Found test case: {}", testName);

				String pathUri =  manifestFileURL.substring(0, manifestFileURL.lastIndexOf('/')+1);
				String path = pathUri.substring(5);
				RDB2RDFScenarioParent test = factory.createRDB2RDFScenarioTest(testURI, testName, path + sqlFile,
						  null, path + outputFile);
				if (test != null) {
					suite.addTest(test);
				}
			}
			dmres.close();
				
			//get r2rml mapping
			String query3 = "PREFIX  rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#>\n" +
						"PREFIX dcterms: <http://purl.org/dc/elements/1.1/> \n" +
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
						"SELECT ?title ?id ?mapping WHERE {" +
						"   <"+relTestCase+"> rdf:type rdb2rdftest:R2RML; "+
						"   dcterms:title ?title;" +
						"   dcterms:identifier ?id;" +
						"  rdb2rdftest:mappingDocument ?mapping}";
			TupleQuery r2rml = con.prepareTupleQuery(QueryLanguage.SPARQL, query3);
			TupleQueryResult r2rmlRes = r2rml.evaluate();
			while(r2rmlRes.hasNext())
			{
				BindingSet bset = r2rmlRes.next();
				 testURI = bset.getValue("id").stringValue();
				 testName = bset.getValue("title").toString();
				 String mappingFile = bset.getValue("mapping").stringValue();
				 
				 
				 String outputFile = null;
				 String query4 = "PREFIX  rdb2rdftest: <http://purl.org/NET/rdb2rdf-test#>\n" +
							"PREFIX dcterms: <http://purl.org/dc/elements/1.1/> \n" +
							"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
							"SELECT ?output WHERE {" +
							"   <"+relTestCase+"> rdf:type rdb2rdftest:R2RML; "+
							"  rdb2rdftest:output ?output}";
				TupleQuery r2rml2 = con.prepareTupleQuery(QueryLanguage.SPARQL, query4);
				TupleQueryResult r2rmlRes2 = r2rml2.evaluate();
				while(r2rmlRes2.hasNext())
					{
						BindingSet bset2 = r2rmlRes2.next();
						outputFile = bset2.getValue("output").stringValue();
					}	
				logger.debug("Found test case: {}", testName);

				String pathUri =  manifestFileURL.substring(0, manifestFileURL.lastIndexOf('/')+1);
				String path = pathUri.substring(8);
				RDB2RDFScenarioParent test2 = null;
				if (outputFile == null) {
					test2 = factory.createRDB2RDFScenarioTest(testURI,
							testName, path + sqlFile, path + mappingFile, null);
				} else {
					test2 = factory.createRDB2RDFScenarioTest(testURI,
							testName, path + sqlFile, path + mappingFile, path
								+ outputFile);
				}
				if (test2 != null) {
					suite.addTest(test2);
				}
			}
			r2rmlRes.close();
			
		}

		testCases.close();
		con.close();

		manifestRep.shutDown();
		logger.info("Created test suite with " + suite.countTestCases() + " test cases.");
		return suite;
	}

	protected static String getManifestName(Repository manifestRep, RepositoryConnection con, String manifestFileURL)
		throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		// Try to extract suite name from manifest file
		TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
				"SELECT ManifestName FROM {ManifestURL} rdfs:label {ManifestName}");
		manifestNameQuery.setBinding("ManifestURL", manifestRep.getValueFactory().createURI(manifestFileURL));
		TupleQueryResult manifestNames = manifestNameQuery.evaluate();
		try {
			if (manifestNames.hasNext()) {
				return manifestNames.next().getValue("ManifestName").stringValue();
			}
		}
		finally {
			manifestNames.close();
		}
		// Derive name from manifest URL
		int lastSlashIdx = manifestFileURL.lastIndexOf('/');
		int secLastSlashIdx = manifestFileURL.lastIndexOf('/', lastSlashIdx - 1);
		return manifestFileURL.substring(secLastSlashIdx + 1, lastSlashIdx);
	}
}
