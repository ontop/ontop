package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to transform/manipulate DAGs into "chain-reachability" DAGs.
 */
public class DAGChain {

	private final static Logger	log	= LoggerFactory.getLogger(DAGChain.class);

	/***
	 * Modifies the DAG so that \exists R = \exists R-, so that the reachability
	 * relation of the original DAG gets extended to the reachability relation
	 * of T and Sigma chains.
	 * 
	 * @param dag
	 */
	public static void getChainDAG(DAG dag) {
		Collection<DAGNode> nodes = new HashSet<DAGNode>(dag.getAllnodes().values());
		OntologyFactory fac = OntologyFactoryImpl.getInstance();
		HashSet<DAGNode> processedNodes = new HashSet<DAGNode>();
		for (DAGNode node : nodes) {
			if (!(node.getDescription() instanceof PropertySomeRestriction) || processedNodes.contains(node)) {
				continue;
			}

			/*
			 * Adding a cycle between exists R and exists R- for each R.
			 */

			PropertySomeRestriction existsR = (PropertySomeRestriction) node.getDescription();
			PropertySomeRestriction existsRin = fac.createPropertySomeRestriction(existsR.getPredicate(), !existsR.isInverse());
			DAGNode existsNode = node;
			DAGNode existsInvNode = dag.getNode(existsRin);
			Set<DAGNode> childrenExist = new HashSet<DAGNode>(existsNode.getChildren());
			Set<DAGNode> childrenExistInv = new HashSet<DAGNode>(existsInvNode.getChildren());

			for (DAGNode child : childrenExist) {
				DAGOperations.addParentEdge(child, existsInvNode);
			}
			for (DAGNode child : childrenExistInv) {
				DAGOperations.addParentEdge(child, existsNode);
			}
			
			Set<DAGNode> parentExist = new HashSet<DAGNode>(existsNode.getParents());
			Set<DAGNode> parentsExistInv = new HashSet<DAGNode>(existsInvNode.getParents());

			for (DAGNode parent : parentExist) {
				DAGOperations.addParentEdge(existsInvNode, parent);
			}
			for (DAGNode parent : parentsExistInv) {
				DAGOperations.addParentEdge(existsNode,parent);
			}

			processedNodes.add(existsInvNode);
			processedNodes.add(existsNode);
		}

		/* Collapsing the cycles */

		dag.clean();
//		DAGOperations.computeTransitiveReduct(dag.getAllnodes());
//		DAGOperations.buildDescendants(dag.getAllnodes());
	}

}
