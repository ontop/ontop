package org.semanticweb.ontop.owlrefplatform.owlapi3.example;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class InteractiveExample {

	/*
	 * Use the sample database using H2 from
	 * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
	 * 
	 * Please use the pre-bundled H2 server from the above link
	 * 
	 */
	final String owlfile = "src/main/resources/example/npd-v2-ql_a.owl";
	final String obdafile = "src/main/resources/example/npd-v2-ql_a.obda";
	
	// Exclude from T-Mappings
	final String tMappingsConfFile = "src/main/resources/example/tMappingsConf.conf";

	public void runQuery() throws Exception {

		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		QuestPreferences preference = new QuestPreferences(p);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), preference);
		
//		/*
//		 * T-Mappings Handling!!
//		 */
//		TMappingsConfParser tMapParser = new TMappingsConfParser(tMappingsConfFile);
//		factory.setExcludeFromTMappingsPredicates(tMapParser.parsePredicates());
		
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		
		String outFile = "src/main/resources/davide/QueriesStdout/prova";
		
		
		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		QuestOWLStatement st = conn.createStatement();
		while(true){
			System.out.println("INSERT A QUERY");
			try {
				StringBuilder builder = new StringBuilder();
				String curLine = null;
				while( !(curLine = br.readLine()).equals("!!") ){
					builder.append(curLine+"\n");
				}
				String sparqlQuery = builder.toString();
				System.out.println(sparqlQuery);
				System.out.println("INSERT A LABEL");
				String label = br.readLine();
				QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
				rs.close();
				
			/*
			 * Print the query summary
			 */
				QuestOWLStatement qst = st;
				String sqlQuery = qst.getUnfolding(sparqlQuery);
								
				System.out.println();
				System.out.println("The input SPARQL query:");
				System.out.println("=======================");
				System.out.println(sparqlQuery);
				System.out.println();
				
				System.out.println("The output SQL query:");
				System.out.println("=====================");
				System.out.println(sqlQuery);
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}	
	}
		
	/**
	 * Main client program
	 */
	public static void main(String[] args) {
		
		try {
			InteractiveExample example = new InteractiveExample();

				example.runQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

