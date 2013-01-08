package inf.unibz.ontp.sesame.repository.test;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelSynchronizer;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameClassicJDBCRepo;
import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;
import sesameWrapper.StartJetty;

public class QuestRepos {

	// classic
	 @Test
	public void test_inmemory() {
		RepositoryConnection con = null;
		try {
			System.out.println("In-memory quest repo.");			

			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-inmemory");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-h2-unittest.owl");

			Repository repo = fact.getRepository(config);

			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();


			 String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			 TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			 TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			result.close();
			
			File file = new File("C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/new 2.ttl");
			String baseURI = "<http://www.owl-ontologies.com/Ontology1207768242.owl#>";
			con.add(file,  baseURI, RDFFormat.TURTLE);
			con.commit();
			
			 String queryString2 = "SELECT * WHERE {?s ?p ?o} Limit 20";
			 TupleQuery tupleQuery2 = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString2);
			 TupleQueryResult result2 = tupleQuery2.evaluate();
			System.out.println("RESULT hasdata: " + result2.hasNext());
			while (result2.hasNext())
				System.out.println(result2.next());
			result2.close();
			//con.export(new RDFHandlerBase(), (Resource)null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	 
	// @Test
	public void test_inmemory_localhost() {
		org.openrdf.repository.RepositoryConnection con = null;
		try {
			System.out.println("In-memory quest repo.");
			
			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);

			// create a configuration for the repository implementation
			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-inmemory");
			config.setName("my_remote");
			config.setOwlFile("C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/stockexchange-h2-unittest.owl");
			RepositoryRegistry.getInstance().add(fact);

			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId,
					repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);

			Repository repository = man.getRepository(repositoryId);

			con = repository.getConnection();
			File f = new File("C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/stockexchange-h2-unittest.ttl");
			con.add(f, "http://www.owl-ontologies.com/Ontology1207768242.owl#", RDFFormat.TURTLE, (Resource)null);
			
			String queryString = "SELECT * WHERE {?s a ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			result.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// @Test
	public void test_remote() {
		System.out.println("\n\n\nRemote quest repo................");
		RepositoryConnection con = null;
		try {
			// String owlfile = "src/test/resources/onto2.owl";

			// setupDB();
			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-remote");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-h2-unittest.owl");

			Repository repo = fact.getRepository(config);

			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	 
	// @Test
	public void test_remote_localhost() {
		System.out.println("\n\n\nRemote quest repo................");
		org.openrdf.repository.RepositoryConnection con = null;
		try {
			
			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);

			// create a configuration for the repository implementation
			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-remote");
			config.setName("my_remote");
			config.setOwlFile("stockexchange-h2-unittest.owl");
			RepositoryRegistry.getInstance().add(fact);
			
			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
		
			Repository repository = man.getRepository(repositoryId);
			
			con = repository.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// virtual
	// @Test
	public void test_virtual() {
		RepositoryConnection con = null;

		try {
			System.out.println("\n\n\nVirtual quest repo.....................");
			//setupDB();

			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(fact);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile("stockexchange-h2-unittest.owl");
			config.setObdaFile("stockexchange-h2-unittest.obda");

			Repository repo = fact.getRepository(config);

			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 5";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			
			 queryString = "SELECT * WHERE {?s ?p ?o} Limit 10";
			 tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			 result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			result.close();
			
			/* queryString = "PREFIX :<http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
			 		"CONSTRUCT {?s :worksFor ?o } WHERE {?s :belongsToCompany ?o} Limit 20";
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,	queryString);
			GraphQueryResult resultg = graphQuery.evaluate();
			System.out.println("RESULT hasdata: " + resultg.hasNext());
			while (resultg.hasNext())
			{
				org.openrdf.model.Statement st = resultg.next();
				System.out.println(st.getSubject().stringValue() + " "+ st.getPredicate().stringValue()+" "+st.getObject().stringValue());
			}
*/
			//con.close();
			repo.shutDown();
			
			repo.initialize();

			con = (RepositoryConnection) repo.getConnection();

			 queryString = "SELECT * WHERE {?s ?p ?o} Limit 5";
			 tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			 result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			
			
			System.out.println("Con is open: "+con.isOpen());
			System.out.println("Result is readable: "+result.hasNext());
			
			repo.shutDown();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void test_virtual_local(){
		try {

			System.out.println("\nVirtal quest repo test....");
			
			String owlfile = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/stockexchange-h2-unittest.owl";//bsbm/bsbm.owl";
			String obdafile = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/stockexchange-h2-unittest.obda";//bsbm/bsbm.obda";

			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);
					
			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile(owlfile);
			config.setObdaFile(obdafile);

			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "stockexch";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
		
			Repository repository = man.getRepository(repositoryId);
			
			repository.initialize();

			org.openrdf.repository.RepositoryConnection con = repository.getConnection();
			
			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 10";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			
			if (con.isOpen()) {
				queryString = "SELECT * WHERE {?s ?p ?o} Limit 10";
				tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
						queryString);
				result = tupleQuery.evaluate();
				System.out.println("RESULT hasdata: " + result.hasNext());
				while (result.hasNext())
					System.out.println(result.next());

				con.close();
			}
			
			repository.shutDown();
			man.removeRepositoryConfig(repositoryId);
			man.shutDown();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	
	//@Test
	public void test_virtual_localhost() {

		try {

			System.out.println("\nVirtal quest repo test....");
			
			String owlfile = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/stockexchange-h2-unittest.owl";//bsbm/bsbm.owl";
			String obdafile = "C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps/Quest/stockexchange/stockexchange-h2-unittest.obda";//bsbm/bsbm.obda";

			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);
					
			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile(owlfile);
			config.setObdaFile(obdafile);

			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "stockexchange";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
		
			Repository repository = man.getRepository(repositoryId);

			org.openrdf.repository.RepositoryConnection con = repository.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 10";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			 queryString = "SELECT * WHERE {?s ?p ?o} Limit 10";
			 tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			 result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			result.close();
			
		/*	 queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			 		"PREFIX rev: <http://purl.org/stuff/rev#>\n" +
			 		"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
			 		"PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
			 		"PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>\n" +
			 		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
			 		"CONSTRUCT {  " +
			 		"				 <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:product ?productURI ." +
			 		"<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:productlabel ?productlabel ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:vendor ?vendorname ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:vendorhomepage ?vendorhomepage . " +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:offerURL ?offerURL ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:price ?price ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:deliveryDays ?deliveryDays ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm-export:validuntil ?validTo } " +
			 		"WHERE { " +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm:product ?productURI ." +
			 		"    ?productURI rdfs:label ?productlabel ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm:vendor ?vendorURI ." +
			 		"    	?vendorURI rdfs:label ?vendorname ." +
			 		"    ?vendorURI foaf:homepage ?vendorhomepage ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm:offerWebpage ?offerURL ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm:price ?price ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm:deliveryDays ?deliveryDays ." +
			 		"    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer196> bsbm:validTo ?validTo }";
			 GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,	queryString);
			GraphQueryResult qresult = graphQuery.evaluate();
			System.out.println("RESULT hasdata: " + qresult.hasNext());
			while (qresult.hasNext())
			{
				org.openrdf.model.Statement st = qresult.next();
				System.out.println(st.getSubject().stringValue() + " "+ st.getPredicate().stringValue()+" "+st.getObject().stringValue());
			}
			
			
			 queryString = "PREFIX  rev: <http://purl.org/stuff/rev#> \n" +
			 		"DESCRIBE ?x " +
			 		"WHERE \n { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite1/Review992> rev:reviewer ?x }";
			 System.out.println(queryString);
			 graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,	queryString);
				 qresult = graphQuery.evaluate();
				System.out.println("RESULT hasdata: " + qresult.hasNext());
				while (qresult.hasNext())
				{
					org.openrdf.model.Statement st = qresult.next();
					System.out.println(st.getSubject().stringValue() + " "+ st.getPredicate().stringValue()+" "+st.getObject().stringValue());
				}
				
				*/
			//repository.shutDown();
			man.removeRepositoryConfig(repositoryId);
			man.shutDown();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	
	//@Test
	public void test_virtual_fish() {

		try {

			System.out.println("\nVirtal quest fish test....");
			
			String owlfile = "//Ubz01fst/Profs/User/TiBagosi/Desktop/mappings/fishdelish/fishdelish-simple.owl";
			String obdafile = "//Ubz01fst/Profs/User/TiBagosi/Desktop/mappings/fishdelish/fishdelish-simple.obda";

			RemoteRepositoryManager man = new RemoteRepositoryManager("http://10.10.160.28:8080/openrdf-sesame");
			man.initialize();
			Set<String> ss = man.getRepositoryIDs();
			for (String s : ss)
				System.out.println(s);
					
			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig config = new SesameRepositoryConfig();
			config.setQuestType("quest-virtual");
			config.setName("my_repo");
			config.setOwlFile(owlfile);
			config.setObdaFile(obdafile);

			RepositoryImplConfig repositoryTypeSpec = config;

			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
		
			Repository repository = man.getRepository(repositoryId);
			System.out.println(repository.getConnection().isOpen());
			org.openrdf.repository.RepositoryConnection con = repository.getConnection();

			String queryString = "SELECT * WHERE {?s ?p ?o} Limit 10";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			System.out.println("RESULT hasdata: " + result.hasNext());
			while (result.hasNext())
				System.out.println(result.next());
			
		
			//repository.shutDown();
			man.removeRepositoryConfig("testdb");
			man.shutDown();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}


}
