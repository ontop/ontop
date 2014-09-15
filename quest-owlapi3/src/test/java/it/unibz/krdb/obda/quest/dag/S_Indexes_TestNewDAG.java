package it.unibz.krdb.obda.quest.dag;

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



import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.TestCase;

import org.jgrapht.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_Indexes_TestNewDAG extends TestCase {
	
	ArrayList<String> input= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_Indexes_TestNewDAG (String name){
		super(name);
	}
	
public void setUp(){
		
	
		/** C -> B  -> A  C->A*/
		input.add("src/test/resources/test/newDag/transitive.owl");
		/** C -> B  -> A  C->D ->A C->A */
		input.add("src/test/resources/test/newDag/transitive2.owl");

		/** C = B -> ER -> A*/
		input.add("src/test/resources/test/newDag/equivalents1.owl");
		/** B -> A -> ER=C */
		input.add("src/test/resources/test/newDag/equivalents2.owl");
		/** C->B = ER -> A*/
		input.add("src/test/resources/test/newDag/equivalents3.owl");
		/** ER-> A=B=C */
		input.add("src/test/resources/test/newDag/equivalents4.owl");
		/** C=ER=A->B */
		input.add("src/test/resources/test/newDag/equivalents5.owl");
		/** D-> ER=C=B -> A*/
		input.add("src/test/resources/test/newDag/equivalents6.owl");
		/** P-> ER=B -> A  C=L ->ES-> ER */
		input.add("src/test/resources/test/newDag/equivalents7.owl");
		/** B->A=ET->ER C->ES=D->A*/
		input.add("src/test/resources/test/newDag/equivalents8.owl");

		/** C = B -> ER- -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents1.owl");
		/** B -> A -> ER- = C */
		input.add("src/test/resources/test/newDag/inverseEquivalents2.owl");
		/** C->B = ER- -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents3.owl");
		/** ER- -> A=B=C */
		input.add("src/test/resources/test/newDag/inverseEquivalents4.owl");
		/** C=ER- =A->B */
		input.add("src/test/resources/test/newDag/inverseEquivalents5.owl");
		/** D-> ER- =C=B -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents6.owl");
		/** D->  ER- =C=B -> A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents6b.owl");
		/** P-> ER- =B -> A  C=L ->ES- -> ER- */
		input.add("src/test/resources/test/newDag/inverseEquivalents7.owl");
		/** B->A=ET- ->ER- C->ES- = D->A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents8.owl");

}

public void testIndexes() throws Exception{
	//for each file in the input
	for (int i=0; i<input.size(); i++){
		String fileInput=input.get(i);

		TBoxReasoner dag= new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));


		//add input named graph
		SemanticIndexBuilder engine= new SemanticIndexBuilder(dag);

		
		log.debug("Input number {}", i+1 );
		log.info("named graph {}", engine);
		
		
		assertTrue(testIndexes(engine, engine.getNamedDAG()));


	}
}

private boolean testIndexes(SemanticIndexBuilder engine, NamedDAG namedDAG){
	boolean result=true;
		
	//create semantic index
	//check that the index of the node is contained in the intervals of the parent node
	for(Description vertex: engine.getIndexed() /*.getNamedDAG().vertexSet()*/){
		int index= engine.getIndex(vertex);
		log.info("vertex {} index {}", vertex, index);
		if (vertex instanceof Property) {
			for(Description parent: namedDAG.getSuccessors((Property)vertex)){
				result = engine.getRange(parent).contained(new SemanticIndexRange(index,index));
				if(!result)
					break;
			}
		}
		else {
			for(Description parent: namedDAG.getSuccessors((BasicClassDescription)vertex)){
				result = engine.getRange(parent).contained(new SemanticIndexRange(index,index));
				if(!result)
					break;
			}			
		}
		
	}
	
	//log.info("ranges {}", ranges);
	
	
	return result;
	

}






}
