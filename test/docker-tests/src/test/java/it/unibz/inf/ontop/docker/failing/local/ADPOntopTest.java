package it.unibz.inf.ontop.docker.failing.local;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;

import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;

import java.io.FileReader;

/**
 *
 * @author 
 */
public class ADPOntopTest {
	
	final String owlFile = "/local/adp/npd-ql.owl";
	final String obdaFile = "/local/adp/mapping-fed.obda";
	final String queryFile = "/local/adp/01.q";
	final String propertyFile = "/local/adp/mapping-fed.properties";
	final String r2rmlfile = "/local/adp/mapping-fed.ttl";

	public void runQuery() throws Exception {

		String owlFileName =  this.getClass().getResource(owlFile).toString();
		String obdaFileName =  this.getClass().getResource(obdaFile).toString();
		String propertyFileName =  this.getClass().getResource(propertyFile).toString();
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.ontologyFile(owlFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();


		try (OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);
			 OWLConnection conn = reasoner.getConnection();
			 OWLStatement st = conn.createStatement()) {
			String sparqlQuery = Joiner.on("\n").join(
					CharStreams.readLines(new FileReader(queryFile)));
			TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
			while (rs.hasNext()) {
				final OWLBindingSet bindingSet = rs.next();
				System.out.print(bindingSet + "\n");
			}
			rs.close();

			/*
			 * Print the query summary
			 */
			OntopOWLStatement qst = (OntopOWLStatement) st;

			System.out.println();
			System.out.println("The input SPARQL query:");
			System.out.println("=======================");
			System.out.println(sparqlQuery);
			System.out.println();

			System.out.println("The output SQL query:");
			System.out.println("=====================");
			System.out.println(qst.getExecutableQuery(sparqlQuery));
		}
	}

  public static void main(String[] args) throws Exception {
	  new ADPOntopTest().runQuery();
	    
  }
}
