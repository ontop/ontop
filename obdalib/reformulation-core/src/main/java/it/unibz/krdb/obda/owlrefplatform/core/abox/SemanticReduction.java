package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGChain;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SemanticReduction {

	private static final Logger			log	= LoggerFactory.getLogger(SemanticReduction.class);
	private final DAG					isa;
	private final DAG					sigma;
	private final DAGChain				isaChain;
	private final DAGChain				sigmaChain;

	private final OBDADataFactory		predicateFactory;
	private final OntologyFactory	descFactory;

	public SemanticReduction(Ontology isat, Ontology sigmat) {
		this.isa = DAGConstructor.getISADAG(isat);
		this.sigma = DAGConstructor.getISADAG(sigmat);

		this.isaChain = new DAGChain(isa);
		this.sigmaChain = new DAGChain(sigma);

		predicateFactory = OBDADataFactoryImpl.getInstance();
		descFactory = new BasicDescriptionFactory();

	}
	
	public Ontology getReducedOntology() {
		Ontology reformulationOntology = new OntologyImpl(URI.create("http://it.unibz.krdb/obda/auxontology"));
		reformulationOntology.addAssertions(reduce());
		return reformulationOntology;
	}

	public List<Axiom> reduce() {
		log.debug("Starting semantic-reduction");
		List<Axiom> rv = new LinkedList<Axiom>();

		for (DAGNode node : isa.getClasses()) {

			// Ignore all edges from T
			if (node.getDescription().equals(DAG.thingConcept)) {
				continue;
			}

			for (DAGNode child : node.descendans) {

				if (!check_redundant(node, child)) {

					rv.add(new SubClassAxiomImpl((ClassDescription) child.getDescription(), (ClassDescription) node
							.getDescription()));
				}
			}
		}
		for (DAGNode node : isa.getRoles()) {

			for (DAGNode child : node.descendans) {
				if (!check_redundant_role(node, child)) {

					rv.add(new SubPropertyAxiomImpl((Property) child.getDescription(), (Property) node.getDescription()));
				}
			}
		}

		log.debug("Finished semantic-reduction {}", rv);
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

		PropertySomeRestriction existParentDesc = descFactory.getPropertySomeRestriction(parentDesc.getPredicate(),
				parentDesc.isInverse());
		PropertySomeRestriction existChildDesc = descFactory.getPropertySomeRestriction(childDesc.getPredicate(),
				childDesc.isInverse());

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
