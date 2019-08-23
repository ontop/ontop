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
		String classowlfile = "src/test/resources/dag-tests-1.owl";
		String roleowlfile = "src/test/resources/test/dag/test-role-hierarchy.owl";
		
		String owlfile = "src/test/resources/test/stockexchange-unittest.owl";
		log.info("Loading ontology");

		// Loading the OWL file
		log.info("Translating");
		ClassifiedTBoxImpl r = (ClassifiedTBoxImpl) loadOntologyFromFileAndClassify(roleowlfile);

		log.info("See information");
		log.debug("properties {}", r.getObjectPropertyGraph());
		log.debug("classes {}", r.getClassGraph());
//		
		log.info("From graph to dag");
		System.out.println(r);
		
		log.info("See information");
		System.out.println(r.classesDAG());
		System.out.println(r.objectPropertiesDAG());
		//System.out.println(r.getDAG());
		
//		log.info("See relations");
//		ClassifiedTBoxImpl tbox= new ClassifiedTBoxImpl(dag);
//		for (Description d: dag.vertexSet()){
//		System.out.println("parents "+d+" "+tbox.getDirectParents(d));
//		System.out.println("children "+d+" "+tbox.getDirectChildren(d));
//		
//		log.info("Descendants");
//		System.out.println("descendants "+d+ " "+tbox.getDescendants(d));
//		
//		log.info("Ancestors");
//		System.out.println("ancestors "+d+" "+ tbox.getAncestors(d));
//		}
		log.info("Get named dag");
		
		log.info("See information named DAG");
		System.out.println(r.classesDAG());
		System.out.println(r.objectPropertiesDAG());
		System.out.println(SemanticIndexBuilder.getNamedDAG(r.classesDAG()));
		System.out.println(SemanticIndexBuilder.getNamedDAG(r.objectPropertiesDAG()));
		
//		log.info("See relations named DAG");
//		ClassifiedTBoxImpl tbox2= new ClassifiedTBoxImpl(dag);
//		for (Description d2: dag.vertexSet()){
//		System.out.println("parents "+d2+" "+tbox2.getDirectParents(d2));
//		System.out.println("children "+d2+" "+tbox2.getDirectChildren(d2));
//		
//		log.info("Descendants namedDAG");
//		System.out.println("descendants "+d2+" "+tbox2.getDescendants(d2));
//		
//		log.info("Ancestors namedDAG");
//		System.out.println("ancestors "+d2+" "+ tbox2.getAncestors(d2));
//		
//		}
	}
}
