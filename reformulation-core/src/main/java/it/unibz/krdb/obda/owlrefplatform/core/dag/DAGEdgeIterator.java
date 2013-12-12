package it.unibz.krdb.obda.owlrefplatform.core.dag;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/***
 * An utility class that will help you enumerate all the edges, given or
 * transitive, of a DAG. This class is aimed at facilitating the computation of
 * all entailments of a DL-Lite/OWL2QL/RDFS ontology. It allows to avoid the
 * materialization of the transitive clousure of an ontology, keeping only the
 * compact representation of its entailments, i.e., the DAG.
 * 
 * This particular iterator will enumerate all the edges "CHILD isa PARENT"
 * starting from the top nodes, enumerating all the children nodes nodes for
 * each PARENT node before advancing to the next. Each enumartion (i.e., parents
 * and children) is done breadth-first.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class DAGEdgeIterator implements Iterator<Edge> {

	DAG				dag								= null;

	Queue<DAGNode>	parentNodeQueue					= new LinkedList<DAGNode>();

	/* Current parent information and pointers */

	DAGNode			currentParent					= null;

	// DAGNode currentChild = null;

	Queue<DAGNode>	nextChildrenQueue				= new LinkedList<DAGNode>();

	Queue<Edge>		currentEdges					= new LinkedList<Edge>();

	/***
	 * Indicates that if a child has equivalences, these must me made explicit
	 * the moment they are found. This is necessary to assure completeness of
	 * the output when the iterator is started from a node and not a DAG.
	 */
	boolean			processChildrensEquivalences	= false;

	boolean			allowParentAdvance				= true;

	public DAGEdgeIterator(DAG dag) {
		for (DAGNode node: dag.getAllnodes().values()) {
			if (node.getParents().isEmpty()) {
				parentNodeQueue.add(node);
			}
		}
//		for (DAGNode node : dag.getClasses()) {
//			if (node.getParents().isEmpty()) {
//				parentNodeQueue.add(node);
//			}
//		}
//		for (DAGNode node : dag.getRoles()) {
//			if (node.getParents().isEmpty()) {
//				parentNodeQueue.add(node);
//			}
//		}
		processChildrensEquivalences = false;
		setCurrentParent(parentNodeQueue.poll());

	}

	public DAGEdgeIterator(DAGNode node) {
		setCurrentParent(node);
		processChildrensEquivalences = true;
		allowParentAdvance = false;
	}

	/***
	 * Sets the current parent to node and computes all the immediate "sub"
	 * edges with node in the right, adding them to "currentEdges". These
	 * include all the equivalent nodes and the immediate children of node.
	 * 
	 * Last, it will add the children of node to the queue of parents. If
	 * allowParentAdvance is true, the queue of parents will be used to advance
	 * through all the nodes in the DAG.
	 * 
	 * @param node
	 */
	private void setCurrentParent(DAGNode node) {
		if (node == null)
			return;
		currentParent = node;
		for (DAGNode equivalent : currentParent.getEquivalents()) {
			currentEdges.add(new Edge(equivalent, currentParent));
			currentEdges.add(new Edge(currentParent, equivalent));
		}
		nextChildrenQueue.addAll(currentParent.getChildren());

		parentNodeQueue.addAll(currentParent.getChildren());
	}

	/***
	 * Tries to setup the current parent to the next one in the queue of
	 * parents. If there are no parents in the queue non it will return false.
	 * 
	 * @return
	 */
	private boolean advanceParent() {
		if (!allowParentAdvance) {
			return false;
		}
		DAGNode nextParent = parentNodeQueue.poll();
		if (nextParent == null)
			return false;
		setCurrentParent(nextParent);
		return true;

	}

	private boolean advanceChild() {
		if (currentParent == null)
			return false;
		DAGNode child = nextChildrenQueue.poll();
		if (child == null)
			return false;
		currentEdges.add(new Edge(child, currentParent));
		nextChildrenQueue.addAll(child.getChildren());

		if (processChildrensEquivalences) {
			for (DAGNode childequivalent : child.getEquivalents()) {
				currentEdges.add(new Edge(childequivalent, child));
				currentEdges.add(new Edge(child, childequivalent));
			}
		}

		return true;
	}

	private boolean currentParentHasNext() {
		if (currentEdges.peek() != null)
			return true;
		return advanceChild();
	}

	@Override
	public boolean hasNext() {
		if (currentParentHasNext())
			return true;
		while (advanceParent()) {
			if (currentParentHasNext())
				return true;
		}
		return false;
	}

	@Override
	public Edge next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return currentEdges.poll();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
