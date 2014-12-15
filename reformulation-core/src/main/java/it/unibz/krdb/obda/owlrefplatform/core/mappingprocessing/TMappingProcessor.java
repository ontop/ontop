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

import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.*;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TMappingProcessor {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private static class TMappingIndexEntry implements Iterable<TMappingRule> {
		private final List<TMappingRule> rules = new LinkedList<>();
	

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
						
				Substitution toNewRule = newRule.computeHomomorphsim(currentRule);
				if ((toNewRule != null) && currentRule.isConditionsEmpty()) {
					// if the new mapping is redundant and there are no conditions then do not add anything		
					return;
				}
				
				Substitution fromNewRule = currentRule.computeHomomorphsim(newRule);		
				if ((fromNewRule != null) && newRule.isConditionsEmpty()) {		
					// The existing query is more specific than the new query, so we
					// need to add the new query and remove the old	 
					mappingIterator.remove();
					continue;
				} 
				
				if ((toNewRule != null) && (fromNewRule != null)) {
					// We found an equivalence, we will try to merge the conditions of
					// newRule into the currentRule
					//System.err.println("\n" + newRule + "\n v \n" + currentRule + "\n");
					
				 	// Here we can merge conditions of the new query with the one we have
					// just found
					List<Function> newconditions = newRule.getConditions();
					for (Function f : newconditions)
						SubstitutionUtilities.applySubstitution(f, fromNewRule);

					List<Function> existingconditions = currentRule.getConditions();
					if (newconditions.equals(existingconditions)) {
						return;
					}	
					
	                mappingIterator.remove();

					newRule = new TMappingRule(currentRule, newconditions);
					break;
				}				
			}
			rules.add(newRule);
		}
	}

	// end of the inner class


	/***
	 * Given a mappings in {@link currentMapping}, this method will
	 * return a new mappings in which no constants appear in the body of
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
	 * @param currentMapping
	 * @return a new CQ that has been normalized in the way described above.
	 */
	private static CQIE normalizeConstants(CQIE currentMapping) {
		
		int freshVarCount = 0;
		List<Function> newBody = new LinkedList<Function>();
		for (Function currentAtom : currentMapping.getBody()) {
			if (!(currentAtom.getFunctionSymbol() instanceof BuiltinPredicate)) {
				Function clone = (Function)currentAtom.clone();
				for (int i = 0; i < clone.getTerms().size(); i++) {
					Term term = clone.getTerm(i);
					if (term instanceof Constant) {
						// Found a constant, replacing with a fresh variable
						// and adding the new equality atom.
						freshVarCount++;
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
		Function head = (Function)currentMapping.getHead().clone();		
		return fac.getCQIE(head, newBody);
	}

	private static void getObjectTMappings(Map<Predicate, TMappingIndexEntry> mappingIndex, 
			Map<Predicate, List<CQIE>> originalMappings,
			EquivalencesDAG<ObjectPropertyExpression> dag, 
			CQContainmentCheckUnderLIDs cqc,
			boolean full) {
		
		for (Equivalences<ObjectPropertyExpression> propertySet : dag) {

			ObjectPropertyExpression current = propertySet.getRepresentative();
			if (current.isInverse())
				continue;
			
			/* Getting the current node mappings */
			Predicate currentPredicate = current.getPredicate();
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);	

			for (Equivalences<ObjectPropertyExpression> descendants : dag.getSub(propertySet)) {
				for(ObjectPropertyExpression childproperty : descendants) {

					/*
					 * adding the mappings of the children as own mappings, the new
					 * mappings use the current predicate instead of the child's
					 * predicate and, if the child is inverse and the current is
					 * positive, it will also invert the terms in the head
					 */
					boolean requiresInverse = childproperty.isInverse();

					List<CQIE> childmappings = originalMappings.get(childproperty.getPredicate());
					if (childmappings == null)
						continue;
					
					for (CQIE childmapping : childmappings) {
						
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
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping.getBody(), cqc);				
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			/* Setting up mappings for the equivalent classes */
			for (ObjectPropertyExpression equivProperty : propertySet) {
					 
				Predicate p = equivProperty.getPredicate();

				// skip the property and its inverse (if it is symmetric)
				if (p.equals(current.getPredicate()))
					continue;
				
				TMappingIndexEntry equivalentPropertyMappings = getMappings(mappingIndex, p);
					
				for (TMappingRule currentNodeMapping : currentNodeMappings) {
					List<Term> terms = currentNodeMapping.getHeadTerms();
					
					Function newhead;
					if (!equivProperty.isInverse()) 
						newhead = fac.getFunction(p, terms);
					else 
						newhead = fac.getFunction(p, terms.get(1), terms.get(0));
					
					TMappingRule newrule = new TMappingRule(newhead, currentNodeMapping);				
					equivalentPropertyMappings.mergeMappingsWithCQC(newrule);
				}
			}
		} // Properties loop ended
		
	}
	private static void getDataTMappings(Map<Predicate, TMappingIndexEntry> mappingIndex, 
			Map<Predicate, List<CQIE>> originalMappings,
			EquivalencesDAG<DataPropertyExpression> dag, 
			CQContainmentCheckUnderLIDs cqc,
			boolean full) {
		
		for (Equivalences<DataPropertyExpression> propertySet : dag) {

			DataPropertyExpression current = propertySet.getRepresentative();
			
			/* Getting the current node mappings */
			Predicate currentPredicate = current.getPredicate();
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);	

			if (full) {
				for (Equivalences<DataPropertyExpression> descendants : dag.getSub(propertySet)) {
					for(DataPropertyExpression childproperty : descendants) {

						/*
						 * adding the mappings of the children as own mappings, the new
						 * mappings use the current predicate instead of the child's
						 * predicate and, if the child is inverse and the current is
						 * positive, it will also invert the terms in the head
						 */
						List<CQIE> childmappings = originalMappings.get(childproperty.getPredicate());
						if (childmappings == null)
							continue;
						
						for (CQIE childmapping : childmappings) {
							
							List<Term> terms = childmapping.getHead().getTerms();

							Function newMappingHead = fac.getFunction(currentPredicate, terms);
							TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping.getBody(), cqc);				
							currentNodeMappings.mergeMappingsWithCQC(newmapping);
						}
					}
				}
			}

			/* Setting up mappings for the equivalent classes */
			for (DataPropertyExpression equivProperty : propertySet) {
			
				 
				Predicate p = equivProperty.getPredicate();

				// skip the property and its inverse (if it is symmetric)
				if (p.equals(current.getPredicate()))
					continue;
				
				TMappingIndexEntry equivalentPropertyMappings = getMappings(mappingIndex, p);
					
				for (TMappingRule currentNodeMapping : currentNodeMappings) {
					List<Term> terms = currentNodeMapping.getHeadTerms();
					
					Function newhead = fac.getFunction(p, terms);
					
					TMappingRule newrule = new TMappingRule(newhead, currentNodeMapping);				
					equivalentPropertyMappings.mergeMappingsWithCQC(newrule);
				}
			}
		} // Properties loop ended
		
	}
	
	/**
	 * 
	 * @param originalMappings
	 * @param reasoner
	 * @param full (false for the Semantic Index)
	 * @return
	 */

	public static List<CQIE> getTMappings(List<CQIE> originalMappings, TBoxReasoner reasoner, boolean full, CQContainmentCheckUnderLIDs cqc) {

		Map<Predicate, TMappingIndexEntry> mappingIndex = new HashMap<>();

		Map<Predicate, List<CQIE>> originalMappingIndex = new HashMap<>();
		
		/***
		 * Creates an index of all mappings based on the predicate of the head of
		 * the mapping. The returned map can be used for fast access to the mapping
		 * list.
		 */
		
		for (CQIE mapping : originalMappings) {
			
			CQIE mapping1 = normalizeConstants(mapping);
			List<CQIE> ms = originalMappingIndex.get(mapping1.getHead().getFunctionSymbol());
			if (ms == null) {
				ms = new LinkedList<>();
				originalMappingIndex.put(mapping1.getHead().getFunctionSymbol(), ms);
			}
			ms.add(mapping1);
						
			TMappingIndexEntry set = getMappings(mappingIndex, mapping1.getHead().getFunctionSymbol());
			TMappingRule rule = new TMappingRule(mapping1.getHead(), mapping1.getBody(), cqc);
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

		getObjectTMappings(mappingIndex, originalMappingIndex, reasoner.getObjectPropertyDAG(), cqc, full);
		getDataTMappings(mappingIndex, originalMappingIndex, reasoner.getDataPropertyDAG(), cqc, full);

		/*
		 * Property t-mappings are done, we now continue with class t-mappings.
		 * Starting with the leafs.
		 */

		for (Equivalences<ClassExpression> classSet : reasoner.getClassDAG()) {

			if (!(classSet.getRepresentative() instanceof OClass)) 
				continue;

			OClass current = (OClass)classSet.getRepresentative();

			/* Getting the current node mappings */
			Predicate currentPredicate = current.getPredicate();
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);

			for (Equivalences<ClassExpression> descendants : reasoner.getClassDAG().getSub(classSet)) {
				for (ClassExpression childDescription : descendants) {

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
					else if (childDescription instanceof ObjectSomeValuesFrom) {
						ObjectPropertyExpression some = ((ObjectSomeValuesFrom) childDescription).getProperty();
						childPredicate = some.getPredicate();
						isClass = false;
						isInverse = some.isInverse();
					} 
					else {
						assert (childDescription instanceof DataSomeValuesFrom);
						DataPropertyExpression some = ((DataSomeValuesFrom) childDescription).getProperty();
						childPredicate = some.getPredicate();
						isClass = false;
						isInverse = false;  // can never be an inverse
					} 
					
					List<CQIE> childmappings = originalMappingIndex.get(childPredicate);
					if (childmappings == null)
						continue;
					
					for (CQIE childmapping : childmappings) {
						
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
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping.getBody(), cqc);				
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			
			/* Setting up mappings for the equivalent classes */
			for (ClassExpression equiv : classSet) {
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
		
		List<CQIE> tmappingsProgram = new LinkedList<CQIE>();
		for (Predicate key : mappingIndex.keySet()) {
			for (TMappingRule mapping : mappingIndex.get(key)) {
				CQIE cq = mapping.asCQIE();
				EQNormalizer.enforceEqualities(cq);
				tmappingsProgram.add(cq);
			}
		}
		
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
