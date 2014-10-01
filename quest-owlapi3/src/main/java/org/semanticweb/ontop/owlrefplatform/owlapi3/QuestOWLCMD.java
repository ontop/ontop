package org.semanticweb.ontop.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.SQLOBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class QuestOWLCMD {

	private static QuestOWL reasoner;
	private static QuestOWLConnection conn;
	private static QuestOWLStatement st;

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

			QuestOWLResultSet result = executeQuery(query.toString());

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

	public static void printResult(OutputStream out, QuestOWLResultSet result) throws Exception {
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out,"utf8"));

		/*
		 * Printing the header
		 */

		int columns = result.getColumnCount();
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

	public static QuestOWLResultSet executeQuery(String query) throws OWLException {
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
		SQLOBDAModel obdaModel = fac.getOBDAModel();
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
