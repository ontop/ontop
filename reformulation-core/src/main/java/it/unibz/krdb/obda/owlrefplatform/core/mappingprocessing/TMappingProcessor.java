package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.config.tmappings.types.SimplePredicate;
import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TMappingProcessor {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private static class TMappingIndexEntry implements Iterable<TMappingRule> {
		private final Set<TMappingRule> rules = new HashSet<TMappingRule>();
	
	/** List of predicates that need to be excluded from T-Mappings **/
	List<SimplePredicate> excludeFromTMappings;

	@Override
		public Iterator<TMappingRule> iterator() {
			return rules.iterator();
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
		 * If no such m is found, then this method simply adds newmapping to
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
	 * @param newmapping
	 *            The new mapping for A/P
	 */
		public void mergeMappingsWithCQC(TMappingRule newRule) {
		
			// Facts are just added
			if (newRule.isFact()) {
				rules.add(newRule);
			return;
		}
		
			Iterator<TMappingRule> mappingIterator = rules.iterator();
		while (mappingIterator.hasNext()) {

				TMappingRule currentRule = mappingIterator.next(); 
				
				if (!newRule.isContainedIn(currentRule))
				continue;

				if (!currentRule.isContainedIn(newRule))
				continue;

			/*
			 * We found an equivalence, we will try to merge the conditions of
			 * newmapping into the currentMapping.
			 */
				if (!newRule.isConditionsEmpty() && currentRule.isConditionsEmpty()) {
				/*
				 * There is a containment and there is no need to add the new
				 * mapping since there there is no extra conditions in the new
				 * mapping
				 */
				return;
				} else if (newRule.isConditionsEmpty() && !currentRule.isConditionsEmpty()) {
				/*
				 * The existing query is more specific than the new query, so we
				 * need to add the new query and remove the old
				 */
				mappingIterator.remove();
				break;
				} else if (newRule.isConditionsEmpty() && currentRule.isConditionsEmpty()) {
				/*
				 * There are no conditions, and the new mapping is redundant, do not add anything
				 */
				return;
			} else {
				/*
				 * Here we can merge conditions of the new query with the one we
				 * just found.
				 */
					Function newconditions = newRule.getMergedConditions();
					Function existingconditions = currentRule.getMergedConditions();
				
	                // we do not add a new mapping if the conditions are exactly the same
	                if (existingconditions.equals(newconditions)) {
	                    continue;
				}			
				mappingIterator.remove();

					// ROMAN: i do not quite understand the code (in particular, the 1 atom in the body)
					//        (but leave this fragment with minimal changes)
					CQIE newmapping = currentRule.getStripped();
					Map<Variable,Term> mgu = null;
					if (newmapping.getBody().size() == 1) {
						mgu = Unifier.getMGU(newmapping.getBody().get(0), newRule.getStripped().getBody().get(0));
					}			
					
					Function orAtom = fac.getFunctionOR(existingconditions, newconditions);
					newmapping.getBody().add(orAtom);
				
				if (mgu != null) {
					newmapping = Unifier.applyUnifier(newmapping, mgu);
				}
					newRule = new TMappingRule(newmapping.getHead(), newmapping.getBody());
				break;
			}
		}
			rules.add(newRule);
	}
	}



	/***
	 * Given a set of mappings in {@link originalMappings}, this method will
	 * return a new set of mappings in which no constants appear in the body of
	 * database predicates. This is done by replacing the constant occurrence
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
	private static DatalogProgram normalizeConstants(DatalogProgram originalMappings) {
		DatalogProgram newProgram = fac.getDatalogProgram();
		newProgram.setQueryModifiers(originalMappings.getQueryModifiers());
		for (CQIE currentMapping : originalMappings.getRules()) {
			int freshVarCount = 0;

			Function head = (Function)currentMapping.getHead().clone();
			List<Function> newBody = new LinkedList<Function>();
			for (Function currentAtom : currentMapping.getBody()) {
				if (!(currentAtom.getPredicate() instanceof BuiltinPredicate)) {
					Function clone = (Function)currentAtom.clone();
					for (int i = 0; i < clone.getTerms().size(); i++) {
						Term term = clone.getTerm(i);
						if (term instanceof Constant) {
							/*
							 * Found a constant, replacing with a fresh variable
							 * and adding the new equality atom.
							 */
							freshVarCount += 1;
							Variable freshVariable = fac.getVariable("?FreshVar" + freshVarCount);
							newBody.add(fac.getFunctionEQ(freshVariable, term));
							clone.setTerm(i, freshVariable);
						}
					}
					newBody.add(clone);
				} else {
					newBody.add((Function)currentAtom.clone());
				}
			}
			CQIE normalizedMapping = fac.getCQIE(head, newBody);
			newProgram.appendRule(normalizedMapping);
		}
		return newProgram;
	}

	/**
	 * 
	 * @param originalMappings
	 * @param reasoner
	 * @param full (false for the Semantic Index)
	 * @return
	 */

	public static DatalogProgram getTMappings(DatalogProgram originalMappings, TBoxReasoner reasoner, boolean full) {

		/*
		 * Normalizing constants
		 */
		originalMappings = normalizeConstants(originalMappings);
		
		
		Map<Predicate, TMappingIndexEntry> mappingIndex = new HashMap<Predicate, TMappingIndexEntry>();

		/***
		 * Creates an index of all mappings based on the predicate of the head of
		 * the mapping. The returned map can be used for fast access to the mapping
		 * list.
		 */
		
		for (CQIE mapping : originalMappings.getRules()) {
			TMappingIndexEntry set = getMappings(mappingIndex, (mapping.getHead().getPredicate()));
			TMappingRule rule = new TMappingRule(mapping.getHead(), mapping.getBody());
			set.mergeMappingsWithCQC(rule);
		}
		

		/*
		 * We start with the property mappings, since class t-mappings require
		 * that these are already processed. 
		 * Processing mappings for all Properties
		 *
		 * We process the mappings for the descendants of the current node,
		 * adding them to the list of mappings of the current node as defined in
		 * the TMappings specification.
		 */

		for (Equivalences<Property> propertySet : reasoner.getProperties()) {

			Property current = propertySet.getRepresentative();
			if (current.isInverse())
				continue;
			
			// Davide> Let's skip?
			SimplePredicate curSimp = new SimplePredicate(current.getPredicate());
			
			if( excludeFromTMappings.contains(curSimp) ){
				// Skip this guy
				continue;
			}				

			/* Getting the current node mappings */
			Predicate currentPredicate = current.getPredicate();
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);	

			for (Equivalences<Property> descendants : reasoner.getProperties().getSub(propertySet)) {
				for(Property childproperty : descendants) {

					/*
					 * adding the mappings of the children as own mappings, the new
					 * mappings use the current predicate instead of the child's
					 * predicate and, if the child is inverse and the current is
					 * positive, it will also invert the terms in the head
					 */
					boolean requiresInverse = (current.isInverse() != childproperty.isInverse());

					for (CQIE childmapping : originalMappings.getRules(childproperty.getPredicate())) {

						List<Term> terms = childmapping.getHead().getTerms();

						Function newMappingHead;
						if (!requiresInverse) {
							if (!full)
								continue;
							newMappingHead = fac.getFunction(currentPredicate, terms);
						} 
						else {
							newMappingHead = fac.getFunction(currentPredicate, terms.get(1), terms.get(0));
						}
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping.getBody());				
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			/* Setting up mappings for the equivalent classes */
			for (Property equivProperty : propertySet) {
			
				 
				Predicate p = equivProperty.getPredicate();

				// skip the property and its inverse (if it is symmetric)
				if (p.equals(current.getPredicate()))
					continue;
				
				TMappingIndexEntry equivalentPropertyMappings = getMappings(mappingIndex, p);
					
				for (TMappingRule currentNodeMapping : currentNodeMappings) {
					List<Term> terms = currentNodeMapping.getHeadTerms();
					
					Function newhead;
					if (equivProperty.isInverse() == current.isInverse()) 
						newhead = fac.getFunction(p, terms);
					else 
						newhead = fac.getFunction(p, terms.get(1), terms.get(0));
					
					TMappingRule newrule = new TMappingRule(newhead, currentNodeMapping);				
					equivalentPropertyMappings.mergeMappingsWithCQC(newrule);
				}
			}
		} // Properties loop ended

		/*
		 * Property t-mappings are done, we now continue with class t-mappings.
		 * Starting with the leafs.
		 */

		for (Equivalences<BasicClassDescription> classSet : reasoner.getClasses()) {

			if (!(classSet.getRepresentative() instanceof OClass)) 
				continue;

			OClass current = (OClass)classSet.getRepresentative();

			// Davide> Let's skip?
			SimplePredicate curSimp = new SimplePredicate(current.getPredicate());
			
			if( excludeFromTMappings.contains(curSimp) ){
				// Skip this guy
				continue;
			}	

			/* Getting the current node mappings */
			Predicate currentPredicate = current.getPredicate();
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);

			for (Equivalences<BasicClassDescription> descendants : reasoner.getClasses().getSub(classSet)) {
				for (BasicClassDescription childDescription : descendants) {

					/* adding the mappings of the children as own mappings, the new
					 * mappings. There are three cases, when the child is a named
					 * class, or when it is an \exists P or \exists \inv P. 
					 */
					
					boolean isClass, isInverse;

					Predicate childPredicate;					
					if (childDescription instanceof OClass) {
						if (!full)
							continue;
						childPredicate = ((OClass) childDescription).getPredicate();
						isClass = true;
						isInverse = false;
					} 
					else if (childDescription instanceof PropertySomeRestriction) {
						PropertySomeRestriction some = (PropertySomeRestriction) childDescription;
						childPredicate = some.getPredicate();
						isClass = false;
						isInverse = some.isInverse();
					} 
					else 
						throw new RuntimeException("Unknown type of node in DAG: " + childDescription);
					
					for (CQIE childmapping : originalMappings.getRules(childPredicate)) {
						List<Term> terms = childmapping.getHead().getTerms();

						Function newMappingHead;
						if (isClass) {
							newMappingHead = fac.getFunction(currentPredicate, terms);
						} 
						else {
							if (!isInverse) 
								newMappingHead = fac.getFunction(currentPredicate, terms.get(0));
							else 
								newMappingHead = fac.getFunction(currentPredicate, terms.get(1));
						}
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping.getBody());				
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			
			/* Setting up mappings for the equivalent classes */
			for (BasicClassDescription equiv : classSet) {
				if (!(equiv instanceof OClass) || equiv.equals(current))
					continue;
				
				Predicate p = ((OClass) equiv).getPredicate();
				TMappingIndexEntry equivalentClassMappings = getMappings(mappingIndex, p);	
				
				for (TMappingRule currentNodeMapping : currentNodeMappings) {
					Function newhead = fac.getFunction(p, currentNodeMapping.getHeadTerms());

					TMappingRule newrule = new TMappingRule(newhead, currentNodeMapping);				
					equivalentClassMappings.mergeMappingsWithCQC(newrule);
				}
			}
		}
		
		DatalogProgram tmappingsProgram = fac.getDatalogProgram();
		for (Predicate key : mappingIndex.keySet()) {
			for (TMappingRule mapping : mappingIndex.get(key)) 
				tmappingsProgram.appendRule(mapping.asCQIE());
			}
		
		tmappingsProgram = DatalogNormalizer.enforceEqualities(tmappingsProgram);

		return tmappingsProgram;
	}

	
	private static TMappingIndexEntry getMappings(Map<Predicate, TMappingIndexEntry> mappingIndex, Predicate current) {
		
		TMappingIndexEntry currentMappings = mappingIndex.get(current);	
		if (currentMappings == null) {
			currentMappings = new TMappingIndexEntry();
			mappingIndex.put(current, currentMappings);
		}
		return currentMappings;
	}


	

}
