package it.unibz.inf.ontop.r2rml;

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

import it.unibz.inf.ontop.exception.InvalidMappingExceptionWithIndicator;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.io.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.model.OBDAModel;

import java.io.File;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.*;

@Deprecated
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
				OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
						.properties(generateProperties())
						.nativeOntopMappingFile(mapFile)
						.build();

				String outfile = mapFile.substring(0, mapFile.length() - 5)
						.concat(".ttl");
				File out = new File(outfile);
				// create model

                OBDAModel model;
                try {
                    model = configuration.loadProvidedPPMapping();
				} catch (InvalidMappingExceptionWithIndicator e) {
					e.printStackTrace();
                    return;
				}

				OWLOntology ontology = null;
				if (args.length > 1) {
					String owlfile = args[1].trim();
					// Loading the OWL file
					OWLOntologyManager manager = OWLManager
							.createOWLOntologyManager();
					ontology = manager
							.loadOntologyFromOntologyDocument((new File(owlfile)));
				}

				OBDAModelToR2RMLConverter writer = new OBDAModelToR2RMLConverter(model, ontology, configuration.getInjector().getInstance(
						NativeQueryLanguageComponentFactory.class));
				// writer.writePretty(out);
				writer.write(out);
				System.out.println("R2RML mapping file " + outfile
						+ " written!");
			} else if (mapFile.endsWith(".ttl")) {

				OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
						.properties(generateProperties())
						.r2rmlMappingFile(mapFile)
						.build();

				String outfile = mapFile.substring(0, mapFile.length() - 4)
						.concat(".obda");
				File out = new File(outfile);

                OBDAModel model = configuration.loadProvidedPPMapping();

                OntopNativeMappingSerializer mappingWriter = new OntopNativeMappingSerializer(model);
				mappingWriter.save(out);
				
				System.out
						.println("OBDA mapping file " + outfile + " written!");
			}

		} catch (Exception e) {
			System.out.println("Error converting mappings:");
			e.printStackTrace();
		}

	}

	private static Properties generateProperties() {
		Properties p = new Properties();
		p.setProperty(JDBC_NAME, "DBName");
		p.setProperty(JDBC_URL, "jdbc:h2:tcp://localhost/DBName");
		p.setProperty(JDBC_USER, "sa");
		p.setProperty(JDBC_PASSWORD, "");
		p.setProperty(JDBC_DRIVER, "com.mysql.jdbc.Driver");
		return p;
	}

}
