package org.obda.owlrefplatform.core.abox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the SemanticIndex of TBox and implements operations for serializing
 * and deserializing it
 * 
 * @author Sergejs Pugacs
 * 
 */
public class DAG {

	private final Logger log = LoggerFactory.getLogger(DAG.class);

	private final Map<String, Node> dagnodes = new HashMap<String, Node>();

	private int index_counter = 1;

	private final SemanticIndexRange NULL_RANGE = new SemanticIndexRange(-1, -1);
	private final int NULL_INDEX = -1;

	/**
	 * Build the DAG from the ontologies
	 * 
	 * @param ontologies
	 *            ontologies that contain TBox assertions for the DAG
	 */
	public DAG(Set<OWLOntology> ontologies) {

		for (OWLOntology onto : ontologies) {
			log.debug("Generating SemanticIndex for ontology: " + onto);

			for (OWLAxiom ax : onto.getAxioms()) {
				if (ax instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom edge = (OWLSubClassAxiom) ax;

					// FIXME: not handling existencial on the left side

					if (!edge.getSubClass().isOWLNothing()
							&& !edge.getSuperClass().isOWLThing()) {
						OWLClass subClass = edge.getSubClass().asOWLClass();
						OWLClass superClass = edge.getSuperClass().asOWLClass();
						addEdge(subClass, superClass);
					}
				} else if (ax instanceof OWLSubPropertyAxiom) {
					OWLSubPropertyAxiom<OWLObjectProperty> edge = (OWLSubPropertyAxiom<OWLObjectProperty>) ax;

					if (!edge.getSubProperty().isAnonymous()
							&& !edge.getSuperProperty().isAnonymous()) {

						OWLObjectProperty subProperty = edge.getSubProperty();
						OWLObjectProperty superProperty = edge
								.getSuperProperty();
						addEdge(subProperty, superProperty);
					}
				} else if (ax instanceof OWLObjectPropertyRangeAxiom) {

					// FIXME: Create an Exists(Inverse R)
					log.debug("ObjectPropRange: " + ax);

				} else if (ax instanceof OWLObjectPropertyDomainAxiom) {

					// FIXME: Create an Exists(R)
					log.debug("ObjectPropDomain: " + ax);

				} else {
					log.debug("Not supported axiom: " + ax);
				}
			}
		}
		index();
		buildRange();
	}

	public SemanticIndexRange getIndexRange(String URI) {
		return dagnodes.get(URI).range;
	}

	/**
	 * Build the DAG from the DB data
	 * 
	 * @param conn
	 *            Connection to the DB where a DAG was serialized
	 */
	public DAG(Connection conn) {

		// TODO: Check if SemanticIndex already present in DB and load it
		log.debug("Checking if SemanticIndex exists in DB");

		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null,
					SemanticIndexDumper.index_table, null);
			if (rs.next()) {
				log.debug("SemanticIndex exists in the DB, trying to load it");
				// table exists
			} else {
				log.error("SemanticIndex does not exist");
			}
		} catch (SQLException e) {
			// FIXME: add proper error handling
			e.printStackTrace();
		}
	}

	private void addEdge(OWLEntity from, OWLEntity to) {

		Node f = dagnodes.get(from.toString());
		if (f == null) {
			f = new Node(from);
			dagnodes.put(from.toString(), f);
		}

		Node t = dagnodes.get(to.toString());
		if (t == null) {
			t = new Node(to);
			dagnodes.put(to.toString(), t);
		}
		t.children.add(f);
		f.parents.add(t);
	}

	private void index() {
		LinkedList<Node> roots = new LinkedList<Node>();
		for (Node n : dagnodes.values()) {
			if (n.parents.isEmpty()) {
				roots.add(n);
			}
		}

		// This is optional, can be removed for performance but
		// The unit tests depend on this to guarantee certain
		// numberings

		Collections.sort(roots);

		for (Node node : roots) {
			indexNode(node);
		}
	}

	private void indexNode(Node node) {

		if (node.index == NULL_INDEX) {
			node.index = index_counter;
			index_counter++;
		} else {
			return;
		}
		for (Node ch : node.children) {
			indexNode(ch);
		}
	}

	private void buildRange() {
		for (Node node : dagnodes.values()) {
			if (node.parents.isEmpty()) {
				buildRangeNode(node);
			}
		}
	}

	private void buildRangeNode(Node node) {
		node.range = new SemanticIndexRange(node.index, node.index);

		if (node.children.isEmpty()) {
			return;
		}
		for (Node ch : node.children) {
			buildRangeNode(ch);
			node.range.addRange(ch.range);
		}
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		for (Node node : dagnodes.values()) {
			res.append(node.toString());
			res.append("\n");
		}
		return res.toString();
	}

	private class Node implements Comparable<Node> {

		private OWLEntity cls;
		private SemanticIndexRange range = NULL_RANGE;
		private int index = NULL_INDEX;
		private Set<Node> parents = new HashSet<Node>();
		private Set<Node> children = new HashSet<Node>();

		public Node(OWLEntity cls) {
			this.cls = cls;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (this.getClass() != other.getClass())
				return false;

			Node otherNode = (Node) other;
			return this.cls.equals(otherNode.cls);
		}

		@Override
		public int hashCode() {
			return this.cls.hashCode();
		}

		@Override
		public String toString() {
			return cls.toString() + range.toString();
		}

		@Override
		public int compareTo(Node o) {
			return this.cls.compareTo(o.cls);
		}

	}

}
