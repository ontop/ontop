package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
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


import it.unibz.inf.ontop.si.repository.impl.SemanticIndexBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;

public class S_NewGraphTest  extends TestCase{
	
	Logger log = LoggerFactory.getLogger(S_NewGraphTest.class);

	public void testCreation() throws Exception {
		String roleowlfile = "src/test/resources/test/dag/test-role-hierarchy.owl";
		
		log.info("Loading ontology");

		// Loading the OWL file
		log.info("Translating");
		ClassifiedTBoxImpl r = (ClassifiedTBoxImpl) loadOntologyFromFileAndClassify(roleowlfile);

		log.info("See information");
		log.debug("properties {}", r.getObjectPropertyGraph());
		log.debug("classes {}", r.getClassGraph());

		log.info("From graph to dag");
		log.debug(r.toString());
		
		log.info("See information");
		log.debug(r.classesDAG().toString());
		log.debug(r.objectPropertiesDAG().toString());

		log.info("Get named dag");
		
		log.info("See information named DAG");
		log.debug(r.classesDAG().toString());
		log.debug(r.objectPropertiesDAG().toString());
		log.debug(SemanticIndexBuilder.getNamedDAG(r.classesDAG()).toString());
		log.debug(SemanticIndexBuilder.getNamedDAG(r.objectPropertiesDAG()).toString());
	}
}
