package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.TBoxDAG;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNOMEDTestJGraphT {
	public static void main(String args[]) throws Exception {

		Logger log = LoggerFactory.getLogger("SNOMEDTEST");

		String owlfile = "/Users/mariano/Downloads/SnomedCT_INT_20110731/res_StatedOWLF_Core_INT_20110731.owl";
		// String owlfile =
		// "src/test/resources/test/stockexchange-unittest.owl";
		log.info("Loading SNOMED");

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		log.info("Translating");

		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology o = translator.translate(ontology);

		log.info("Generating dag");
		TBoxDAG dag = new TBoxDAG(o);

		log.info("Computing connected components");
		StrongConnectivityInspector<Description, DefaultEdge> inspector = new StrongConnectivityInspector<Description, DefaultEdge>(
				dag.getDag());
		List<Set<Description>> equivalenceSets = inspector.stronglyConnectedSets();

//		int count = 0;
//		for (Set<Description> set : equivalenceSets) {
//			if (set.size() > 1) {
//				System.out.println(set);
//				count += 1;
//			}
//		}
		
		log.info("Equi sets: {}", equivalenceSets.size());
		
		log.info("Transitive closure");
		TransitiveClosure t = TransitiveClosure.INSTANCE;
		t.closeSimpleDirectedGraph(dag.getDag());

		

		log.info("Done.");

	}
}
