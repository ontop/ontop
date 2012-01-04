package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.SemanticIndex.SemanticIndexHelper;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGEdgeIterator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.Edge;
import junit.framework.TestCase;

public class DAGEdgeIteratorTest extends TestCase {

	SemanticIndexHelper	helper	= new SemanticIndexHelper();

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEnumerationOfSubClass() throws Exception {
		Ontology ontology = helper.load_onto("equivalence-classes");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.clean();
		
		DAGEdgeIterator it = new DAGEdgeIterator(isa);
		int count = 0;
		while (it.hasNext()) {
			count += 1;
			Edge edge = it.next();
			//System.out.println(edge.toString());
		}
		assertEquals(9,count);
	}

	public void testEnumerationOfSubProperty() throws Exception {
		Ontology ontology = helper.load_onto("equivalence-roles");
		DAG isa = DAGConstructor.getISADAG(ontology);
		isa.clean();
		
		DAGEdgeIterator it = new DAGEdgeIterator(isa);
		int count = 0;
		while (it.hasNext()) {
			count += 1;
			Edge edge = it.next();
			//System.out.println(edge.toString());
		}
		
		/* 9 for role inclusions, 9 for exists R, 9 for exists Rinv */
		assertEquals(36,count);
	}

	public void testEnumerationForSingleNodeClass() {

	}

	public void testEnumerationForSingleNodeProperty() {

	}

}
