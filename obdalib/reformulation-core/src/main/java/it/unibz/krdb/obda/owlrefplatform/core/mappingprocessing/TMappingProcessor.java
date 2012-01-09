package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMappingProcessor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6032320436478004010L;

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private final DAG dag;

	private final Ontology aboxDependencies;

	private final DAG pureIsa;

	private final static Logger log = LoggerFactory.getLogger(TMappingProcessor.class);

	public TMappingProcessor(Ontology tbox) {
		dag = DAGConstructor.getISADAG(tbox);
		dag.clean();
		pureIsa = DAGConstructor.filterPureISA(dag);
		aboxDependencies = DAGConstructor.getSigmaOntology(dag);

	}

	private Map<Predicate, Set<CQIE>> getMappingIndex(DatalogProgram mappings) {
		Map<Predicate, Set<CQIE>> mappingIndex = new HashMap<Predicate, Set<CQIE>>();

		for (CQIE mapping : mappings.getRules()) {
			Set<CQIE> set = mappingIndex.get(mapping.getHead().getPredicate());
			if (set == null) {
				set = new HashSet<CQIE>();
				mappingIndex.put(mapping.getHead().getPredicate(), set);
			}
			set.add(mapping);
		}

		return mappingIndex;
	}

	public Ontology getABoxDependencies() {
		return aboxDependencies;
	}

	public DatalogProgram getTMappings(DatalogProgram originalMappings) throws OBDAException {

		Map<Predicate, Set<CQIE>> mappingIndex = getMappingIndex(originalMappings);

		/*
		 * Processing mappings for all Properties
		 */

		/*
		 * We process the mappings for the descendents of the current node,
		 * adding them to the list of mappings of the current node as defined in
		 * the TMappings specification.
		 */

		/*
		 * We start with the property mappings, since class t-mappings require
		 * that these are already processed. We start with the leafs.
		 */

		for (DAGNode currentNode : dag.getRoles()) {
			/* setting up the queue for the next iteration */

			Property currentProperty = (Property) currentNode.getDescription();
			if (currentProperty.isInverse()) {
				/* we only create mappings for named properties */
				continue;
			}

			/* Getting the current node mappings */
			Predicate currentPredicate = currentProperty.getPredicate();
			Set<CQIE> currentNodeMappings = mappingIndex.get(currentPredicate);
			if (currentNodeMappings == null) {
				currentNodeMappings = new LinkedHashSet<CQIE>();
				mappingIndex.put(currentPredicate, currentNodeMappings);
			}

			for (DAGNode descendant : currentNode.getDescendants()) {
				/*
				 * adding the mappings of the children as own mappings, the new
				 * mappings use the current predicate instead of the childs
				 * predicate and, if the child is inverse and the current is
				 * positive, it will also invert the terms in the head
				 */
				Property childproperty = (Property) descendant.getDescription();
				List<CQIE> childMappings = originalMappings.getRules(childproperty.getPredicate());

				boolean requiresInverse = (currentProperty.isInverse() != childproperty.isInverse());

				for (CQIE childmapping : childMappings) {
					CQIE newmapping = null;
					Atom newMappingHead = null;
					Atom oldMappingHead = childmapping.getHead();
					if (!requiresInverse) {
						newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms());
					} else {
						Term term0 = oldMappingHead.getTerms().get(1);
						Term term1 = oldMappingHead.getTerms().get(0);
						newMappingHead = fac.getAtom(currentPredicate, term0, term1);
					}
					newmapping = fac.getCQIE(newMappingHead, childmapping.getBody());
					currentNodeMappings.add(newmapping);
				}

			}

			/* Setting up mappings for the equivalent classes */
			for (DAGNode equiv : currentNode.getEquivalents()) {
				Property equivProperty = (Property) equiv.getDescription();
				// if (equivProperty.isInverse())
				// continue;
				Predicate p = ((Property) equiv.getDescription()).getPredicate();
				Set<CQIE> equivalentPropertyMappings = mappingIndex.get(p);
				if (equivalentPropertyMappings == null) {
					equivalentPropertyMappings = new LinkedHashSet<CQIE>();
					mappingIndex.put(p, equivalentPropertyMappings);
				}

				for (CQIE currentNodeMapping : currentNodeMappings) {

					if (equivProperty.isInverse() == currentProperty.isInverse()) {
						Atom newhead = fac.getAtom(p, currentNodeMapping.getHead().getTerms());
						CQIE newmapping = fac.getCQIE(newhead, currentNodeMapping.getBody());
						equivalentPropertyMappings.add(newmapping);
					} else {
						Term term0 = currentNodeMapping.getHead().getTerms().get(1);
						Term term1 = currentNodeMapping.getHead().getTerms().get(0);
						Atom newhead = fac.getAtom(p, term0, term1);
						CQIE newmapping = fac.getCQIE(newhead, currentNodeMapping.getBody());
						equivalentPropertyMappings.add(newmapping);
					}
				}

			}
		} // Properties loop ended

		/*
		 * Property t-mappings are done, we now continue with class t-mappings.
		 * Starting with the leafs.
		 */

		for (DAGNode currentNode : dag.getClasses()) {

			if (!(currentNode.getDescription() instanceof OClass)) {
				/* we only create mappings for named concepts */
				continue;
			}

			OClass currentProperty = (OClass) currentNode.getDescription();

			/* Getting the current node mappings */
			Predicate currentPredicate = currentProperty.getPredicate();
			Set<CQIE> currentNodeMappings = mappingIndex.get(currentPredicate);
			if (currentNodeMappings == null) {
				currentNodeMappings = new LinkedHashSet<CQIE>();
				mappingIndex.put(currentPredicate, currentNodeMappings);
			}

			for (DAGNode descendant : currentNode.getDescendants()) {
				/*
				 * adding the mappings of the children as own mappings, the new
				 * mappings. There are three cases, when the child is a named
				 * class, or when it is an \exists P or \exists \inv P.
				 */
				ClassDescription childDescription = (ClassDescription) descendant.getDescription();
				Predicate childPredicate = null;
				boolean isClass = true;
				boolean isInverse = false;
				if (childDescription instanceof OClass) {
					childPredicate = ((OClass) childDescription).getPredicate();
				} else if (childDescription instanceof PropertySomeRestriction) {
					childPredicate = ((PropertySomeRestriction) childDescription).getPredicate();
					isInverse = ((PropertySomeRestriction) childDescription).isInverse();
					isClass = false;
				} else {
					throw new RuntimeException("Unknown type of node in DAG: " + descendant.getDescription());
				}

				List<CQIE> desendantMappings = originalMappings.getRules(childPredicate);

				for (CQIE childmapping : desendantMappings) {
					CQIE newmapping = null;
					Atom newMappingHead = null;
					Atom oldMappingHead = childmapping.getHead();

					if (isClass) {
						newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms());
					} else {
						if (!isInverse) {
							newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms().get(0));
						} else {
							newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms().get(1));
						}
					}
					newmapping = fac.getCQIE(newMappingHead, childmapping.getBody());
					currentNodeMappings.add(newmapping);

				}

				// TODO HACK! Remove when the API of DAG is clean, the following
				// is a hack due to a bug in the DAG API.
				/*
				 * Currently, the DAG.buildDescendants() call has an error with
				 * \exists R. If a node A has an equivalent node \exists R, and
				 * R has equivalent roles P,S, we will have that \exists P and
				 * \exists S do not appear as descendants of A altough they
				 * should. Hence, we have to collect them manually with the next
				 * code block.
				 */

				if (descendant.getDescription() instanceof PropertySomeRestriction) {
					for (DAGNode descendant2 : descendant.getEquivalents()) {
						childDescription = (ClassDescription) descendant2.getDescription();
						childPredicate = null;
						isClass = true;
						isInverse = false;
						if (childDescription instanceof OClass) {
							childPredicate = ((OClass) childDescription).getPredicate();
						} else if (childDescription instanceof PropertySomeRestriction) {
							childPredicate = ((PropertySomeRestriction) childDescription).getPredicate();
							isInverse = ((PropertySomeRestriction) childDescription).isInverse();
							isClass = false;
						} else {
							throw new RuntimeException("Unknown type of node in DAG: " + descendant2.getDescription());
						}

						desendantMappings = originalMappings.getRules(childPredicate);

						for (CQIE childmapping : desendantMappings) {
							CQIE newmapping = null;
							Atom newMappingHead = null;
							Atom oldMappingHead = childmapping.getHead();

							if (isClass) {
								newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms());
							} else {
								if (!isInverse) {
									newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms().get(0));
								} else {
									newMappingHead = fac.getAtom(currentPredicate, oldMappingHead.getTerms().get(1));
								}
							}
							newmapping = fac.getCQIE(newMappingHead, childmapping.getBody());
							currentNodeMappings.add(newmapping);

						}
					}
				}

			}
			/* Setting up mappings for the equivalent classes */
			for (DAGNode equiv : currentNode.getEquivalents()) {
				if (!(equiv.getDescription() instanceof OClass))
					continue;
				Predicate p = ((OClass) equiv.getDescription()).getPredicate();
				Set<CQIE> equivalentClassMappings = mappingIndex.get(p);

				if (equivalentClassMappings == null) {
					equivalentClassMappings = new LinkedHashSet<CQIE>();
					mappingIndex.put(p, equivalentClassMappings);
				}

				for (CQIE currentNodeMapping : currentNodeMappings) {
					Atom newhead = fac.getAtom(p, currentNodeMapping.getHead().getTerms());
					CQIE newmapping = fac.getCQIE(newhead, currentNodeMapping.getBody());
					equivalentClassMappings.add(newmapping);
				}

			}

		}
		DatalogProgram tmappingsProgram = fac.getDatalogProgram();
		for (Predicate key : mappingIndex.keySet()) {
			for (CQIE mapping : mappingIndex.get(key)) {
				tmappingsProgram.appendRule(mapping);
			}
		}

		return tmappingsProgram;
	}

	public List<DAGNode> getLeafs(Collection<DAGNode> nodes) {
		LinkedList<DAGNode> leafs = new LinkedList<DAGNode>();
		for (DAGNode node : nodes) {
			if (node.getChildren().isEmpty())
				leafs.add(node);
		}
		return leafs;
	}

}
