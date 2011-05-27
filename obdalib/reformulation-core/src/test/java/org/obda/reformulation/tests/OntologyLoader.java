package org.obda.reformulation.tests;

import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.model.QueryController;
import inf.unibz.it.obda.model.QueryControllerEntity;
import inf.unibz.it.obda.owlapi.OWLAPIController;
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;
import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatform;
import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactoryImpl;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OntologyLoader {


    public static void main(String args[]) {

        String owlfile = args[0];

        OBDAOWLReformulationPlatform reasoner = null;
        try {

            // Loading the OWL file
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

            // Loading the OBDA data (note, the obda file must be in the same folder as the owl file
            OWLAPIController controller = new OWLAPIController();
            String obdafile = owlfile.substring(0, owlfile.length()-3) + ".obda";
            controller.getIOManager().loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), controller.getPrefixManager());;

//            DataManager ioManager = controller.getIOManager();
//            URI obdaUri = ioManager.getOBDAFile(new File(owlfile).toURI());
//            ioManager.loadOBDADataFromURI(obdaUri);


            ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
            pref.setCurrentValueOf(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, true);
            pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, "inmemory");
            pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, "virtual");
            pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, "improved");

            OBDAOWLReformulationPlatformFactoryImpl factory = new OBDAOWLReformulationPlatformFactoryImpl();
            factory.setOBDAController(controller);
            factory.setPreferenceHolder(pref);

            reasoner = (OBDAOWLReformulationPlatform) factory.createReasoner(manager);
            reasoner.loadOntologies(manager.getOntologies());
            reasoner.classify();

            String prefix = "BASE <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                    "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n" +
                    "PREFIX dllite: <http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#>" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";

            // Executing the query
            QueryController queryCon = controller.getQueryController();
            Vector<QueryControllerEntity> s = queryCon.getElements();
            Vector<String> queryStrings = new Vector<String>();
            Vector<String> queryIds = new Vector<String>();

            Iterator<QueryControllerEntity> qit = s.iterator();
            while (qit.hasNext()) {
                QueryControllerEntity entity = qit.next();
                if (entity instanceof QueryControllerGroup) {
                    QueryControllerGroup group = (QueryControllerGroup) entity;
                    Vector<QueryControllerQuery> t = group.getQueries();
                    Iterator<QueryControllerQuery> it = t.iterator();
                    while (it.hasNext()) {
                        QueryControllerQuery query = it.next();
                        queryStrings.add(query.getQuery());
                        queryIds.add(query.getID());
                    }
                } else {
                    QueryControllerQuery query = (QueryControllerQuery) entity;
                    queryStrings.add(query.getQuery());
                    queryIds.add(query.getID());
                }
            }

            Iterator<String> query_it = queryStrings.iterator();
            Iterator<String> queryid = queryIds.iterator();
            while (query_it.hasNext()) {
                int resultcount = 0;
                String query = query_it.next();
                String id = queryid.next();

                System.out.println("ID: " + id);

                String sparqlstr = prefix + query;
                Statement statement = reasoner.getStatement();
                QueryResultSet result = statement.executeQuery(sparqlstr);

                // Printing the results
                System.out.println("Results:");
                if (result == null) {
                    System.out.print("no answers\n\n");
                } else {
                    int cols = result.getColumCount();
                    while (result.nextRow()) {
                        for (int i = 0; i < cols; i++) {
                            System.out.print(result.getAsString(i + 1) + " ");
                        }
                        System.out.println("");
                        resultcount += 1;
                    }
                    System.out.println("Result count: " + resultcount);
                    System.out.println("-------------------\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reasoner.dispose();
            } catch (Exception e) {

            }
        }
    }

}
