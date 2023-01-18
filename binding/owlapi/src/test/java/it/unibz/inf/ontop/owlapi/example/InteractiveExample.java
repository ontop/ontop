package it.unibz.inf.ontop.owlapi.example;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;

import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class InteractiveExample {

    /*
     * Use the sample database using H2 from
     * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
     *
     * Please use the pre-bundled H2 server from the above link
     *
     */

	final String owlfile = "src/test/resources/example/exampleBooks.owl";
	final String obdafile = "src/test/resources/example/exampleBooks.obda";
	final String propertyFile = "src/test/resources/example/exampleBooks.properties";

    // Exclude from T-Mappings
    final String tMappingsConfFile = "src/test/resources/example/tMappingsConf.conf";

	public void runQuery() throws Exception {

//		/*
//		 * T-Mappings Handling!!
//		 */
//		TMappingsConfParser tMapParser = new TMappingsConfParser(tMappingsConfFile);
//		factory.setExcludeFromTMappingsPredicates(tMapParser.parsePredicates());

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(propertyFile)
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.enableTestMode()
				.build();
		OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);


	/*
	 * Prepare the data connection for querying.
	 */
		OntopOWLConnection conn = reasoner.getConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		OntopOWLStatement st = conn.createStatement();
		while(true){
			System.out.println("INSERT A SELECT QUERY");
			try {
				StringBuilder builder = new StringBuilder();
				String curLine;
				while( !(curLine = br.readLine()).equals("!!") ){
					builder.append(curLine).append("\n");
				}
				String sparqlQuery = builder.toString();
				System.out.println(sparqlQuery);
				TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
				rs.close();
				
			/*
			 * Print the query summary
			 */
								
				System.out.println();
				System.out.println("The input SPARQL query:");
				System.out.println("=======================");
				System.out.println(sparqlQuery);
				System.out.println();
				
				System.out.println("The output SQL query:");
				System.out.println("=====================");
				System.out.println(st.getExecutableQuery(sparqlQuery));
				
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

