package it.unibz.krdb.obda.r2rml;

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
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;

class MappingConverterCMD {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println(" MappingConverterCMD");
			System.out.println("");
			System.out
					.println(" (1) MappingConverterCMD  map.obda [ontology.owl] ");
			System.out.println(" (2) MappingConverterCMD  map.ttl  ");
			System.out
					.println(" map.obda/map.ttl   The full path to the OBDA/R2RML file");
			System.out
					.println(" Given *.obda file, the script will produce *.ttl file and vice versa");
			System.out.println("");
			return;
		}

		String mapFile = args[0].trim();

		try {
			if (mapFile.endsWith(".obda")) {
				String outfile = mapFile.substring(0, mapFile.length() - 5)
						.concat(".ttl");
				File out = new File(outfile);
				URI obdaURI = new File(mapFile).toURI();
				// create model
				OBDAModel model = OBDADataFactoryImpl.getInstance()
						.getOBDAModel();

				// obda mapping
				ModelIOManager modelIO = new ModelIOManager(model);

				try {
					modelIO.load(new File(obdaURI));
				} catch (IOException | InvalidMappingException e) {
					e.printStackTrace();
				}
				URI srcURI = model.getSources().get(0).getSourceID();

				OWLOntology ontology = null;
				if (args.length > 1) {
					String owlfile = args[1].trim();
					// Loading the OWL file
					OWLOntologyManager manager = OWLManager
							.createOWLOntologyManager();
					ontology = manager
							.loadOntologyFromOntologyDocument((new File(owlfile)));
				}

				R2RMLWriter writer = new R2RMLWriter(model, srcURI, ontology);
				// writer.writePretty(out);
				writer.write(out);
				System.out.println("R2RML mapping file " + outfile
						+ " written!");
			} else if (mapFile.endsWith(".ttl")) {
				String outfile = mapFile.substring(0, mapFile.length() - 4)
						.concat(".obda");
				File out = new File(outfile);

				URI obdaURI = new File(mapFile).toURI();
				R2RMLReader reader = new R2RMLReader(mapFile);
				
				String jdbcurl = "jdbc:h2:tcp://localhost/DBName";
				String username = "sa";
				String password = "";
				String driverclass = "com.mysql.jdbc.Driver";

				OBDADataFactory f = OBDADataFactoryImpl.getInstance();
				String sourceUrl =obdaURI.toString();
				OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
						username, password, driverclass);
				OBDAModel model = reader.readModel(dataSource);

				ModelIOManager modelIO = new ModelIOManager(model);
				modelIO.save(out);

				System.out.println("OBDA mapping file " + outfile + " written!");
			}

		} catch (Exception e) {
			System.out.println("Error converting mappings:");
			e.printStackTrace();
		}

	}

}
