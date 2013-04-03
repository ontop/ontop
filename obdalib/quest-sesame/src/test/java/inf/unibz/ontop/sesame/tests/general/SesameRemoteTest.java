package inf.unibz.ontop.sesame.tests.general;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelSynchronizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import junit.framework.TestCase;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.SesameClassicJDBCRepo;


public class SesameRemoteTest extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "../quest-owlapi3/src/test/resources/test/stockexchange-unittest.owl";
	final String obdafile = "../quest-owlapi3/src/test/resources/test/stockexchange-h2-unittest.obda";
	
	public void setup() throws Exception
	{
		/* * Initializing and H2 database with the stock exchange data
		 */
	  //  String driver = "org.h2.Driver";
	//	String url = "jdbc:h2:tcp://localhost/quest";
	    String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("../quest-owlapi3/src/test/resources/test/stockexchange-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();
		
		st.executeUpdate(bf.toString());
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(new File(obdafile));

		OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);

	}
	
	
	public void test() throws Exception
	{	
		setup();
		
		//create a sesame virtual repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			repo = new SesameClassicJDBCRepo("my_name", owlfile);
			repo.initialize();
			con = repo.getConnection();
			
		///query repo
		      String queryString = 
		      		"SELECT * WHERE {?x ?p ?y} limit 10";
		      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		      TupleQueryResult result = tupleQuery.evaluate();
		      try {
		    	  while (result.hasNext()) {
		    		   BindingSet bindingSet = result.next();
		    		   Value valueOfX = bindingSet.getValue("x");
		    		   Value valueOfY = bindingSet.getValue("y");
		    		   System.out.println("x="+valueOfX.toString()+", y="+valueOfY.toString());
		    	  }
		      }
		      finally {
		         result.close();
		      }
			  System.out.println("Closing...");
		      con.close();
		   }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }		   
		  
	System.out.println("Done.");	
	}

}

