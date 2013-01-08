package inf.unibz.ontp.sesame.repository.test;


import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import org.junit.Test;
import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Statement;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.sail.nativerdf.NativeStore;

import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;
import sesameWrapper.StartJetty;


public class RepoTest {

	//@Test
	public void test() {
	
		System.out.println("\nTEST1....");
		try {
			
			//HTTPClient client = new HTTPClient();
			//System.out.println(client.getServerURL());
			
			String sesameServer = "http://localhost:8080/openrdf-sesame";
			String repositoryID = "mytest";

			Repository myRepository = new HTTPRepository(sesameServer, repositoryID);
			myRepository.initialize();
			
			 RepositoryConnection con = myRepository.getConnection();
			   try {
			      String queryString = "SELECT * where {<http://protege.stanford.edu/mv#MiniVan> ?p ?o} Limit 10";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			      try {
			    	  while(result.hasNext()){
			      System.out.println(result.next());
			    	  }
			      }
			      finally {
			         result.close();
			      }
			   }
			   finally {
			      con.close();
			   }
			
			myRepository.shutDown();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	
	//@Test
	public void test2()
	{try{

		System.out.println("\nTEST2....");
		String url = "http://localhost:8080/openrdf-sesame";
		RemoteRepositoryManager man = new RemoteRepositoryManager(url);
		man.initialize();
		String s = man.getServerURL();
		System.out.println(s);
		
		Set<String> ss = man.getRepositoryIDs();
		System.out.println(ss);
		
		Repository mytest = man.getRepository("mytest");
		System.out.println(mytest.getConnection().getContextIDs().asList().toString());

		
		RepositoryImplConfig implConfig = new HTTPRepositoryConfig(url);
		RepositoryConfig config = new RepositoryConfig("testhttp", implConfig);
		man.addRepositoryConfig(config);
		
		Repository rep = man.getRepository("testhttp");
		
		System.out.println(rep.getConnection().toString());
		
		man.shutDown();
		
		
	}catch(Exception e)
	{	e.printStackTrace();
	System.out.println(e.getMessage());
	}
	}
	
//	@Test
	public void test3()
	{
		LocalRepositoryManager man = null;
		try{

			System.out.println("\nTEST3....");
			File dataDir = new File("c:\\Project\\Timi\\");
			man = new LocalRepositoryManager(dataDir);
			man.initialize();
			
			SailImplConfig backendConfig = new MemoryStoreConfig();
			 
			// create a configuration for the repository implementation
			RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
			
			
			String repositoryId = "test-db";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
			 
			Repository repository = man.getRepository(repositoryId);
			
			System.out.println(repository.getClass().toString());
			
		}catch(Exception e)
		{	e.printStackTrace();
		System.out.println(e.getMessage());
		}
		finally{
			man.shutDown();
		}
	}
	
	
	
	//@Test
	public void test4(){
		try{
			System.out.println("\nTEST4....");
			File dataDir = new File("c:\\Project\\Timi\\");
			String owlfile = "C:\\Users\\TiBagosi\\Downloads\\openrdf-sesame-2.6.9-sdk\\openrdf-sesame-2.6.9\\bin\\onto2.owl";
			//"src/test/resources/onto2.owl";

			
			LocalRepositoryManager man = new LocalRepositoryManager(dataDir);
			man.initialize();
			
				 
			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig conf = new SesameRepositoryConfig();
			conf.setName("myrepo");
			conf.setQuestType("quest-inmemory");
			conf.setOwlFile(owlfile);
			
		//	System.out.println(RepositoryRegistry.getInstance().get("obda:QuestRepository").getRepositoryType());
			RepositoryImplConfig repositoryTypeSpec =  conf;
			
			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
			 
			
			Repository repository = man.getRepository(repositoryId);
			
			RepositoryConfig cnf = man.getRepositoryConfig(repositoryId);
			System.out.println(cnf.getRepositoryImplConfig().getType());
			System.out.println(repository.getClass().toString());
			
			
			 RepositoryConnection con = repository.getConnection();
			
			 File ff = new File("C:\\Users\\TiBagosi\\Downloads\\openrdf-sesame-2.6.9-sdk\\openrdf-sesame-2.6.9\\bin\\onto2plus.rdf");
			 //"src/test/resources/onto2plus.owl");
		
			 
			 con.add(ff, "http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#", RDFFormat.RDFXML);
			 
			
			      String queryString = "SELECT * where {?s ?p ?o} Limit 20";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			    	  while(result.hasNext())
			        System.out.println(result.next());
			    	 
			  con.close();
			  repository.shutDown();
		
			 man.removeRepositoryConfig(repositoryId);
			man.shutDown();
			  
		}catch(Exception e)
		{	e.printStackTrace();
		System.out.println(e.getMessage());
		}
	}

	@Test
	public void test5()
	{
		try{
			System.out.println("\nTEST5....");
			String owlfile = "onto2.owl";
					//"C:\\Users\\TiBagosi\\Downloads\\openrdf-sesame-2.6.9-sdk\\openrdf-sesame-2.6.9\\bin\\onto2.owl";
			//"src/test/resources/onto2.owl";

			Thread t = new Thread(new StartJetty());
			//t.start();
			//t.join();
			
		
			RemoteRepositoryManager man = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
			man.initialize();
			Set<String>ss = man.getRepositoryIDs();
			for (String s: ss)
				System.out.println(s);
			
			// create a configuration for the repository implementation
			SesameRepositoryFactory f = new SesameRepositoryFactory();
			RepositoryRegistry.getInstance().add(f);
			SesameRepositoryConfig conf = new SesameRepositoryConfig();
			conf.setName("myrepo");
			conf.setQuestType("quest-inmemory");
			conf.setOwlFile(owlfile);
			
			//SailImplConfig backendConfig = new MemoryStoreConfig();
			
			RepositoryImplConfig repositoryTypeSpec =  conf;// new SailRepositoryConfig(backendConfig);
			
			String repositoryId = "testdb";
			RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
			man.addRepositoryConfig(repConfig);
			
			RepositoryConfig cnf = man.getRepositoryConfig(repositoryId);
			System.out.println(cnf.getRepositoryImplConfig().toString());
			 
			ss = man.getRepositoryIDs();
			for (String s: ss)
				{
				System.out.println(s);
				}
			
			Repository repository = man.getRepository(repositoryId);
			
			 RepositoryConnection con = repository.getConnection();
				
			 System.out.println(con.getClass().getName());
			 File ff = new File("src/test/resources/onto2plus.owl");
					 //"C:\\Users\\TiBagosi\\Downloads\\openrdf-sesame-2.6.9-sdk\\openrdf-sesame-2.6.9\\bin\\onto2plus.rdf");
			 //"src/test/resources/onto2plus.owl");
		
			 
			 con.add(ff, "http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#", RDFFormat.RDFXML);
		
			 
			System.out.println("Conn empty: "+con.isEmpty());
			      String queryString = "SELECT * WHERE {?s ?p ?o} Limit 20";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			      System.out.println("RESULT hasdata: "+result.hasNext());
			    	  while(result.hasNext())
			        System.out.println(result.next());
			    	 
			  con.close();
			  repository.shutDown();
			  man.removeRepositoryConfig("testdb");
			  man.shutDown();
			  
		}catch(Exception e)
		{	e.printStackTrace();
		System.out.println(e.getMessage());
		}
	}
}
