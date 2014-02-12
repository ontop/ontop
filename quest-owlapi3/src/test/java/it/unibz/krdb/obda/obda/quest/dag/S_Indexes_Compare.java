package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGOperations;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.TestCase;

import org.jgrapht.Graphs;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_Indexes_Compare extends TestCase {
	
	ArrayList<String> input= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_Indexes_Compare (String name){
		super(name);
	}
	
public void setUp(){
		
	
	input.add("src/test/resources/test/equivalence/test_404.owl");

}

public void testIndexes() throws Exception{
	//for each file in the input
	for (int i=0; i<input.size(); i++){
		String fileInput=input.get(i);

		TBoxReasonerImpl dag= new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));


		//add input named graph
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);

		
		log.debug("Input number {}", i+1 );
		log.info("named graph {}", engine.getNamedDAG());
		
		
		testIndexes(engine, engine.getNamedDAG());

		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(fileInput));
		Ontology onto = t.translate(owlonto);
		DAG dag2 = DAGConstructor.getISADAG(onto);
		dag2.clean();
        DAGOperations.buildDescendants(dag2);
        DAGOperations.buildAncestors(dag2);
		DAG pureIsa = DAGConstructor.filterPureISA(dag2);
		 pureIsa.clean();
			pureIsa.index();
			 DAGOperations.buildDescendants(pureIsa);
		        DAGOperations.buildAncestors(pureIsa);
		 testOldIndexes(pureIsa, engine);
		
	}
}

private void testOldIndexes(DAG d1, SemanticIndexBuilder d2){
	
	
	
	for(DAGNode d: d1.getClasses()){
		System.out.println(d + "\n "+ d.getEquivalents());
		System.out.println(d1.equi_mappings.values());
		
	}
	
	
	for(DAGNode d: d1.getRoles()){
		System.out.println(d );
		for(DAGNode dd: d.getEquivalents()){
		System.out.println(d1.getRoleNode(((Property)dd.getDescription())));
		;
		}
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		System.out.println(d1.getRoleNode(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2")));
		;
	}
	

	

	
	
}
private boolean testIndexes(SemanticIndexBuilder engine, NamedDAG namedDAG) {
	boolean result=false;
	
	
	//create semantic index
	Map<Description, SemanticIndexRange> ranges=engine.getIntervals();
	
	//check that the index of the node is contained in the intervals of the parent node
	for(Description vertex: engine.getIndexed()) { // .getNamedDAG().vertexSet()
		int index= engine.getIndex(vertex);
		log.info("vertex {} index {}", vertex, index);
		if (vertex instanceof Property) {
			for(Description parent: namedDAG.getSuccessors((Property)vertex)){
				result = ranges.get(parent).contained(new SemanticIndexRange(index,index));			
				if(result)
					break;
			}
		}
		else {
			for(Description parent: namedDAG.getSuccessors((BasicClassDescription)vertex)){
				result = ranges.get(parent).contained(new SemanticIndexRange(index,index));			
				if(result)
					break;
			}
			
		}	
		if(!result)
			break;
	}
	
	log.info("ranges {}", ranges);
	
	
	return result;
	

}






}
