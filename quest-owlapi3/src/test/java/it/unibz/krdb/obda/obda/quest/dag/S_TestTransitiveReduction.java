package it.unibz.krdb.obda.obda.quest.dag;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TestTBoxReasonerImplOnGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_TestTransitiveReduction extends TestCase {

	ArrayList<String> input= new ArrayList<String>();
	ArrayList<String> output= new ArrayList<String>();

	Logger log = LoggerFactory.getLogger(S_HierarchyTestNewDAG.class);

	public S_TestTransitiveReduction (String name){
		super(name);
	}

	public void setUp(){
		
		input.add("src/test/resources/test/dag/test-equivalence-roles-inverse.owl");
		input.add("src/test/resources/test/dag/test-role-hierarchy.owl");
		input.add("src/test/resources/test/stockexchange-unittest.owl");
		input.add("src/test/resources/test/dag/role-equivalence.owl");
		
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
		/** P-> ER- =B -> A  C=L ->ES- -> ER- */
		input.add("src/test/resources/test/newDag/inverseEquivalents7.owl");
		/** B->A=ET- ->ER- C->ES- = D->A*/
		input.add("src/test/resources/test/newDag/inverseEquivalents8.owl");



	}

	public void testSimplification() throws Exception{
		//for each file in the input
		for (int i=0; i<input.size(); i++){
			String fileInput=input.get(i);

			TBoxReasonerImpl dag2 = new TBoxReasonerImpl(S_InputOWL.createOWL(fileInput));
			DefaultDirectedGraph<Description,DefaultEdge> graph1 = dag2.getGraph();

			log.debug("Input number {}", i+1 );
			log.info("First graph {}", graph1);
			log.info("Second dag {}", dag2);
						
			assertTrue(testRedundantEdges(graph1,dag2));


		}
	}


	private boolean testRedundantEdges(DefaultDirectedGraph<Description,DefaultEdge> g1, TBoxReasonerImpl d2){
		//number of edges in the graph
		int  numberEdgesD1= g1.edgeSet().size();
		//number of edges in the dag
		int numberEdgesD2 = d2.getDAG().edgeSetSize();

		//number of edges between the equivalent nodes
		int numberEquivalents=0;

		//number of redundant edges 
		int numberRedundants=0;

		Set<Equivalences<Description>> nodesd2= d2.getNodes();
		Iterator<Equivalences<Description>> it1 =nodesd2.iterator();
		while (it1.hasNext()) {
			Equivalences<Description> equivalents=it1.next();

			//two nodes have two edges, three nodes have three edges...
			if(equivalents.size()>=2){
				numberEquivalents += equivalents.size();
			}
		}

		TestTBoxReasonerImplOnGraph reasonerd1= new TestTBoxReasonerImplOnGraph(g1);

		Set<Equivalences<Description>> nodesg1= reasonerd1.getNodes();
		Iterator<Equivalences<Description>> it2 =nodesg1.iterator();
		

		while (it2.hasNext()) {

			
			Equivalences<Description> equivalents=it2.next();
			log.info("equivalents {} ", equivalents);
			
			
			
			//check if there are redundant edges
			for (Description vertex: equivalents){
				if(g1.incomingEdgesOf(vertex).size()!= g1.inDegreeOf(vertex)) //check that there anren't two edges pointing twice to the same nodes
					numberRedundants +=g1.inDegreeOf(vertex)- g1.incomingEdgesOf(vertex).size();
			
				
				//descendants of the vertex
				Set<Equivalences<Description>> descendants=d2.getDescendants(vertex);
				Set<Equivalences<Description>> children=d2.getDirectChildren(vertex);

				log.info("descendants{} ", descendants);
				log.info("children {} ", children);

				for(DefaultEdge edge: g1.incomingEdgesOf(vertex)){
					Description source=g1.getEdgeSource(edge);
					for(Equivalences<Description> descendant:descendants){

					if (!children.contains(descendant) & ! equivalents.contains(descendant.iterator().next()) &descendant.contains(source))
						numberRedundants +=1;	
					}
					

				}
				
				
			}

		}

		log.info("edges graph {}", numberEdgesD1);
		log.info("edges dag {}", numberEdgesD2);
		log.info("equivalents {} ", numberEquivalents);
		log.info("redundants {} ", numberRedundants);

		return numberEdgesD1>= (numberRedundants+ numberEquivalents+ numberEdgesD2);

	}
}

