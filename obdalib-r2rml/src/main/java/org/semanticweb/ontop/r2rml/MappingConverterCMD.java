package org.semanticweb.ontop.r2rml;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.exception.InvalidMappingExceptionWithIndicator;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.OntopNativeMappingSerializer;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
                Injector injector = Guice.createInjector(new OBDACoreModule(
                        new OBDAProperties()));
                NativeQueryLanguageComponentFactory factory =
                        injector.getInstance(NativeQueryLanguageComponentFactory.class);

				String outfile = mapFile.substring(0, mapFile.length() - 5)
						.concat(".ttl");
				File out = new File(outfile);
				URI obdaURI = new File(mapFile).toURI();
				// create model

                OBDAModel model;
                try {
                    MappingParser mappingParser = factory.create(new File(obdaURI));
                    model = mappingParser.getOBDAModel();
				} catch (InvalidMappingExceptionWithIndicator e) {
					e.printStackTrace();
                    return;
				}
				URI srcURI = model.getSources().iterator().next().getSourceID();

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

                Properties p = new Properties();
                p.setProperty(OBDAProperties.DB_NAME, "DBName");
                p.setProperty(OBDAProperties.JDBC_URL, "jdbc:h2:tcp://localhost/DBName");;
                p.setProperty(OBDAProperties.DB_USER, "sa");
                p.setProperty(OBDAProperties.DB_PASSWORD, "");
                p.setProperty(OBDAProperties.JDBC_DRIVER, "com.mysql.jdbc.Driver");
				OBDAProperties pref = new OBDAProperties(p);

                Injector injector = Guice.createInjector(new OBDACoreModule(pref));
                NativeQueryLanguageComponentFactory factory =
                        injector.getInstance(NativeQueryLanguageComponentFactory.class);

				String outfile = mapFile.substring(0, mapFile.length() - 4)
						.concat(".obda");
				File out = new File(outfile);

				URI obdaURI = new File(mapFile).toURI();
                MappingParser mappingParser = factory.create(new File(obdaURI));
                OBDAModel model = mappingParser.getOBDAModel();

                OntopNativeMappingSerializer mappingWriter = new OntopNativeMappingSerializer(model);
				mappingWriter.save(out);
				
				/*Add the not standard prefixes to prefix manager.
				 * If you want them to have the abbreviation in the obda file
				 PrefixManager pm = model.getPrefixManager();
				 pm.addPrefix(":", "http://example.org/");
				 
				*/ 
				 
				 
				 /* Deprecated way
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
				 writer.write("connectionUrl" + "\t" +
				 "jdbc:h2:tcp://localhost/DBName");
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
				 	*/ 
				
				System.out
						.println("OBDA mapping file " + outfile + " written!");
			}

		} catch (Exception e) {
			System.out.println("Error converting mappings:");
			e.printStackTrace();
		}

	}

}
