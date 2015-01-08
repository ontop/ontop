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
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlapi3.QuestOWLIndividualIterator;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.OWLAPI3Materializer;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.*;

public class QuestOWLMaterializerCMD {

	private static String owlFile;
	private static String obdaFile;
	private static String format;
	private static String outputFile;

	public static void main(String args[]) {

		if (!parseArgs(args)) {
			printUsage();
			System.exit(1);
		}

		BufferedOutputStream output = null;
		BufferedWriter writer = null;

		try {
			final long startTime = System.currentTimeMillis();
			
			if (outputFile != null) {
				output = new BufferedOutputStream(new FileOutputStream(outputFile)); 
			} else {
				output = new BufferedOutputStream(System.out);
			}
			writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
			
			
			OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
			OBDAModel obdaModel = fac.getOBDAModel();
			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(obdaFile);

			OWLOntology ontology = null;
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLAPI3Materializer materializer = null;
			
			if (owlFile != null) {
			// Loading the OWL ontology from the file as with normal OWLReasoners
				ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
				Ontology onto =  OWLAPI3TranslatorUtility.translate(ontology);
				obdaModel.declareAll(onto.getVocabulary());
				materializer = new OWLAPI3Materializer(obdaModel, onto);
			}
			else {
				ontology = manager.createOntology();
				materializer = new OWLAPI3Materializer(obdaModel);
			}


			// OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);


			QuestOWLIndividualIterator iterator = materializer.getIterator();
	
			while(iterator.hasNext()) 
				manager.addAxiom(ontology, iterator.next());
			
			
			OWLOntologyFormat ontologyFormat = getOntologyFormat(format);
			
			manager.saveOntology(ontology, ontologyFormat, new WriterDocumentTarget(writer));	
			
			System.out.println("NR of TRIPLES: " + materializer.getTriplesCount());
			System.out.println("VOCABULARY SIZE (NR of QUERIES): " + materializer.getVocabularySize());
			
			materializer.disconnect();
			if (outputFile!=null)
				output.close();
			
			final long endTime = System.currentTimeMillis();
			final long time = endTime - startTime;
			System.out.println("Elapsed time to materialize: "+time + " {ms}");
			
		} catch (Exception e) {
			System.out.println("Error materializing ontology:");
			e.printStackTrace();
		} 

	}

	private static OWLOntologyFormat getOntologyFormat(String format) throws Exception {
		OWLOntologyFormat ontoFormat;
		
		if(format == null){
			ontoFormat = new RDFXMLOntologyFormat();
		}
		else {
		switch (format) {
			case "rdfxml":
				ontoFormat = new RDFXMLOntologyFormat();
				break;
			case "owlxml":
				ontoFormat = new OWLXMLOntologyFormat();
				break;
			case "turtle":
				ontoFormat = new TurtleOntologyFormat();
				break;
			default:
				throw new Exception("Unknown format: " + format);
			}
		}
		return ontoFormat;
	}

	private static void printUsage() {
		System.out.println("Usage");
		System.out.println(" QuestOWLMaterializerCMD -obda mapping.obda [-onto ontology.owl] [-format format] [-output outputfile]");
		System.out.println("");
		System.out.println(" -obda mapping.obda    The full path to the OBDA file");
		System.out.println(" -onto ontology.owl    [OPTIONAL] The full path to the OWL file");
		System.out.println(" -format format        [OPTIONAL] The format of the materialized ontology: ");
		System.out.println("                          Options: rdfxml, owlxml, turtle. Default: rdfxml");
		System.out.println(" -output outputfile    [OPTIONAL] The full path to the output file. If not specified, the output will be stdout");
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
			case "-output":
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
