package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi2.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi2.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi2.QuestOWLFactory;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OntologyLoader {

	public static void main(String args[]) {

		String owlfile = args[0];

		QuestOWL reasoner = null;
		try {

			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

			// Loading the OBDA data (note, the obda file must be in the same
			// folder as the owl file
			OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
			OBDAModel controller = obdafac.getOBDAModel();
			String obdafile = owlfile.substring(0, owlfile.length() - 3) + ".obda";
			DataManager ioManager = new DataManager(controller);
			ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), controller.getPrefixManager());
			;

			// DataManager ioManager = controller.getIOManager();
			// URI obdaUri = ioManager.getOBDAFile(new File(owlfile).toURI());
			// ioManager.loadOBDADataFromURI(obdaUri);

			QuestPreferences pref = new QuestPreferences();
			pref.setCurrentValueOf(QuestPreferences.DBTYPE, "inmemory");
			pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, "virtual");
			pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, "improved");
			pref.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
			pref.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");

			QuestOWLFactory factory = new QuestOWLFactory();
			// factory.setOBDAController(controller);
			factory.setPreferenceHolder(pref);

			reasoner = (QuestOWL) factory.createReasoner(manager);
			reasoner.loadOntologies(manager.getOntologies());
			reasoner.classify();

			String prefix = "BASE <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n"
					+ "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> \n"
					+ "PREFIX dllite: <http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#>"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";

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
			OBDAStatement statement = reasoner.getStatement();

			while (query_it.hasNext()) {
				int resultcount = 0;
				String query = query_it.next();
				String id = queryid.next();

				// System.out.println("ID: " + id);

				String sparqlstr = prefix + query;
				OBDAResultSet result = statement.execute(sparqlstr);

				// Printing the results
				// System.out.println("Results:");
				if (result == null) {
					// System.out.print("no answers\n\n");
				} else {
					int cols = result.getColumCount();
					while (result.nextRow()) {
						for (int i = 0; i < cols; i++) {
							// System.out.print(result.getAsString(i + 1) +
							// " ");
						}
						// System.out.println("");
						resultcount += 1;
					}
					result.close();
					
					// System.out.println("Result count: " + resultcount);
					// System.out.println("-------------------\n");
				}
			}
			statement.close();

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
