package it.unibz.inf.ontop.docker.failing.local;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.semanticweb.owlapi.model.OWLObject;

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
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.ontologyFile(owlFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

		/*
		 * Prepare the data connection for querying.
		 */
		OWLConnection conn = reasoner.getConnection();
		OWLStatement st = conn.createStatement();

		String sparqlQuery = Joiner.on("\n").join(
				CharStreams.readLines(new FileReader(queryFile)));
		
		//System.out.println(sparqlQuery);
		
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
			int columnSize = rs.getColumnCount();
			while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
				for (int idx = 1; idx <= columnSize; idx++) {
                    OWLObject binding = bindingSet.getOWLObject(idx);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
			rs.close();

			/*
			 * Print the query summary
			 */
			DefaultOntopOWLStatement qst = (DefaultOntopOWLStatement) st;
			String sqlQuery = ((SQLExecutableQuery)qst.getExecutableQuery(sparqlQuery)).getSQL();;

			System.out.println();
			System.out.println("The input SPARQL query:");
			System.out.println("=======================");
			System.out.println(sparqlQuery);
			System.out.println();
			
			System.out.println("The output SQL query:");
			System.out.println("=====================");
			System.out.println(sqlQuery);
			
		} finally {
			
			/*
			 * Close connection and resources
			 */
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			reasoner.dispose();
		}
	}

  public static void main(String[] args) throws Exception {
	  new ADPOntopTest().runQuery();
	    
  }
}
