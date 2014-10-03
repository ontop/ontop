package it.unibz.krdb.obda.reformulation.owlapi3;

import it.unibz.krdb.config.tmappings.parser.TMappingsConfParser;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.sql.ImplicitDBConstraints;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import junit.framework.TestCase;

public class TMappingDisablingTest extends TestCase {
	
	// Let's use emptiesDatabase
	final String owlfile = "src/test/resources/test/tmapping/exampleTMapping.owl";
	final String obdafile = "src/test/resources/test/tmapping/exampleTMapping.obda";
//	final String usrConstrinFile = "src/main/resources/example/funcCons.txt";

	// Exclude from T-Mappings
	final String tMappingsConfFile = "src/test/resources/test/tmapping/exampleTMappingConf.conf";
	
	public void testDisableTMappings(){
		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
		
		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		QuestPreferences preference = new QuestPreferences();
		preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		preference.setCurrentValueOf(QuestPreferences.T_MAPPINGS, QuestConstants.FALSE); // Disable T_Mappings
		
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(preference);
		
//		/*
//		 * USR CONSTRAINTS !!!!
//		 */
//		ImplicitDBConstraints constr = new ImplicitDBConstraints(usrConstrinFile);
//		factory.setImplicitDBConstraints(constr);
//		
//		/*
//		 * T-Mappings Handling!!
//		 */
//		TMappingsConfParser tMapParser = new TMappingsConfParser(tMappingsConfFile);
//		factory.setExcludeFromTMappingsPredicates(tMapParser.parsePredicates());

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		
		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();
		

		/*
		 * Get the book information that is stored in the database
		 */
		
		
		
			
			
			String sparqlQuery = "PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y .}  ";;
				QuestOWLStatement st = conn.createStatement();
				try {
		            
					long time = 0;
					int count = 0;
					
					for (int i=0; i<4; i++){
						long t1 = System.currentTimeMillis();
						QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
						int columnSize = rs.getColumnCount();
						 count = 0;
						while (rs.nextRow()) {
							count ++;
							for (int idx = 1; idx <= columnSize; idx++) {
								OWLObject binding = rs.getOWLObject(idx);
								//System.out.print(binding.toString() + ", ");
							}
							//System.out.print("\n");
						}
						long t2 = System.currentTimeMillis();
						time = time + (t2-t1);
						System.out.println("partial time:" + time);
						rs.close();
	}
}
