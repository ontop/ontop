package org.semanticweb.ontop.reformulation.semindex.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;

import java.io.File;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNOMEDTest {
	
	
	// TODO: check with mariano
	public static void main(String args[]) throws Exception {
		
		Logger log = LoggerFactory.getLogger("SNOMEDTEST");

		String owlfile = "/Users/mariano/Downloads/SnomedCT_INT_20110731/res_StatedOWLF_Core_INT_20110731.owl";
		
		log.info("Loading SNOMED");

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		QuestPreferences preferences = new QuestPreferences(p);

		/*
		 * Loading the queries (we have 11 queries)
		 */

		// Creating a new instance of the reasoner
		QuestOWLFactory questOWLFactory = new QuestOWLFactory(preferences);

		log.info("Creating the reasoner");
		
		QuestOWL reasoner =  questOWLFactory.createReasoner(ontology, new SimpleConfiguration());
		
		log.info("Done");
		
		reasoner.dispose();
	}
}
