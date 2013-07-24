package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphDAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxGraphImpl;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class S_InputOWL {
	
	
	public static Ontology createOWL(String file) throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(file)));

		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology o = translator.translate(ontology);
		return o;
	}
	
	public static DAGImpl createDAG(String file) throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(file)));

		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology o = translator.translate(ontology);
		
		//generate Graph
		TBoxGraphImpl change= new TBoxGraphImpl(o);
		
		GraphImpl graph = (GraphImpl) change.getGraph();
		
		//generate DAG
		GraphDAGImpl change2 = new GraphDAGImpl(graph);
		
		DAGImpl dag=(DAGImpl) change2.getDAG();
		

		return dag;
	}
	
public static GraphImpl createGraph(String file) throws Exception{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(file)));

		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology o = translator.translate(ontology);
		
		//generate Graph
		TBoxGraphImpl change= new TBoxGraphImpl(o);
		
		GraphImpl graph = (GraphImpl) change.getGraph();
		
		
		
		return graph;
	}

}
