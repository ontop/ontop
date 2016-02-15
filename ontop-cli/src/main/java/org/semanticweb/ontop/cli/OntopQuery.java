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


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;

@Command(name = "query",
        description = "Query the RDF graph exposed by the mapping and the OWL ontology")
public class OntopQuery extends OntopReasoningCommandBase {

    @Option(type = OptionType.COMMAND, name = {"-q", "--query"}, title = "queryFile",
            description = "SPARQL query file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
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

//        QuestOWLFactory factory = null;
//        try {
//            factory = createQueryOWLFactory(mappingFile);
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }

        QuestOWLFactory factory = null;
        QuestOWLConfiguration config = null;
        try {
            OBDAModel obdaModel = loadMappingFile(mappingFile);
            config = QuestOWLConfiguration.builder().obdaModel(obdaModel).build();
            factory = new QuestOWLFactory();
        } catch (IOException | InvalidPredicateDeclarationException | InvalidMappingException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try (QuestOWL reasoner = factory.createReasoner(ontology, config);
             QuestOWLConnection conn = reasoner.getConnection();
             QuestOWLStatement st = conn.createStatement();
        ) {

			/*
             * Reading query file:
			 */
//            String query = Joiner.on("\n").
//                    join(Files.readAllLines(Paths.get(queryFile), StandardCharsets.UTF_8));

            String query = Files.lines(Paths.get(queryFile), StandardCharsets.UTF_8).collect(joining("\n"));

            QuestOWLResultSet result = st.executeTuple(query);

            OutputStream out = null;
            if (outputFile == null) {
                out = System.out;
            } else {
                out = new FileOutputStream(new File(outputFile));
            }
            printResult(out, result);


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

        while (result.nextRow()) {
            for (int c = 0; c < columns; c++) {
                String value = ToStringRenderer.getInstance().getRendering(result.getOWLObject(c + 1));
                wr.append(value);
                if (c + 1 < columns)
                    wr.append(",");
            }
            wr.newLine();
        }
        wr.flush();

        result.close();
    }


}
