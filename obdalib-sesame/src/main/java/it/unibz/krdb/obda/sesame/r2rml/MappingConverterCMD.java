package it.unibz.krdb.obda.sesame.r2rml;

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
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.TurtleFormatter;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class MappingConverterCMD {

	public static void main(String[] args) {
		if (args.length < 1 ) {
			System.out.println("Usage:");
			System.out.println(" MappingConverterCMD mappingFile");
			System.out.println("");
			System.out.println(" mappingFile   The full path to the OBDA/R2RML file");
			System.out.println(" Given *.obda file, the script will produce *.ttl file and vice versa");
			System.out.println(" ontologyFilr 	Given *.ttl file, you should provide the .owl file");
			System.out.println("");
			return;
		}

		String mapFile = args[0].trim();

		try {
			if (mapFile.endsWith(".obda")) {
				String outfile = mapFile.substring(0, mapFile.length() - 5).concat(".ttl");
				File out = new File(outfile); 
				URI obdaURI =  new File(mapFile).toURI();
				//create model
				OBDAModel model = OBDADataFactoryImpl.getInstance().getOBDAModel();
				
				//obda mapping
				ModelIOManager modelIO = new ModelIOManager(model);
				
				try {
					modelIO.load(new File(obdaURI));
				} catch (IOException | InvalidMappingException e) {
					e.printStackTrace();
				}
                URI srcURI = model.getSources().get(0).getSourceID();
                
                String owlfile = args[1].trim();
             // Loading the OWL file
        		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
        		
				R2RMLWriter writer = new R2RMLWriter(model, srcURI, ontology);
//				writer.writePretty(out);
				writer.write(out);
				System.out.println("R2RML mapping file " + outfile + " written!");
			}
			else if (mapFile.endsWith(".ttl")) {
				String outfile = mapFile.substring(0, mapFile.length() - 4).concat(".obda");
				//File out = new File(outfile);
				
				URI obdaURI =  new File(mapFile).toURI();
				R2RMLReader reader = new R2RMLReader(mapFile);
				OBDAModel model = reader.readModel(obdaURI);
				PrefixManager pm = model.getPrefixManager();
				
				TurtleFormatter tf = new TurtleFormatter(pm);
				ArrayList<OBDAMappingAxiom> axioms = reader.readMappings();
				BufferedWriter writer = null;
				writer = new BufferedWriter(new FileWriter(outfile));
				writer.write("[PrefixDeclaration]");
				writer.newLine();
				Map<String, String> map = pm.getPrefixMap();
				for (String key : map.keySet()) {
					writer.write(key + "\t" + map.get(key));
					writer.newLine();
				}
				writer.newLine();
				writer.write("[SourceDeclaration]");
				writer.newLine();
				writer.write("sourceUri" + "\t" + "customSource");
				writer.newLine();
				writer.write("connectionUrl" + "\t" + "jdbc:h2:tcp://localhost/DBName");
				writer.newLine();
				writer.write("username" + "\t" + "sa");
				writer.newLine();
				writer.write("password"+ "\t");
				writer.newLine();
				writer.write("driverClass" + "\t" + "org.h2.Driver");
				writer.newLine();
				writer.newLine();
				writer.write("[MappingDeclaration] @collection [[");
				writer.newLine();
				
				for (OBDAMappingAxiom ax : axioms) {
					writer.write("mappingId" + "\t" + ax.getId());
					writer.newLine();
					CQIE cq = (CQIE) ax.getTargetQuery();
					String cqStr = tf.print(cq);
					writer.write("target" + "\t" + cqStr);
					writer.newLine();
					writer.write("source" + "\t" + ax.getSourceQuery());
					writer.newLine();
					writer.newLine();
				}
				writer.write("]]");
				writer.close();
				System.out.println("OBDA mapping file " + outfile + " written!");
			}
			

		} catch (Exception e) {
			System.out.println("Error converting mappings:");
			e.printStackTrace();
		} 

	}

}
