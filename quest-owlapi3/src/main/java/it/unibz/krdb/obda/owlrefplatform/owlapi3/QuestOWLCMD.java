package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class QuestOWLCMD {

	private static QuestOWL reasoner;
	private static OWLConnection conn;
	private static OWLStatement st;

	public static void main(String args[]) {

		if (args.length != 3 && args.length != 4) {
			System.out.println("Usage");
			System.out.println(" QuestOWLCMD owlfile obdafile queryile [outputfile]");
			System.out.println("");
			System.out.println(" owlfile    The full path to the OWL file");
			System.out.println(" obdafile   The full path to the OBDA file");
			System.out.println(" queryfile  The full path to the file with the SPARQL query");
			System.out.println(" outputfile [OPTIONAL] The full path to the file with the SPARQL query");
			System.out.println("");
			return;
		}

		String owlfile = args[0].trim();
		String obdafile = args[1].trim();
		String queryfie = args[2].trim();
		String outputfile = null;

		if (args.length == 4) {
			outputfile = args[3].trim();
		}

		try {
			initQuest(owlfile, obdafile);

			/*
			 * Reading query file:
			 */
			StringBuilder query = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(queryfie));
			String line = null;
			while ((line = reader.readLine()) != null) {
				query.append(line + "\n");
			}

			OWLResultSet result = executeQuery(query.toString());

			OutputStream out = null;
			if (outputfile == null) {
				out = System.out;
			} else {
				out = new FileOutputStream(new File(outputfile));
			}
			printResult(out, result);

		} catch (Exception e) {
			System.out.println("ERROR trying to load the scenario or execute the query. Message: ");
			e.printStackTrace();
		} finally {

			try {
				st.close();
			} catch (Exception e) {
			}

			try {
				conn.close();
			} catch (Exception e) {
			}

			try {
				reasoner.dispose();
			} catch (Exception e) {
			}
		}

	}

	public static void printResult(OutputStream out, OWLResultSet result) throws Exception {
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out,"utf8"));

		/*
		 * Printing the header
		 */

		int columns = result.getColumCount();
		for (int c = 0; c < columns; c++) {
			String value = result.getSignature().get(c);
			wr.append(value);
			if (c + 1 < columns)
				wr.append("\t");
		}
		wr.newLine();
		/*
		 * Printing the header
		 */

		while (result.nextRow()) {
			for (int c = 0; c < columns; c++) {
				String value = result.getOWLObject(c+1).toString();
				wr.append(value);
				if (c + 1 < columns)
					wr.append(",");
			}
			wr.newLine();
		}
		wr.flush();

		result.close();

	}

	public static OWLResultSet executeQuery(String query) throws OWLException {
		return st.executeTuple(query);
	}

	public static void initQuest(String owlfile, String obdafile) throws Exception {
		/*
		 * Loading the OWL ontology from the file as with normal OWLReasoners
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		/*
		 * Loading the OBDA model (database declaration and mappings) from the
		 * .obda file (this code will change in the future)
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		/*
		 * Preparing the configuration for the new Quest instance, we need to
		 * make sure it will be setup for "virtual ABox mode"
		 */
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		/*
		 * Creating the instance of the reasoner using the factory. Remember
		 * that the RDBMS that contains the data must be already running and
		 * accepting connections. The HelloWorld and Books tutorials at our wiki
		 * show you how to do this.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(p);
		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Now we are ready to query. Querying is done as with JDBC. First we
		 * get a connection, from the connection we get a Statement and using
		 * the statement we query.
		 */
		conn = reasoner.getConnection();

		st = conn.createStatement();
	}
}
