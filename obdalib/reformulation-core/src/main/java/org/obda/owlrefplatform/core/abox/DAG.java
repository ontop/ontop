package org.obda.owlrefplatform.core.abox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;

class DAG {

	private Map<OWLClass, Node> dagnodes = new HashMap<OWLClass, Node>();
	private int index_counter = 1;

	public void addEdge(OWLClass from, OWLClass to) {

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
		t.addChild(f);
		f.addParent(t);
	}

	public void index() {
		for (Node node : dagnodes.values()) {
			if (node.parents.isEmpty()) {
				indexNode(node);
			}
		}
	}

	private void indexNode(Node node) {
		if (node.range != null) {
			return;
		}
		node.range = new SemanticIndexRange(index_counter, index_counter);

		index_counter++;

		for (Node ch : node.getChildren()) {
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

		private OWLClass cls;
		private SemanticIndexRange range;
		private Set<Node> parents = new HashSet<Node>();
		private Set<Node> children = new HashSet<Node>();

		public Node(OWLClass cls) {
			this.cls = cls;
		}

		public void addParent(Node par) {
			parents.add(par);
		}

		public void addChild(Node chl) {
			children.add(chl);
		}

		public Set<Node> getChildren() {
			return children;
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
			if (range == null) {
				System.out.println("Range is null for " + cls);
				return cls.toString();
			} else
				return cls.toString() + range.toString();
		}

	}

}
