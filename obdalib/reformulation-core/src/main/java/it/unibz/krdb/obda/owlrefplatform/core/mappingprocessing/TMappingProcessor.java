package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	/***
	 * Creates an index of all mappings based on the predicate of the head of
	 * the mapping. The returned map can be used for fast access to the mapping
	 * list.
	 * 
	 * @param mappings
	 *            A set of mapping given as CQIEs
	 * @return A map from a predicate to the list of mappings that have that
	 *         predicate in the head atom.
	 */
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

	/***
	 * 
	 * This is an optimization mechanism that allows T-mappings to produce a
	 * smaller number of mappings, and hence, the unfolding will be able to
	 * produce fewer queries.
	 * 
	 * Given a set of mappings for a class/property A in {@link currentMappings}
	 * , this method tries to add a the data coming from a new mapping for A in
	 * an optimal way, that is, this method will attempt to include the content
	 * of coming from {@link newmapping} by modifying an existing mapping
	 * instead of adding a new mapping.
	 * 
	 * <p/>
	 * 
	 * To do this, this method will strip {@link newmapping} from any
	 * (in)equality conditions that hold over the variables of the query,
	 * leaving only the raw body. Then it will look for another "stripped"
	 * mapping <bold>m</bold> in {@link currentMappings} such that m is
	 * equivalent to stripped(newmapping). If such a m is found, this method
	 * will add the extra semantics of newmapping to "m" by appending
	 * newmapping's conditions into an OR atom, together with the existing
	 * conditions of m.
	 * 
	 * </p>
	 * If no such m is found, then this method simply adds newmmapping to
	 * currentMappings.
	 * 
	 * 
	 * <p/>
	 * For example. If new mapping is equal to
	 * <p/>
	 * 
	 * S(x,z) :- R(x,y,z), y = 2
	 * 
	 * <p/>
	 * and there exists a mapping m
	 * <p/>
	 * S(x,z) :- R(x,y,z), y > 7
	 * 
	 * This method would modify 'm' as follows:
	 * 
	 * <p/>
	 * S(x,z) :- R(x,y,z), OR(y > 7, y = 2)
	 * 
	 * <p/>
	 * 
	 * @param currentMappings
	 *            The set of existing mappings for A/P
	 * @param newmapping
	 *            The new mapping for A/P
	 */
	public void mergeMappingsWithCQC(Set<CQIE> currentMappings, CQIE newmapping) {
		List<Atom> strippedNewConditions = new LinkedList<Atom>();
		CQIE strippedNewMapping = getStrippedMapping(newmapping, strippedNewConditions);

		CQCUtilities cqc1 = new CQCUtilities(strippedNewMapping);

		Iterator<CQIE> mappingIterator = currentMappings.iterator();
		
		Atom head = newmapping.getHead();
//		System.out.println(newmapping.getHead().getPredicate().getName());
//		if (head.getPredicate().getName().toString().equals("http://www.semanticweb.org/ontologies/2011/3/LUCADAOntology.owl#hasPreHistology"))
//			System.out.println("Here");
		while (mappingIterator.hasNext()) {
			CQIE currentMapping = mappingIterator.next();
			List<Atom> strippedExistingConditions = new LinkedList<Atom>();
			CQIE strippedCurrentMapping = getStrippedMapping(currentMapping, strippedExistingConditions);

			if (!cqc1.isContainedIn(strippedCurrentMapping))
				continue;

			CQCUtilities cqc = new CQCUtilities(strippedCurrentMapping);
			if (!cqc.isContainedIn(strippedNewMapping))
				continue;

			/*
			 * We found an equivalence, we will try to merge the conditions of
			 * newmapping into the currentMapping.
			 */
			if (strippedNewConditions.size() != 0 && strippedExistingConditions.size() == 0) {
				/*
				 * There is a containment and there is no need to add the new
				 * mapping since there there is no extra conditions in the new
				 * mapping
				 */
				return;
			} else if (strippedNewConditions.size() == 0 && strippedExistingConditions.size() != 0) {
				/*
				 * The existing query is more specific than the new query, so we
				 * need to add the new query and remove the old
				 */
				mappingIterator.remove();
				break;
			} else if (strippedNewConditions.size() == 0 && strippedExistingConditions.size() == 0) {
				/*
				 * There are no conditions, and the new mapping is redundant, do not add anything
				 */
				return;
			} else {
				/*
				 * Here we can merge conditions of the new query with the one we
				 * just found.
				 */
				Atom newconditions = mergeConditions(strippedNewConditions);
				Atom existingconditions = mergeConditions(strippedExistingConditions);
				NewLiteral newconditionsTerm = fac.getFunctionalTerm(newconditions.getPredicate(), newconditions.getTerms());
				NewLiteral existingconditionsTerm = fac.getFunctionalTerm(existingconditions.getPredicate(), existingconditions.getTerms());
				Atom orAtom = fac.getORAtom(existingconditionsTerm, newconditionsTerm);
				strippedCurrentMapping.getBody().add(orAtom);
				mappingIterator.remove();
				newmapping = strippedCurrentMapping;
				break;
			}
		}
		currentMappings.add(newmapping);
	}

	/***
	 * Takes a conjunctive boolean atoms and returns one single atom
	 * representing the conjunction (it might be a single atom if
	 * conditions.size() == 1.
	 * 
	 * @param conditions
	 * @return
	 */
	private Atom mergeConditions(List<Atom> conditions) {
		if (conditions.size() == 1)
			return conditions.get(0);
		Atom atom0 = conditions.remove(0);
		Atom atom1 = conditions.remove(0);
		NewLiteral f0 = fac.getFunctionalTerm(atom0.getPredicate(), atom0.getTerms());
		NewLiteral f1 = fac.getFunctionalTerm(atom1.getPredicate(), atom1.getTerms());
		Atom nestedAnd = fac.getANDAtom(f0, f1);
		while (conditions.size() != 0) {
			Atom condition = conditions.remove(0);
			NewLiteral term0 = nestedAnd.getTerm(1);
			NewLiteral term1 = fac.getFunctionalTerm(condition.getPredicate(), condition.getTerms());
			NewLiteral newAND = fac.getANDFunction(term0, term1);
			nestedAnd.setTerm(1, newAND);
		}
		return nestedAnd;
	}

	/***
	 * Returns a new CQIE obtained from {@link mapping} where all builtin
	 * predicates (conditionals) have been removed. The removed conditional
	 * atoms will be added to the {@link strippedConditionsHolder} list.
	 * 
	 * <p>
	 * This method is used by mergeMappingsWithCQC to test for containtment of
	 * the stripped bodies.
	 * 
	 * @param mapping
	 * @param strippedConditionsHolder
	 * @return
	 */
	private CQIE getStrippedMapping(CQIE mapping, List<Atom> strippedConditionsHolder) {
		strippedConditionsHolder.clear();
		Atom head = mapping.getHead().clone();
		List<Atom> body = mapping.getBody();
		List<Atom> newbody = new LinkedList<Atom>();
		for (int i = 0; i < body.size(); i++) {
			Atom atom = body.get(i);
			Atom clone = atom.clone();
			if (clone.getPredicate() instanceof BuiltinPredicate) {
				strippedConditionsHolder.add(clone);
			} else {
				newbody.add(clone);
			}
		}
		return fac.getCQIE(head, newbody);
	}

	/***
	 * Given a set of mappings in {@link originalMappings}, this method will
	 * return a new set of mappings in which no constants appear in the body of
	 * database predicates. This is done by replacing the constant occurence
	 * with a fresh variable, and adding a new equality condition to the body of
	 * the mapping.
	 * <p/>
	 * 
	 * For example, let the mapping m be
	 * <p/>
	 * A(x) :- T(x,y,22)
	 * 
	 * <p>
	 * Then this method will replace m by the mapping m'
	 * <p>
	 * A(x) :- T(x,y,z), EQ(z,22)
	 * 
	 * @param originalMappings
	 * @return A new DatalogProgram that has been normalized in the way
	 *         described above.
	 */
	public DatalogProgram normalizeConstants(DatalogProgram originalMappings) {
		DatalogProgram newProgram = fac.getDatalogProgram();
		newProgram.setQueryModifiers(originalMappings.getQueryModifiers());
		for (CQIE currentMapping : originalMappings.getRules()) {
			int freshVarCount = 0;

			Atom head = currentMapping.getHead().clone();
			List<Atom> newBody = new LinkedList<Atom>();
			for (Atom currentAtom : currentMapping.getBody()) {
				if (!(currentAtom.getPredicate() instanceof BuiltinPredicate)) {
					Atom clone = currentAtom.clone();
					for (int i = 0; i < clone.getTerms().size(); i++) {
						NewLiteral term = clone.getTerm(i);
						if (term instanceof Constant) {
							/*
							 * Found a constant, replacing with a fresh variable
							 * and adding the new equality atom.
							 */
							freshVarCount += 1;
							Variable freshVariable = fac.getVariable("?FreshVar" + freshVarCount);
							newBody.add(fac.getEQAtom(freshVariable, term));
							clone.setTerm(i, freshVariable);
						}
					}
					newBody.add(clone);
				} else {
					newBody.add(currentAtom.clone());
				}
			}
			CQIE normalizedMapping = fac.getCQIE(head, newBody);
			newProgram.appendRule(normalizedMapping);
		}
		return newProgram;
	}

	public DatalogProgram getTMappings(DatalogProgram originalMappings) throws OBDAException {

		/*
		 * Normalizing constants
		 */
		originalMappings = normalizeConstants(originalMappings);
//		System.out.println(originalMappings.toString());

		Map<Predicate, Set<CQIE>> mappingIndex = getMappingIndex(originalMappings);
		
		/*
		 * Merge original mappings that have similar source query.
		 */
		optimizeMappingProgram(mappingIndex);
		
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
						NewLiteral term0 = oldMappingHead.getTerms().get(1);
						NewLiteral term1 = oldMappingHead.getTerms().get(0);
						newMappingHead = fac.getAtom(currentPredicate, term0, term1);
					}
					newmapping = fac.getCQIE(newMappingHead, childmapping.getBody());

					mergeMappingsWithCQC(currentNodeMappings, newmapping);
					// currentNodeMappings.add(newmapping);
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
						mergeMappingsWithCQC(equivalentPropertyMappings, newmapping);
						// equivalentPropertyMappings.add(newmapping);
					} else {
						NewLiteral term0 = currentNodeMapping.getHead().getTerms().get(1);
						NewLiteral term1 = currentNodeMapping.getHead().getTerms().get(0);
						Atom newhead = fac.getAtom(p, term0, term1);
						CQIE newmapping = fac.getCQIE(newhead, currentNodeMapping.getBody());
						mergeMappingsWithCQC(equivalentPropertyMappings, newmapping);
						// equivalentPropertyMappings.add(newmapping);
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
					
					mergeMappingsWithCQC(currentNodeMappings, newmapping);
//					currentNodeMappings.add(newmapping);

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
							mergeMappingsWithCQC(currentNodeMappings, newmapping);
//							currentNodeMappings.add(newmapping);
							

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
					mergeMappingsWithCQC(equivalentClassMappings, newmapping);
//					equivalentClassMappings.add(newmapping);
					
				}

			}

		}
		DatalogProgram tmappingsProgram = fac.getDatalogProgram();
		for (Predicate key : mappingIndex.keySet()) {
			for (CQIE mapping : mappingIndex.get(key)) {
				tmappingsProgram.appendRule(mapping);
			}
		}

//		System.out.println(tmappingsProgram);
		return tmappingsProgram;
	}

	private void optimizeMappingProgram(Map<Predicate, Set<CQIE>> mappingIndex) {
		for (Predicate p : mappingIndex.keySet()) {
			Set<CQIE> similarMappings = mappingIndex.get(p);
			Set<CQIE> result = new HashSet<CQIE>();
			Iterator<CQIE> iterSimilarMappings = similarMappings.iterator();
			while (iterSimilarMappings.hasNext()) {
				CQIE candidate = iterSimilarMappings.next();
				iterSimilarMappings.remove();
				mergeMappingsWithCQC(result, candidate);
			}
			mappingIndex.put(p, result);
		}
	}

	private List<DAGNode> getLeafs(Collection<DAGNode> nodes) {
		LinkedList<DAGNode> leafs = new LinkedList<DAGNode>();
		for (DAGNode node : nodes) {
			if (node.getChildren().isEmpty())
				leafs.add(node);
		}
		return leafs;
	}

}
