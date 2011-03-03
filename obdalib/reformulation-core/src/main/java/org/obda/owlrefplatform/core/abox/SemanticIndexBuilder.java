package org.obda.owlrefplatform.core.abox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to create, serialize and deserialize SemanticIndex
 * 
 * @author Sergejs Pugacs
 * 
 */
public class SemanticIndexBuilder {
	private static Logger log = LoggerFactory
			.getLogger(SemanticIndexBuilder.class);

	/**
	 * @param ontologies
	 * 
	 * @return dictionary of OWL entity and its index range
	 */
	public static Map<OWLEntity, SemanticIndexRange> build(
			HashSet<OWLOntology> ontologies) {
		HashMap<OWLEntity, SemanticIndexRange> index_range = new HashMap<OWLEntity, SemanticIndexRange>();

		DAG dag = new DAG();

		Iterator<OWLOntology> ontologyIterator = ontologies.iterator();

		while (ontologyIterator.hasNext()) {
			OWLOntology onto = ontologyIterator.next();
			log.debug("Generating SemanticIndex for ontology: " + onto);

			// subclass relations = edges in the DAG
			Set<OWLAxiom> nodes = onto.getAxioms();
			for (OWLAxiom ax : nodes) {
				if (ax instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom edge = (OWLSubClassAxiom) ax;
					if (!edge.getSubClass().isAnonymous()
							&& !edge.getSuperClass().isAnonymous()
							&& !edge.getSubClass().isOWLNothing()
							&& !edge.getSuperClass().isOWLThing()) {
						OWLClass subClass = edge.getSubClass().asOWLClass();
						OWLClass superClass = edge.getSuperClass().asOWLClass();
						dag.addEdge(subClass, superClass);
					}
				} else if (ax instanceof OWLSubPropertyAxiom) {
					OWLSubPropertyAxiom<OWLObjectProperty> edge = (OWLSubPropertyAxiom<OWLObjectProperty>) ax;

					if (!edge.getSubProperty().isAnonymous()
							&& !edge.getSuperProperty().isAnonymous()) {

						OWLObjectProperty subProperty = edge.getSubProperty();
						OWLObjectProperty superProperty = edge
								.getSuperProperty();
						dag.addEdge(subProperty, superProperty);
					}
				}
			}
		}
		dag.index();
		dag.buildRange();

		log.debug(dag.toString());

		return dag.getIndex();
	}
}
