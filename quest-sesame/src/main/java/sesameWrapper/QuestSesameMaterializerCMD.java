package sesameWrapper;

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

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.r2rml.R2RMLReader;
import it.unibz.krdb.obda.sesame.SesameStatementIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class QuestSesameMaterializerCMD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// check argument correctness
		if (args.length != 3 && args.length != 4) {
			System.out.println("Usage:");
			System.out.println(" QuestSesameMaterializerCMD obdafile owlfile->yes/no format [outputfile]");
			System.out.println("");
			System.out.println(" obdafile   The full path to the OBDA file");
			System.out.println(" owlfile    yes/no to use the OWL file or not");
			System.out.println(" format     The desired output format: N3/Turtle/RDFXML");
			System.out.println(" outputfile [OPTIONAL] The full path to output file");
			System.out.println("");
			return;
		}

		// get parameter values
		String obdafile = args[0].trim();
		String yesno = args[1].trim();
		String owlfile = null;
		if (yesno.toLowerCase().equals("yes"))
			owlfile = obdafile.substring(0, obdafile.length()-4) + "owl";
		String format = args[2].trim();
		String out = null;
		if (args.length == 4)
			out = args[3].trim();
		Writer writer = null;
		
		
		try {
			final long startTime = System.currentTimeMillis();
			if (out != null) {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(out)), "UTF-8")); 
			} else {
				writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
			}
			
			URI obdaURI =  new File(obdafile).toURI();
			//create model
			OBDAModel model = OBDADataFactoryImpl.getInstance().getOBDAModel();
			//obda mapping
			if (obdaURI.toString().endsWith(".obda"))
			{
					ModelIOManager modelIO = new ModelIOManager(model);
					try {
						modelIO.load(new File(obdaURI));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InvalidMappingException e) {
						e.printStackTrace();
					}
			}//r2rml mapping
			else if (obdaURI.toString().endsWith(".ttl"))
			{
				R2RMLReader reader = new R2RMLReader(new File(obdaURI));
				model = reader.readModel(obdaURI);
			}
			
			//create onto
			Ontology onto = null;
			OWLOntology ontology = null;
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			if (owlfile != null) {
			// Loading the OWL ontology from the file as with normal OWLReasoners
				ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
				 onto =  OWLAPI3TranslatorUtility.translate(ontology);
				 model.declareAll(onto.getVocabulary());
			}
			else {
				ontology = manager.createOntology();
			}
			//OBDAModelSynchronizer.declarePredicates(ontology, model);

			 //start materializer
			SesameMaterializer materializer = new SesameMaterializer(model, onto);
			SesameStatementIterator iterator = materializer.getIterator();
			RDFHandler handler = null;
			
			if (format.equals("N3"))
			{
				handler = new N3Writer(writer);
			}
			else if (format.equals("Turtle"))
			{
				handler = new TurtleWriter(writer);
			}
			else if (format.equals("RDFXML"))
			{
				handler = new RDFXMLWriter(writer);
			}

			handler.startRDF();
			while(iterator.hasNext())
				handler.handleStatement(iterator.next());
			handler.endRDF();
			
			System.out.println("NR of TRIPLES: "+materializer.getTriplesCount());
			System.out.println( "VOCABULARY SIZE (NR of QUERIES): "+materializer.getVocabularySize());
			
			materializer.disconnect();
			if (out!=null)
				writer.close();
			
			final long endTime = System.currentTimeMillis();
			final long time = endTime - startTime;
			System.out.println("Elapsed time to materialize: "+time + " {ms}");
				
			
		} catch (Exception e) {
			System.out.println("Error materializing ontology:");
			e.printStackTrace();
		} 

	}

}
