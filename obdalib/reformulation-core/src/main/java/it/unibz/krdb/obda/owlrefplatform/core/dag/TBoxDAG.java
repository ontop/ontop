package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class TBoxDAG {

	private static final OntologyFactory descFactory = new OntologyFactoryImpl();

	SimpleDirectedGraph<Description, DefaultEdge> dag = new SimpleDirectedGraph<Description, DefaultEdge>(DefaultEdge.class);

	public SimpleDirectedGraph<Description, DefaultEdge> getDag() {
		return dag;
	}

	public TBoxDAG(Ontology ontology) {

		for (Predicate conceptp : ontology.getConcepts()) {
			ClassDescription concept = descFactory.createClass(conceptp);
			dag.addVertex(concept);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		for (Predicate rolep : ontology.getRoles()) {
			Property role = descFactory.createProperty(rolep);
			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			dag.addVertex(role);
			dag.addVertex(roleInv);
			dag.addVertex(existsRole);
			dag.addVertex(existsRoleInv);
		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();
				dag.addVertex(child);
				dag.addVertex(parent);
				dag.addEdge(child, parent);
			} else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();

				// This adds the direct edge and the inverse, e.g., R ISA S and
				// R- ISA S-,
				// R- ISA S and R ISA S-
				dag.addVertex(child);
				dag.addVertex(parent);
				dag.addEdge(child, parent);
			}
		}
	}

}
