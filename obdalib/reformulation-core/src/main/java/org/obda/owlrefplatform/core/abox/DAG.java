package org.obda.owlrefplatform.core.abox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLEntity;

class DAG {

	private Map<OWLEntity, Node> dagnodes = new HashMap<OWLEntity, Node>();
	private int index_counter = 1;

	private static final SemanticIndexRange NULL_RANGE = new SemanticIndexRange(
			-1, -1);
	private static final int NULL_INDEX = -1;

	public void addEdge(OWLEntity from, OWLEntity to) {

		Node f = dagnodes.get(from);
		if (f == null) {
			f = new Node(from);
			dagnodes.put(from, f);
		}

		Node t = dagnodes.get(to);
		if (t == null) {
			t = new Node(to);
			dagnodes.put(to, t);
		}
		t.children.add(f);
		f.parents.add(t);
	}

	public void index() {
		for (Node node : dagnodes.values()) {
			if (node.parents.isEmpty()) {
				indexNode(node);
			}
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

	public void buildRange() {
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

	public Map<OWLEntity, SemanticIndexRange> getIndex() {
		Map<OWLEntity, SemanticIndexRange> index = new HashMap<OWLEntity, SemanticIndexRange>(
				dagnodes.size());
		for (Node node : dagnodes.values()) {
			index.put(node.cls, node.range);
		}
		return index;
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

	class Node {

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

	}

}
