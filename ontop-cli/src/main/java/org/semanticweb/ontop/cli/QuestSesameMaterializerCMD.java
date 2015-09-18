package org.semanticweb.ontop.cli;

/*
 * #%L
 * ontop-quest-sesame
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

import java.io.*;
import java.net.URI;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlapi3.OWLAPI3TranslatorUtility;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;
import org.semanticweb.ontop.sesame.SesameStatementIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.ontop.sesame.SesameMaterializer;

/**
 * @deprecated Use {@code QuestOWLMaterialzerCMD}  instead
 */

@Deprecated
class QuestSesameMaterializerCMD {

	private static String owlFile;
	private static String obdaFile;
	private static String format;
	private static String outputFile;

	/**
	 * Necessary for materialize large RDF graphs without
	 * storing all the SQL results of one big query in memory.
	 *
	 * TODO: add an option to disable it.
	 */
	private static boolean DO_STREAM_RESULTS = true;

	public static void main(String[] args) {
		// check argument correctness
		if (!parseArgs(args)) {
			printUsage();
			System.exit(1);
		}

		// get parameter values

		Writer writer = null;

        URI obdaURI =  new File(obdaFile).toURI();


		QuestPreferences preferences;
		/**
		 * R2RML case
		 */
        if (obdaURI.toString().endsWith(".ttl")) {
			preferences = new R2RMLQuestPreferences();
        }
		/**
		 * Default case: Ontop Native Mapping Language
		 */
		else {
			preferences = new QuestPreferences();
		}

        Injector injector = Guice.createInjector(new OBDACoreModule(preferences));
        NativeQueryLanguageComponentFactory nativeQLFactory = injector.getInstance(
                NativeQueryLanguageComponentFactory.class);
		
		try {
			final long startTime = System.currentTimeMillis();
			if (outputFile != null) {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile)), "UTF-8"));
			} else {
				writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
			}

			//create model
            MappingParser mappingParser = nativeQLFactory.create(new FileReader(obdaURI.toString()));
            OBDAModel model = mappingParser.getOBDAModel();
			
			//create onto
			Ontology onto = null;
			OWLOntology ontology;
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			if (owlFile != null) {
			// Loading the OWL ontology from the file as with normal OWLReasoners
				ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
				 onto =  OWLAPI3TranslatorUtility.translate(ontology);
				 model.declareAll(onto.getVocabulary());
			}
			else {
				ontology = manager.createOntology();
			}

			 //start materializer
			SesameMaterializer materializer = new SesameMaterializer(model, onto, DO_STREAM_RESULTS);
			SesameStatementIterator iterator = materializer.getIterator();
			RDFHandler handler;

			switch (format.toLowerCase()) {
				case "n3":
					handler = new N3Writer(writer);
					break;
				case "turtle":
					handler = new TurtleWriter(writer);
					break;
				case "rdfxml":
					handler = new RDFXMLWriter(writer);
					break;
				default:
					throw new IllegalArgumentException("Unsupported output format: " + format);
			}

			handler.startRDF();
			while(iterator.hasNext())
				handler.handleStatement(iterator.next());
			handler.endRDF();
			
			System.out.println("NR of TRIPLES: "+materializer.getTriplesCount());
			System.out.println( "VOCABULARY SIZE (NR of QUERIES): "+materializer.getVocabularySize());
			
			materializer.disconnect();

			writer.close();
			
			final long endTime = System.currentTimeMillis();
			final long time = endTime - startTime;
			System.out.println("Elapsed time to materialize: "+time + " {ms}");
				
			
		} catch (Exception e) {
			System.out.println("Error materializing ontology:");
			e.printStackTrace();
		} 

	}

	private static void printUsage() {
		System.out.println("Usage");
		System.out.println(" QuestSesameMaterializerCMD -obda mapping.obda [-onto ontology.owl] [-format format] [-out outputFile] [--enable-reasoning | --disable-reasoning]");
		System.out.println("");
		System.out.println(" -obda mapping.obda    The full path to the OBDA file");
		System.out.println(" -onto ontology.owl    [OPTIONAL] The full path to the OWL file");
		System.out.println(" -format format        [OPTIONAL] The format of the materialized ontology: ");
		System.out.println("                          Options: rdfxml, n3, turtle. Default: rdfxml");
		System.out.println(" -out outputFile    [OPTIONAL] The full path to the output file. If not specified, the output will be stdout");
		System.out.println(" --enable-reasoning    [OPTIONAL] enable the OWL reasoning (default)");
		System.out.println(" --disable-reasoning   [OPTIONAL] disable the OWL reasoning (not implemented yet) ");
		System.out.println("");
	}


	public static boolean parseArgs(String[] args) {
		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
				case "-obda":
					obdaFile = args[i + 1];
					i += 2;
					break;
				case "-onto":
					owlFile = args[i + 1];
					i += 2;
					break;
				case "-format":
					format = args[i + 1];
					i += 2;
					break;
				case "-out":
					outputFile = args[i + 1];
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
