package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGChain;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGEdgeIterator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.Edge;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SigmaTBoxOptimizer {

	private static final Logger		log	= LoggerFactory.getLogger(SigmaTBoxOptimizer.class);
	private final DAG				isa;
	private final DAG				sigma;
	private final DAGChain			isaChain;
	private final DAGChain			sigmaChain;

	private final OBDADataFactory	predicateFactory;
	private final OntologyFactory	descFactory;

	public SigmaTBoxOptimizer(Ontology isat, Ontology sigmat) {
		this.isa = DAGConstructor.getISADAG(isat);
		this.isa.clean();
		this.sigma = DAGConstructor.getISADAG(sigmat);
		this.sigma.clean();

		this.isaChain = new DAGChain(isa);
		this.sigmaChain = new DAGChain(sigma);

		predicateFactory = OBDADataFactoryImpl.getInstance();
		descFactory = new OntologyFactoryImpl();

	}

	public Ontology getReducedOntology() {
		Ontology reformulationOntology = descFactory.createOntology(URI.create("http://it.unibz.krdb/obda/auxontology"));
		reformulationOntology.addAssertions(reduce());
		return reformulationOntology;
	}

	public List<Axiom> reduce() {
		log.debug("Starting semantic-reduction");
		List<Axiom> rv = new LinkedList<Axiom>();

		DAGEdgeIterator edgeIterator = new DAGEdgeIterator(isa);
		while (edgeIterator.hasNext()) {
			Edge edge = edgeIterator.next();
			if (edge.getLeft().getDescription() instanceof ClassDescription) {
				if (!check_redundant(edge.getRight(), edge.getLeft())) {
					rv.add(descFactory.createSubClassAxiom((ClassDescription) edge.getLeft().getDescription(), (ClassDescription) edge
							.getRight().getDescription()));
				}
			} else {
				if (!check_redundant_role(edge.getRight(), edge.getLeft())) {
					rv.add(descFactory.createSubPropertyAxiom((Property) edge.getLeft().getDescription(), (Property) edge.getRight()
							.getDescription()));
				}

			}
		}
		log.debug("Finished semantic-reduction.");
		return rv;
	}

	private boolean check_redundant_role(DAGNode parent, DAGNode child) {

		if (check_directly_redundant_role(parent, child))
			return true;
		else {
			log.debug("Not directly redundant role {} {}", parent, child);
			for (DAGNode child_prime : parent.getChildren()) {
				if (!child_prime.equals(child) && check_directly_redundant_role(child_prime, child)
						&& !check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		log.debug("Not redundant role {} {}", parent, child);

		return false;
	}

	private boolean check_directly_redundant_role(DAGNode parent, DAGNode child) {
		Property parentDesc = (Property) parent.getDescription();
		Property childDesc = (Property) child.getDescription();

		PropertySomeRestriction existParentDesc = descFactory.getPropertySomeRestriction(parentDesc.getPredicate(), parentDesc.isInverse());
		PropertySomeRestriction existChildDesc = descFactory.getPropertySomeRestriction(childDesc.getPredicate(), childDesc.isInverse());

		DAGNode exists_parent = isa.getClassNode(existParentDesc);
		DAGNode exists_child = isa.getClassNode(existChildDesc);

		return check_directly_redundant(parent, child) && check_directly_redundant(exists_parent, exists_child);
	}

	private boolean check_redundant(DAGNode parent, DAGNode child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (DAGNode child_prime : parent.getChildren()) {
				if (!child_prime.equals(child) && check_directly_redundant(child_prime, child) && !check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean check_directly_redundant(DAGNode parent, DAGNode child) {
		DAGNode sp = sigmaChain.chain().get(parent.getDescription());
		DAGNode sc = sigmaChain.chain().get(child.getDescription());
		DAGNode tc = isaChain.chain().get(child.getDescription());

		if (sp == null || sc == null || tc == null) {
			return false;
		}

		return (sp.getChildren().contains(sc) && sc.descendans.containsAll(tc.descendans));

	}

}
