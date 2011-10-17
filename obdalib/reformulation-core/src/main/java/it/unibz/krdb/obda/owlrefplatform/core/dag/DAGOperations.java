package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Graph specific operations like get all ancestors and get all
 * descendants
 * 
 * @author Sergejs Pugac
 */

public class DAGOperations {
	private static final Logger	log	= LoggerFactory.getLogger(DAGOperations.class);

	
	public static void buildDescendants(DAG dag) {
		buildDescendants(dag.roles);
		buildDescendants(dag.classes);
	}
	
	/**
	 * Calculate the descendants for all nodes in the given DAG
	 * 
	 * @param dagnodes
	 *            a DAG
	 * @return Map from uri to the Set of their descendants
	 */
	public static void buildDescendants(Map<Description, DAGNode> dagnodes) {
		Queue<DAGNode> stack = new LinkedList<DAGNode>();

		// Start with bottom nodes, that don't have children
		for (DAGNode n : dagnodes.values()) {
			n.getDescendants().clear();
			if (n.getChildren().isEmpty()) {
				stack.add(n);
			}
		}
		if (stack.isEmpty() && !dagnodes.isEmpty()) {
			log.error("Can not build descendants for graph with cycles");
		}
		while (!stack.isEmpty()) {
			DAGNode cur_el = stack.remove();
			
//			cur_el.getDescendants().add(cur_el);
			
			for (DAGNode eq_node : cur_el.equivalents) {
				if (!cur_el.getDescendants().contains(eq_node))
					cur_el.getDescendants().add(eq_node);
			}

			for (DAGNode par_node : cur_el.getParents()) {

				// add child to descendants list
				if (!par_node.getDescendants().contains(cur_el)) {
					par_node.getDescendants().add(cur_el);
				}

				// add child children to descendants list
				for (DAGNode cur_el_descendant : cur_el.getDescendants()) {
					if (!par_node.getDescendants().contains(cur_el_descendant))
						par_node.getDescendants().add(cur_el_descendant);
				}
				stack.add(par_node);
			}
		}
	}

	/**
	 * Adds two edges between child and parent. The first edge is a child
	 * hasParent edge, the second edge is a parent hasChild edge. The method
	 * guarantees no duplicate edges in any single node, e.g., no node will have
	 * the edge A hasParent B two times.
	 * 
	 * @param childnode
	 * @param parentnode
	 */
	public static void addParentEdge(DAGNode childnode, DAGNode parentnode) {

		if (childnode.equals(parentnode)) {
			return;
		}

		childnode.getParents().add(parentnode);
		parentnode.getChildren().add(childnode);
	}

	/**
	 * Removes the hasParent and hasChild edges between the two nodes, if they
	 * exist. The method also guarantees that if as a result of this operation
	 * the Node's children or parents list becomes empty, then they will be
	 * assigned NULL. This is done in order to gurantee that at any time, if a
	 * Node has no children or parents, then the correspondent collections will
	 * be NULL.
	 * 
	 * @param childnode
	 * @param parentnode
	 */
	private static void removeParentEdge(DAGNode childnode, DAGNode parentnode) {
		childnode.getParents().remove(parentnode);
		parentnode.getChildren().remove(childnode);
	}

	/**
	 * The methods setups the equivalent relations between n1 and n2. Note that
	 * this doesn't guarantee that nodes that are equivalent to n2 and n1 are
	 * also set. For that purpose see {@see addAllEquivalences}.
	 * 
	 * @param node1
	 * @param node2
	 */
	public static void addEquivalence(DAGNode node1, DAGNode node2) {
		if (node1.equals(node2))
			return;

		node1.getEquivalents().remove(node2);
		node1.getEquivalents().add(node2);

		node2.getEquivalents().remove(node1);
		node2.getEquivalents().add(node1);
	}

	public static void computeTransitiveReduct(Map<Description, DAGNode> dagnodes) {
		buildDescendants(dagnodes);

		LinkedList<Edge> redundantEdges = new LinkedList<Edge>();
		for (DAGNode node : dagnodes.values()) {
			for (DAGNode child : node.getChildren()) {
				for (DAGNode child_desc : child.getDescendants()) {
					redundantEdges.add(new Edge(node, child_desc));
				}
			}
		}
		for (Edge edge : redundantEdges) {
			DAGNode from = edge.getLeft();
			DAGNode to = edge.getRight();

			from.getChildren().remove(to);
			to.getParents().remove(from);
		}
	}

	public static void removeCycles(Map<Description, DAGNode> dagnodes, Map<Description, Description> equi_mapp, DAG dag) {

		// Finding the cycles (strongly connected components)
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		ArrayList<ArrayList<DAGNode>> sccs = scc(dagnodes);

		/*
		 * A set with all the nodes that have been proceesed as participating in
		 * an equivalence cycle. If a component contains any of these nodes, the
		 * component should be ignored, since a cycle involving the same nodes
		 * or nodes for inverse descriptions has already been processed.
		 */
		Set<DAGNode> processedNodes = new HashSet<DAGNode>();

		for (ArrayList<DAGNode> component : sccs) {

			/*
			 * Avoiding processing nodes two times
			 */
			boolean ignore = false;
			for (DAGNode node : component) {
				if (processedNodes.contains(node)) {
					ignore = true;
					break;
				}
			}
			if (ignore)
				continue;

			DAGNode cycleheadNode = component.get(0);
			DAGNode cycleheadinverseNode = null;
			DAGNode cycleheaddomainNode = null;
			DAGNode cycleheadrangeNode = null;

			if (cycleheadNode.getDescription() instanceof Property) {

				Property prop = (Property) cycleheadNode.getDescription();

				Property inverse = fac.createProperty(prop.getPredicate(), !prop.isInverse());
				PropertySomeRestriction domain = fac.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse());
				PropertySomeRestriction range = fac.createPropertySomeRestriction(prop.getPredicate(), !prop.isInverse());

				cycleheadinverseNode = dag.getNode(inverse);
				cycleheaddomainNode = dag.getNode(domain);
				cycleheadrangeNode = dag.getNode(range);
			}

			/*
			 * putting a cyclehead that is a named concept or named role
			 */
			if (component.size() > 1 && cycleheadNode.getDescription() instanceof PropertySomeRestriction) {

				for (int i = 1; i < component.size(); i++) {
					if (component.get(i).getDescription() instanceof OClass) {
						DAGNode tmp = component.get(i);
						component.set(i, cycleheadNode);
						component.set(0, tmp);
						cycleheadNode = tmp;
						break;
					}
				}
			}

			if (component.size() > 0 && cycleheadNode.getDescription() instanceof Property
					&& ((Property) cycleheadNode.getDescription()).isInverse()) {
				for (int i = 1; i < component.size(); i++) {
					if (component.get(i).getDescription() instanceof Property
							&& !((Property) component.get(i).getDescription()).isInverse()) {
						DAGNode tmp = component.get(i);
						component.set(i, cycleheadNode);
						component.set(0, tmp);
						cycleheadNode = tmp;

						Property prop = (Property) cycleheadNode.getDescription();

						Property inverse = fac.createProperty(prop.getPredicate(), !prop.isInverse());
						PropertySomeRestriction domain = fac.createPropertySomeRestriction(prop.getPredicate(), prop.isInverse());
						PropertySomeRestriction range = fac.createPropertySomeRestriction(prop.getPredicate(), !prop.isInverse());

						cycleheadinverseNode = dag.getNode(inverse);
						cycleheaddomainNode = dag.getNode(domain);
						cycleheadrangeNode = dag.getNode(range);

						break;
					}
				}
			}
			processedNodes.add(cycleheadNode);

			if (cycleheadinverseNode != null) {
				processedNodes.add(cycleheadinverseNode);
				processedNodes.add(cycleheaddomainNode);
				processedNodes.add(cycleheadrangeNode);
			}

			/*
			 * Collapsing the cycle (the nodes in the component)
			 */
			for (int i = 1; i < component.size(); i++) {
				DAGNode equivnode = component.get(i);

				for (DAGNode parent : new LinkedList<DAGNode>(equivnode.getParents())) {
					removeParentEdge(equivnode, parent);
					addParentEdge(cycleheadNode, parent);
				}

				for (DAGNode childchild : new LinkedList<DAGNode>(equivnode.getChildren())) {
					removeParentEdge(childchild, equivnode);
					addParentEdge(childchild, cycleheadNode);
				}

				if (cycleheadinverseNode != null) {
					/*
					 * we are dealing with properties, so we need to also
					 * collapse the inverses and existentials
					 */
					Property equiprop = (Property) equivnode.getDescription();

					DAGNode equivinverseNode = dag.getNode(fac.createProperty(equiprop.getPredicate(), !equiprop.isInverse()));
					DAGNode equivDomainNode = dag.getNode(fac.createPropertySomeRestriction(equiprop.getPredicate(), equiprop.isInverse()));
					DAGNode equivRangeNode = dag.getNode(fac.createPropertySomeRestriction(equiprop.getPredicate(), !equiprop.isInverse()));

					/*
					 * Doing the inverses
					 */
					for (DAGNode parent : new LinkedList<DAGNode>(equivinverseNode.getParents())) {
						removeParentEdge(equivinverseNode, parent);
						addParentEdge(cycleheadinverseNode, parent);
					}

					for (DAGNode childchild : new LinkedList<DAGNode>(equivinverseNode.getChildren())) {
						removeParentEdge(childchild, equivinverseNode);
						addParentEdge(childchild, cycleheadinverseNode);
					}

					/*
					 * Doing the domain
					 */

					for (DAGNode parent : new LinkedList<DAGNode>(equivDomainNode.getParents())) {
						removeParentEdge(equivDomainNode, parent);
						addParentEdge(cycleheaddomainNode, parent);
					}

					for (DAGNode childchild : new LinkedList<DAGNode>(equivDomainNode.getChildren())) {
						removeParentEdge(childchild, equivDomainNode);
						addParentEdge(childchild, cycleheaddomainNode);
					}

					/*
					 * Collapsing the range
					 */

					for (DAGNode parent : new LinkedList<DAGNode>(equivRangeNode.getParents())) {
						removeParentEdge(equivRangeNode, parent);
						addParentEdge(cycleheadrangeNode, parent);
					}

					for (DAGNode childchild : new LinkedList<DAGNode>(equivRangeNode.getChildren())) {
						removeParentEdge(childchild, equivRangeNode);
						addParentEdge(childchild, cycleheadrangeNode);
					}
				}

				Description description = equivnode.getDescription();

				/***
				 * Setting up the equivalence map
				 */

				dagnodes.remove(equivnode.getDescription());
				equi_mapp.put(equivnode.getDescription(), cycleheadNode.getDescription());
				cycleheadNode.equivalents.add(equivnode);
				dag.allnodes.remove(equivnode.getDescription());
				dag.classes.remove(equivnode.getDescription());
				dag.roles.remove(equivnode.getDescription());

				processedNodes.add(equivnode);

				if (description instanceof Property) {

					
					
					
					/*
					 * we are dealing with properties, so we need to also
					 * collapse the inverses and existentials
					 */
					Property equiprop = (Property) equivnode.getDescription();
					
					Property inverseequiprop = fac.createProperty(equiprop.getPredicate(), !equiprop.isInverse());
					Property cycleheadprop =(Property)cycleheadNode.getDescription(); 
					Property invesenonredundantprop = fac.createProperty(cycleheadprop.getPredicate(), !cycleheadprop.isInverse());
					equi_mapp.put(inverseequiprop, invesenonredundantprop);
					dag.equi_mappings.put(inverseequiprop, invesenonredundantprop);
					
					

					DAGNode equivinverseNode = dag.getNode(inverseequiprop);
					DAGNode equivDomainNode = dag.getNode(fac.createPropertySomeRestriction(equiprop.getPredicate(), equiprop.isInverse()));
					DAGNode equivRangeNode = dag.getNode(fac.createPropertySomeRestriction(equiprop.getPredicate(), !equiprop.isInverse()));

					if (!(equivinverseNode == null && equivDomainNode == null && equivRangeNode == null)) {
						/*
						 * This check is only necesary because of ISA DAGs in
						 * which we removed all descriptions that are not named
						 * classes or roles... in the future we will simplify
						 * this.
						 */

						processedNodes.add(equivinverseNode);
						processedNodes.add(equivDomainNode);
						processedNodes.add(equivRangeNode);

						dag.getRoles().remove(equivinverseNode.getDescription());
						dag.getClasses().remove(equivDomainNode.getDescription());
						dag.getClasses().remove(equivRangeNode.getDescription());
						
						dag.allnodes.remove(equivinverseNode.getDescription());
						dag.classes.remove(equivinverseNode.getDescription());
						dag.roles.remove(equivinverseNode.getDescription());
						
						dag.allnodes.remove(equivDomainNode.getDescription());
						dag.classes.remove(equivDomainNode.getDescription());
						dag.roles.remove(equivDomainNode.getDescription());

						dag.allnodes.remove(equivRangeNode.getDescription());
						dag.classes.remove(equivRangeNode.getDescription());
						dag.roles.remove(equivRangeNode.getDescription());

						

						equi_mapp.put(equivinverseNode.getDescription(), cycleheadinverseNode.getDescription());
						equi_mapp.put(equivDomainNode.getDescription(), cycleheaddomainNode.getDescription());
						equi_mapp.put(equivRangeNode.getDescription(), cycleheadrangeNode.getDescription());

						cycleheadinverseNode.equivalents.add(equivinverseNode);
						cycleheaddomainNode.equivalents.add(equivDomainNode);
						cycleheadrangeNode.equivalents.add(equivRangeNode);
					}

				}

			}
		}
	}

	private static int								index	= 0;
	private static ArrayList<DAGNode>				stack;
	private static ArrayList<ArrayList<DAGNode>>	SCC;

	private static Map<DAGNode, Integer>			t_idx;
	private static Map<DAGNode, Integer>			t_low_idx;

	private static ArrayList<ArrayList<DAGNode>> scc(Map<Description, DAGNode> list) {
		stack = new ArrayList<DAGNode>();
		SCC = new ArrayList<ArrayList<DAGNode>>();
		t_idx = new HashMap<DAGNode, Integer>();
		t_low_idx = new HashMap<DAGNode, Integer>();
		for (DAGNode node : list.values()) {
			if (t_idx.get(node) == null) {
				strongconnect(node);
			}
		}
		return SCC;
	}

	private static void strongconnect(DAGNode v) {
		t_idx.put(v, index);
		t_low_idx.put(v, index);

		index++;
		stack.add(0, v);
		for (DAGNode child : v.getChildren()) {
			if (t_idx.get(child) == null) {
				strongconnect(child);
				t_low_idx.put(v, Math.min(t_low_idx.get(v), t_low_idx.get(child)));
			} else if (stack.contains(child)) {
				t_low_idx.put(v, Math.min(t_low_idx.get(v), t_idx.get(child)));
			}
		}
		if (t_low_idx.get(v).equals(t_idx.get(v))) {
			DAGNode n;
			ArrayList<DAGNode> component = new ArrayList<DAGNode>();
			do {
				n = stack.remove(0);
				component.add(n);
			} while (!n.equals(v));
			SCC.add(component);
		}
	}

}
