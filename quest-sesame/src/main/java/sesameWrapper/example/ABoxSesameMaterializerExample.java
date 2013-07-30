/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package sesameWrapper.example;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;

import sesameWrapper.SesameMaterializer;
import sesameWrapper.SesameStatementIterator;

public class ABoxSesameMaterializerExample {

	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
	 */
	final String inputFile = "src/main/resources/example/exampleBooks.obda";
	final String outputFile = "src/main/resources/example/exampleBooks.n3";
	
	public void generateTriples() throws Exception {

		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(inputFile);

		/*
		 * Start materializing data from database to triples.
		 */

		SesameMaterializer materializer = new SesameMaterializer(obdaModel);
		
		long numberOfTriples = materializer.getTriplesCount();
		System.out.println("Generated triples: " + numberOfTriples);

		/*
		 * Obtain the triples iterator
		 */
		SesameStatementIterator triplesIter = materializer.getIterator();
		
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
