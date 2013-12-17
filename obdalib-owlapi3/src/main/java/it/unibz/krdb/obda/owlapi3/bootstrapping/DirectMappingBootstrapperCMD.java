package it.unibz.krdb.obda.owlapi3.bootstrapping;

/*
 * #%L
 * ontop-obdalib-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
import it.unibz.krdb.obda.model.OBDAModel;

import java.io.File;
import java.net.URI;

import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

public class DirectMappingBootstrapperCMD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// check argument correctness
		if (args.length != 6) {
			System.out.println("Usage:");
			System.out
					.println(" DirectMappingBootstrapperCMD base_uri jdbc_url username password driver owlfile");
			System.out.println("");
			System.out
					.println(" base_uri    The base uri of the generated onto");
			System.out.println(" jdbc_url    The jdbc url path");
			System.out.println(" username    The database username");
			System.out.println(" password    The database password");
			System.out.println(" driver      The jdbc driver class name");
			System.out
					.println(" owlfile  The full path to the owl output file");
			System.out.println("");
			return;
		}

		// get parameter values
		String uri = args[0].trim();
		String url = args[1].trim();
		String user = args[2].trim();
		String passw = args[3].trim();
		String driver = args[4].trim();
		String owlfile = null;
		String obdafile = null;
		if (args.length == 6) {
			owlfile = args[5].trim();
			if (owlfile.endsWith(".owl"))
				obdafile = owlfile.substring(0, owlfile.length() - 4) + ".obda";
		}
		try {
			if (uri.contains("#")) {
				System.out
						.println("Base uri cannot contain the character '#'!");
			} else {
				if (owlfile != null) {
					File owl = new File(owlfile);
					File obda = new File(obdafile);
					DirectMappingBootstrapper dm = new DirectMappingBootstrapper(
							uri, url, user, passw, driver);
					OBDAModel model = dm.getModel();
					OWLOntology onto = dm.getOntology();
					ModelIOManager mng = new ModelIOManager(model);
					mng.save(obda);
					onto.getOWLOntologyManager().saveOntology(onto,
							new FileDocumentTarget(owl));
				} else {
					System.out.println("Output file not found!");
				}
			}
		} catch (Exception e) {
			System.out.println("Error occured during bootstrapping: "
					+ e.getMessage());
			System.out.println("Debugging information for developers: ");
			e.printStackTrace();
		}

	}

}
