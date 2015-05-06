package org.semanticweb.ontop.cli;

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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class OntopQueryCMD {

    private static String owlFile;
    private static String obdaFile;
    private static String format;
    private static String outputFile;
    private static String sparqlFile;

    private static QuestOWL reasoner;
	private static QuestOWLConnection conn;
	private static QuestOWLStatement st;


    public static void main(String args[]) {

        if (!parseArgs(args)) {
            printUsage();
            System.exit(1);
        }

		try {
			initQuest(owlFile, obdaFile);

			/*
			 * Reading query file:
			 */
			StringBuilder query = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(sparqlFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				query.append(line + "\n");
			}

			QuestOWLResultSet result = executeQuery(query.toString());

			OutputStream out = null;
			if (outputFile == null) {
				out = System.out;
			} else {
				out = new FileOutputStream(new File(outputFile));
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
				wr.append(",");
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
		reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Now we are ready to query. Querying is done as with JDBC. First we
		 * get a connection, from the connection we get a Statement and using
		 * the statement we query.
		 */
		conn = reasoner.getConnection();

		st = conn.createStatement();
	}

    private static void printUsage() {
        System.out.println("Usage");
        System.out.println(" ontop-query -obda mapping.obda [-onto ontology.owl] [-out outputFile] -query sparql.rq");
        System.out.println("");
        System.out.println(" -obda mapping.obda    The full path to the OBDA file");
        System.out.println(" -onto ontology.owl    [OPTIONAL] The full path to the OWL file");
        System.out.println(" -out outputFile       [OPTIONAL] The full path to the output file. If not specified, the output will be stdout");
        System.out.println(" -sparql sparql.rq     The full path to the SPARQL file");
        System.out.println("");
    }



    private static boolean parseArgs(String[] args) {
        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-obda":
                case "--obda":
                    obdaFile = args[i + 1];
                    i += 2;
                    break;
                case "-onto":
                case "--onto":
                    owlFile = args[i + 1];
                    i += 2;
                    break;
                case "-format":
                case "--format":
                    format = args[i + 1];
                    i += 2;
                    break;
                case "-out":
                case "--out":
                    outputFile = args[i + 1];
                    i += 2;
                    break;
                case "-q":
                case "--query":
                    sparqlFile = args[i + 1];
                    i += 2;
                    break;
                default:
                    System.err.println("Unknown option " + args[i]);
                    System.err.println();
                    return false;
            }
        }

        if (obdaFile == null) {
            System.err.println("Please specify the ontology file\n");
            return false;
        }

        return true;
    }

}
