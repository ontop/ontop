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
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.sesame.SesameStatementIterator;
import it.unibz.krdb.obda.sesame.r2rml.R2RMLReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.rio.RDFHandler;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class MappingConverterCMD {

	public static void main(String[] args) {
		if (args.length != 1 ) {
			System.out.println("Usage:");
			System.out.println(" MappingConverterCMD mappingfile");
			System.out.println("");
			System.out.println(" mappingfile   The full path to the OBDA/R2RML file");
			System.out.println(" Given *.obda file, the script will produce *.ttl file and vice versa");
			System.out.println("");
			return;
		}

		String mapfile = args[0].trim();

		try {
			if (mapfile.endsWith(".obda"))
			{
				String outfile = mapfile.substring(0, mapfile.length() - 5).concat(".ttl");
				File out = new File(outfile);
				URI obdaURI =  new File(mapfile).toURI();
				//create model
				OBDAModel model = OBDADataFactoryImpl.getInstance().getOBDAModel();
				
				//obda mapping
				ModelIOManager modelIO = new ModelIOManager(model);
				
				try {
					modelIO.load(new File(obdaURI));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InvalidMappingException e) {
					e.printStackTrace();
				}
				URI srcURI = model.getSources().get(0).getSourceID();
				R2RMLWriter writer = new R2RMLWriter(model, srcURI);
				//writer.writePretty(out);
				writer.write(out);
				System.out.println("R2RML mapping file " + outfile + " written!");
			}
			else if (mapfile.toString().endsWith(".ttl"))
			{
				String outfile = mapfile.substring(0, mapfile.length() - 4).concat(".obda");
				File out = new File(outfile);
				R2RMLReader reader = new R2RMLReader(mapfile);
				ArrayList<OBDAMappingAxiom> axioms = reader.readMappings();
				for (OBDAMappingAxiom ax : axioms)
					//ax.getId()
					System.out.println(ax);

			}


		} catch (Exception e) {
			System.out.println("Error converting mappings:");
			e.printStackTrace();
		} 

	}

}
