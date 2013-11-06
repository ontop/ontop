package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/** Build a DAG with only the named descriptions */

public class NamedDAGBuilderImpl implements NamedDAGBuilder {

	private Set<OClass> namedClasses;
	private Set<Property> property;

	private Map<Description, Set<Description>> equivalencesMap = new HashMap<Description, Set<Description>>();

	private Map<Description, Description> replacements = new HashMap<Description, Description>();;
	DAGImpl namedDag;

	private TBoxReasonerImpl reasoner;

	public NamedDAGBuilderImpl(DAG dag) {

		namedDag = new DAGImpl(DefaultEdge.class);

		// clone all the vertexes and edges from dag

		for (Description v : ((DAGImpl) dag).vertexSet()) {
			namedDag.addVertex(v);
		}
		for (DefaultEdge e : ((DAGImpl) dag).edgeSet()) {
			Description s = ((DAGImpl) dag).getEdgeSource(e);
			Description t = ((DAGImpl) dag).getEdgeTarget(e);

			namedDag.addEdge(s, t, e);
		}

		reasoner = new TBoxReasonerImpl(dag);
		// take classes, roles, equivalences map and replacements from the DAG
		namedClasses = dag.getClasses();
		property = dag.getRoles();

		// clone the equivalences and replacements map
		Map<Description, Set<Description>> equivalencesDag = dag
				.getMapEquivalences();
		Map<Description, Description> replacementsDag = dag.getReplacements();
		for (Description vertex : equivalencesDag.keySet()) {
			

				HashSet<Description> equivalents = new HashSet<Description>();
				for (Description equivalent : equivalencesDag.get(vertex)) {
					equivalents.add(equivalent);
				}
				equivalencesMap.put(vertex, new HashSet<Description>(
						equivalents));
			

		}
		for (Description eliminateNode : replacementsDag.keySet()) {

			Description referent = replacementsDag.get(eliminateNode);
			replacements.put(eliminateNode, referent);

		}

		for (Description vertex : ((DAGImpl) dag).vertexSet()) {

			// if the vertex has equivalent nodes leave only the named one
//			Set<Description> equivalences = checkEquivalences(vertex);

			// if the node is named keep it
			if (namedClasses.contains(vertex) | property.contains(vertex)) {

				continue;
			}

			// Description reference = replacements.get(vertex);

			// /** if the node is not named and it's representative delete it
			// and repoint all links */
			//
			// if(!equivalences.isEmpty()){ //node is not named and there are
			// equivalent named classes
			//
			//
			// //change the representative node
			// Iterator<Description> e=equivalences.iterator();
			// Description newReference= e.next();
			// replacements.remove(newReference);
			// namedDag.addVertex(newReference);
			//
			// while(e.hasNext()){
			// Description node =e.next();
			// replacements.put(node, newReference);
			// }
			//
			// /*
			// * Re-pointing all links to and from the eliminated node to the
			// new
			// * representative node
			// */
			//
			// Set<DefaultEdge> edges = new
			// HashSet<DefaultEdge>(namedDag.incomingEdgesOf(vertex));
			// for (DefaultEdge incEdge : edges) {
			// Description source = namedDag.getEdgeSource(incEdge);
			// namedDag.removeAllEdges(source, vertex);
			//
			// if (source.equals(newReference))
			// continue;
			//
			// namedDag.addEdge(source, newReference);
			// }
			//
			// edges = new
			// HashSet<DefaultEdge>(namedDag.outgoingEdgesOf(vertex));
			// for (DefaultEdge outEdge : edges) {
			// Description target = namedDag.getEdgeTarget(outEdge);
			// namedDag.removeAllEdges(vertex, target);
			//
			// if (target.equals(newReference))
			// continue;
			// namedDag.addEdge(newReference, target);
			// }
			//
			// namedDag.removeVertex(vertex);
			// }
			//
			// else{ //representative node without equivalents
			// add edge between the first of the ancestor that it's still
			// present and its child

			Set<DefaultEdge> incomingEdges = new HashSet<DefaultEdge>(
					namedDag.incomingEdgesOf(vertex));

			// I do a copy of the dag not to remove edges that I still need to
			// consider in the loops
			DAGImpl copyDAG = (DAGImpl) namedDag.clone();
			Set<DefaultEdge> outgoingEdges = new HashSet<DefaultEdge>(
					copyDAG.outgoingEdgesOf(vertex));
			for (DefaultEdge incEdge : incomingEdges) {

				Description source = namedDag.getEdgeSource(incEdge);
				namedDag.removeAllEdges(source, vertex);

				for (DefaultEdge outEdge : outgoingEdges) {
					Description target = copyDAG.getEdgeTarget(outEdge);
					namedDag.removeAllEdges(vertex, target);

					if (source.equals(target))
						continue;
					namedDag.addEdge(source, target);
				}

			}

			namedDag.removeVertex(vertex);
			// }

		}

		namedDag.setMapEquivalences(equivalencesMap);
		namedDag.setReplacements(replacements);
		namedDag.setIsaNamedDAG(true);

	}

	/** using the TBoxReasoner keeps only the equivalent named nodes */

	private Set<Description> checkEquivalences(Description vertex) {
		// delete from equivalencesMap and assign a new representative node
		Set<Description> namedEquivalences = reasoner.getEquivalences(vertex,
				true);
		Set<Description> allEquivalences = reasoner.getEquivalences(vertex,
				false);
		;

		Iterator<Description> e = allEquivalences.iterator();

		while (e.hasNext()) {
			Description node = e.next();
			if (namedEquivalences.contains(node)
					&& namedEquivalences.size() > 1) {
				equivalencesMap.put(node, namedEquivalences);
			} else {
				replacements.remove(node);
				equivalencesMap.remove(node);

			}
		}
		return namedEquivalences;

	}

	@Override
	public DAGImpl getDAG() {
		return namedDag;
	}

}
