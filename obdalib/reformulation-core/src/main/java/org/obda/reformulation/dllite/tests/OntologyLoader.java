package org.obda.reformulation.dllite.tests;

import inf.unibz.it.obda.api.controller.QueryController;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.io.DataManager;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.owlapi.OWLAPIController;
import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Vector;

import org.obda.owlreformulationplatform.preferences.ReformulationPlatformPreferences;
import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatform;
import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactoryImpl;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OntologyLoader {

	private static String owlfile = null;

	public static void main(String args[]){

		owlfile = args[0];

		OBDAOWLReformulationPlatform reasoner = null;
	    try {

	            // Loading the OWL file
	            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	            OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

	            // Loading the OBDA data (note, the obda file must be in the same folder as the owl file
	            OWLAPIController controller = new OWLAPIController(manager, ontology);
	            controller.loadData(new File(owlfile).toURI());

	            DataManager ioManager = controller.getIOManager();
	            URI obdaUri = ioManager.getOBDAFile(new File(owlfile).toURI());
	            ioManager.loadOBDADataFromURI(obdaUri);

	            // Creating a new instance of a quonto reasoner
	            OBDAOWLReformulationPlatformFactoryImpl factory = new OBDAOWLReformulationPlatformFactoryImpl();
	            factory.setup(manager, "asdf", "asdf");
	            factory.setOBDAController(controller);

	            ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
	            ReformulationPlatformPreferences.setDefaultValueOf(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, "false");
	            ReformulationPlatformPreferences.setDefaultValueOf(ReformulationPlatformPreferences.USE_INMEMORY_DB, "false");
	            ReformulationPlatformPreferences.setDefaultValueOf(ReformulationPlatformPreferences.UNFOLDING_MECHANMISM, "complex");

	            reasoner = (OBDAOWLReformulationPlatform) factory.createReasoner(manager);
	            reasoner.loadOntologies(manager.getOntologies());
	            reasoner.classify();
	            String prefix = "PREFIX :		<http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
	            					"PREFIX onto:		<http://www.owl-ontologies.com/Ontology1207768242.owl#> \n"+
	    						   "PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" ;
//	    						   "SELECT ?x WHERE { ?x rdf:type onto:Investor }";

	            
//	            http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-991 991 Road street Chonala Mexico Veracruz 24 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-992 992 Via Marconi Bolzano Italy Bolzano 3 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-993 993 Romer Street Malaga Spain Malaga 32 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-995 995 Huberg Strasse Bolzano Italy Bolzano 3 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-996 996 Via Piani di Bolzano Marconi Italy Trentino 7 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-997 997 Samara road Puebla Mexico Puebla 9976 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-998 998 ID address city country state 245 
//	            	-------------------
//	            	Results:
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getPersonObj-111 John Smith http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-991 Road street Chonala Mexico 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getPersonObj-112 Joana Lopatenkko http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-992 Via Marconi Bolzano Italy 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getPersonObj-113 Walter Schmidt http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-993 Romer Street Malaga Spain 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getPersonObj-114 Patricia Lombrardi http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-997 Samara road Puebla Mexico 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getPersonObj-118 name1 lastname1 http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-998 ID address city country 
//	            	http://www.owl-ontologies.com/Ontology1207768242.owl#getPersonObj-119 name2 lastname2 http://www.owl-ontologies.com/Ontology1207768242.owl#getAddressObj-998 ID address city country 
//	            	-------------------
//	            	Results:
//	            	Joana Lopatenkko JLPTK54992 
//	            	-------------------
//	            	Results:
//	            	Joana Lopatenkko JLPTK54992 
//	            	Patricia Lombrardi PTLM8878767830 
//	            	-------------------
	            
	            
	            String sparqlstr = "PREFIX :		<http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
									"PREFIX onto:		<http://www.owl-ontologies.com/Ontology1207768242.owl#> \n"+
									"PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			   						"SELECT ?x ?y ?g WHERE { ?x onto:addressID '991'. ?x onot:inState ?y. ?x onto:inStreet ?g }";


	            // Executing the query

//	            QueryController queryCon = controller.getQueryController();
//	            Vector<QueryControllerEntity> s = queryCon.getElements();
//	            Vector<String> queryStrings = new Vector<String>();
//	            Iterator<QueryControllerEntity> qit = s.iterator();
//	            while(qit.hasNext()){
//	            	QueryControllerEntity entity = qit.next();
//	            	if(entity instanceof QueryControllerGroup){
//	            		QueryControllerGroup group = (QueryControllerGroup) entity;
//	            		Vector<QueryControllerQuery> t = group.getQueries();
//	            		Iterator<QueryControllerQuery> it = t.iterator();
//	            		while(it.hasNext()){
//	            			QueryControllerQuery query = it.next();
//	            			queryStrings.add(query.getQuery());
//	            		}
//	            	}else{
//	            		QueryControllerQuery query = (QueryControllerQuery) entity;
//            			queryStrings.add(query.getQuery());
//	            	}
//	            }
//	            
//	            Iterator<String> query_it = queryStrings.iterator();
//	            while(query_it.hasNext()){
//	            	String query =query_it.next();
//	            	String sparqlstr = prefix+query;
		            Statement statement = reasoner.getStatement(sparqlstr);
		            QueryResultSet result = statement.getResultSet();
	
		            // Printing the results
		            System.out.println("Results:");
		            if(result == null){
		            	System.out.print("no answers");
		            }else{
			            int cols = result.getColumCount();
			            while (result.nextRow()) {
			                    for (int i = 0; i < cols; i++) {
			                            System.out.print(result.getAsString(i+1) + " ");
			                    }
			                    System.out.println("");
			            }
			            System.out.println("-------------------");
		            }
//	            }

	    } catch (Exception e) {
	            e.printStackTrace();
	    } finally {
	            try {
	                    reasoner.dispose();
	            } catch (Exception e)  {

	            }
	    }
	}

}
