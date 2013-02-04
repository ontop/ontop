package it.unibz.krdb.obda.quest.benchmark;

import it.unibz.krdb.obda.gui.swing.exception.InvalidMappingException;
import it.unibz.krdb.obda.gui.swing.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class QuestQueryPrint {

	public static void main(String[] args) {
		String owlFile = args[0];
		String obdaFile = args[1];
		String queryFile = args[2];
		String outputFile = args[3];
		String useRewriting = args[4];
		try {
			executeQueries(owlFile, obdaFile, queryFile, outputFile, useRewriting);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void executeQueries(String owlFile, String obdaFile, String queryFile, String outputFile, String useRewriting)
			throws OWLException, IOException, InvalidMappingException,
			OBDAException, InvalidPredicateDeclarationException {
		/*
		 * Loading the OWL ontology file
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

		/*
		 * Loading the OBDA model file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);

		/*
		 * Preparing the configuration for the new Quest instance.
		 */
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		p.setCurrentValueOf(QuestPreferences.REWRITE, useRewriting);

		/*
		 * Creating the instance of the reasoner using the factory.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(p);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Now we are ready to query.
		 */
		OWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = (QuestOWLStatement) conn.createStatement();
		QuestStatement qst = st.getQuestStatement();
		
		/*
		 * Load the query file.
		 */
		QueryController qc = new QueryController();
		QueryIOManager qim = new QueryIOManager(qc);
		qim.load(new File(queryFile));

		/*
		 * Output buffer
		 */
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		try {
			List<QueryControllerEntity> entities = qc.getElements();
			for (QueryControllerEntity entity : entities) {
				if (entity instanceof QueryControllerQuery) {
					QueryControllerQuery query = (QueryControllerQuery) entity;
					String sparql = query.getQuery();
					
					System.out.println("Executing " + query.getID());
					st.execute(sparql);
										
					out.write("========== " + query.getID() + " ==========\n\n");
					out.write(sparql);
					out.write("\n----------------------\n\n");
					out.write(qst.getSqlString(sparql));
					out.write("\n\n");
					out.flush();
				}
			}
			System.out.println("Finish.");
		} catch (OWLException e) {
			throw e;
		} finally {
			st.close();
			conn.close();
			reasoner.dispose();
			out.close();
		}
	}
}
