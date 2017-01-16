package it.unibz.inf.ontop.rdf4j.tests.general;

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

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.rdf4j.SesameMaterializer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.n3.N3Writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

public class ABoxSesameMaterializerExample {

	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
	 */
	private static final String inputFile = "src/test/resources/example/exampleBooks.obda";
	private static final String PROPERTY_FILE = "src/test/resources/example/exampleBooks.properties";
	private static final String outputFile = "src/test/resources/example/exampleBooks.n3";

	/**
	 * TODO: try with result streaming
	 */
	private static boolean DO_STREAM_RESULTS = false;
	
	public void generateTriples() throws Exception {

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile(inputFile)
				.propertyFile(PROPERTY_FILE)
				.build();

		SesameMaterializer materializer = new SesameMaterializer(configuration, DO_STREAM_RESULTS);
		
		long numberOfTriples = materializer.getTriplesCount();
		System.out.println("Generated triples: " + numberOfTriples);

		/*
		 * Obtain the triples iterator
		 */
		Iterator<Statement> triplesIter = materializer.getIterator();
		
		/*
		 * Print the triples into an external file.
		 */
		File fout = new File(outputFile);
		if (fout.exists()) {
			fout.delete(); // clean any existing output file.
		}
		
		BufferedWriter out = null;
		try {
		    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout, true)));
		    RDFWriter writer = new N3Writer(out);
		    writer.startRDF();
			while (triplesIter.hasNext()) {
				Statement stmt = triplesIter.next();
				writer.handleStatement(stmt);
			}
			writer.endRDF();
		} finally {
		    if (out != null) {
		    	out.close();
		    }
		    materializer.disconnect();
		}
	}

	public static void main(String[] args) {
		try {
			ABoxSesameMaterializerExample example = new ABoxSesameMaterializerExample();
			example.generateTriples();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
