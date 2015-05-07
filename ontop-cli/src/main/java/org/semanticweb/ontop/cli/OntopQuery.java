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

import com.google.common.base.Joiner;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

@Command(name = "query",
        description = "query the RDF graph exposed by the mapping and the OWL ontology")
public class OntopQuery extends OntopReasoningCommandBase {

    @Option(type = OptionType.COMMAND, name = {"-q", "--query"}, title = "queryFile",
            description = "SPARQL query file")
    private String queryFile;


    public OntopQuery() {
    }

    @Override
    public void run() {


        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;

        try {
            if (owlFile != null) {
                // Loading the OWL ontology from the file as with normal OWLReasoners
                ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

                if (disableReasoning) {
                /*
                 * when reasoning is disabled, we extract only the declaration assertions for the vocabulary
                 */
                    ontology = extractDeclarations(manager, ontology);
                }

            } else {
                ontology = manager.createOntology();
            }
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return;
        }

        QuestOWLFactory factory = null;
        try {
            factory = createQueryOWLFactory(mappingFile);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try (
                QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());
                QuestOWLConnection conn = reasoner.getConnection();
                QuestOWLStatement st = conn.createStatement();
        ) {

			/*
             * Reading query file:
			 */
            String query = Joiner.on("\n").
                    join(Files.readAllLines(Paths.get(queryFile), StandardCharsets.UTF_8));

            QuestOWLResultSet result = st.executeTuple(query);

            OutputStream out = null;
            if (outputFile == null) {
                out = System.out;
            } else {
                out = new FileOutputStream(new File(outputFile));
            }
            printResult(out, result);


        } catch (OBDAException e1) {
            e1.printStackTrace();
        } catch (OWLException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();

        }
    }

    public static void printResult(OutputStream out, QuestOWLResultSet result) throws Exception {
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out, "utf8"));

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
                String value = result.getOWLObject(c + 1).toString();
                wr.append(value);
                if (c + 1 < columns)
                    wr.append(",");
            }
            wr.newLine();
        }
        wr.flush();

        result.close();

    }


    public QuestOWLFactory createQueryOWLFactory(String mappingFile) throws Exception {

        OBDAModel obdaModel = loadMappingFile(mappingFile);

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

        return factory;
    }
}
