package org.semanticweb.ontop.sesame;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.Rio;

public class QuestSesameCMD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Repository repo = null;
		org.openrdf.repository.RepositoryConnection conn = null;
		// check argument correctness
		if (args.length != 3 && args.length != 4) {
			System.out.println("Usage:");
			System.out.println(" QuestSesameCMD owlfile obdafile queryfile [outputfile]");
			System.out.println("");
			System.out.println(" owlfile    The full path to the OWL file");
			System.out.println(" obdafile   The full path to the OBDA file");
			System.out.println(" queryfile  The full path to the file with the SPARQL query");
			System.out.println(" outputfile [OPTIONAL] The full path to output file");
			System.out.println("");
			return;
		}

		// get parameter values
		String owlfile = args[0].trim();
		String obdafile = args[1].trim();
		String qfile = args[2].trim();
		String out = null;
		if (args.length == 4)
			out = args[3].trim();

		try {
			// create and initialize repo
			repo = new SesameVirtualRepo("test_repo", owlfile, obdafile, false, "TreeWitness");
			repo.initialize();
			conn = repo.getConnection();
			String querystr = "";
			// read query from file
			FileInputStream input = new FileInputStream(new File(qfile));
			byte[] fileData = new byte[input.available()];

			input.read(fileData);
			input.close();

			querystr = new String(fileData, "UTF-8");

			
			// execute query
			Query query = conn.prepareQuery(QueryLanguage.SPARQL, querystr);

			if (query instanceof TupleQuery) {
				TupleQuery tuplequery = (TupleQuery) query;
				TupleQueryResultHandler handler = null;

				// set handler to output file or printout
				if (out != null) {

					FileOutputStream output = new FileOutputStream(new File(out));
					handler = new SPARQLResultsTSVWriter(output);

				} else {

					handler = new SPARQLResultsTSVWriter(System.out);
				}
				// evaluate the query
				tuplequery.evaluate(handler);
			} else if (query instanceof GraphQuery) {
				GraphQuery tuplequery = (GraphQuery) query;
				Writer writer = null;
				// set handler to output file or printout
				if (out != null) {
					
					writer = new BufferedWriter(new FileWriter(new File(out))); 
					

				} else {

					writer = new BufferedWriter(new OutputStreamWriter(System.out));
					
				}
				

				// evaluate the query
				RDFHandler handler = Rio.createWriter(RDFFormat.TURTLE, writer);
//				conn.exportStatements(ValueFactoryImpl.getInstance().createURI("http://meraka/moss/exampleBooks.owl#author/내용/"), null, null, true, handler, null);

				
				tuplequery.evaluate(handler);
				
				
			} else {
				System.out.println("Boolean queries are not supported in this script yet.");
			}

		} catch (Exception e) {
			System.out.println("Error executing query:");
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (Exception e1) {

			}

			try {
				repo.shutDown();
			} catch (Exception e1) {

			}
		}

	}

}
