package org.semanticweb.ontop.sql;


import java.io.File;
import java.io.FileReader;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;

/**
 *
 * @author 
 */
public class ADPOntopTest {
	
	final String owlfile = "src/test/resources/adp/npd-ql.owl";
	final String obdafile = "src/test/resources/adp/mapping-fed.obda";
	final String queryfile = "src/test/resources/adp/01.q";
	final String r2rmlfile = "src/test/resources/adp/mapping-fed.ttl";

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
		QuestPreferences preferences = new QuestPreferences();
		preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), preferences);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String sparqlQuery = Joiner.on("\n").join(
				CharStreams.readLines(new FileReader(queryfile))); 
		
		//System.out.println(sparqlQuery);
		
		try {
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			int columnSize = rs.getColumnCount();
			while (rs.nextRow()) {
				for (int idx = 1; idx <= columnSize; idx++) {
					OWLObject binding = rs.getOWLObject(idx);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
			rs.close();

			/*
			 * Print the query summary
			 */
			QuestOWLStatement qst = (QuestOWLStatement) st;
			String sqlQuery = qst.getUnfolding(sparqlQuery);

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
