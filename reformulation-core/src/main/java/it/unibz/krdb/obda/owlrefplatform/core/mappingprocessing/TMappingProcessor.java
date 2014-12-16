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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
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
import java.util.Map.Entry;

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
						
				boolean couldIgnore = false;
				
				Substitution toNewRule = newRule.computeHomomorphsim(currentRule);
				if ((toNewRule != null) && currentRule.isConditionsEmpty()) {
					if (newRule.databaseAtomsSize() < currentRule.databaseAtomsSize()) {
						couldIgnore = true;
						System.err.println("IGNORE " + newRule + " because of " + currentRule);
					}
					else {
						if (newRule.getHead().getFunctionSymbol().getName().endsWith("#Facility"))
							System.err.println("IGNORE " + newRule + " because of " + currentRule);
						// if the new mapping is redundant and there are no conditions then do not add anything		
						return;
					}
				}
				
				Substitution fromNewRule = currentRule.computeHomomorphsim(newRule);		
				if ((fromNewRule != null) && newRule.isConditionsEmpty()) {		
					// The existing query is more specific than the new query, so we
					// need to add the new query and remove the old	 
					mappingIterator.remove();
					if (newRule.getHead().getFunctionSymbol().getName().endsWith("#Facility"))
						System.err.println("REMOVE " + currentRule + " because of " + newRule);
					continue;
				} 
				
				if (couldIgnore) {
					if (newRule.getHead().getFunctionSymbol().getName().endsWith("#Facility"))
						System.err.println("FINAL IGNORE " + newRule + " because of " + currentRule);
					// if the new mapping is redundant and there are no conditions then do not add anything		
					return;					
				}
				
				if ((toNewRule != null) && (fromNewRule != null)) {
					// We found an equivalence, we will try to merge the conditions of
					// newRule into the currentRule
					//System.err.println("\n" + newRule + "\n v \n" + currentRule + "\n");
					
				 	// Here we can merge conditions of the new query with the one we have
					// just found
					// new map always has just one set of filters  !!
					List<Function> newconditions = TMappingRule.cloneList(newRule.getConditions().get(0));
					for (Function f : newconditions) 
						SubstitutionUtilities.applySubstitution(f, fromNewRule);

					List<List<Function>> existingconditions = currentRule.getConditions();
					for (List<Function> econd : existingconditions) {
						boolean found = true;
						for (Function nc : newconditions)
							if (!econd.contains(nc)) { 
								found = false;
								break;
							}	
						// if each of the new conditions is found among econd then the new map is redundant
						if (found)
							return;
					}
					
	                mappingIterator.remove();
	                
					if (newRule.getHead().getFunctionSymbol().getName().endsWith("#Facility"))
						System.err.println("MERGE " + newRule + " because of " + currentRule);

	                
					newRule = new TMappingRule(currentRule, newconditions);

					if (newRule.getHead().getFunctionSymbol().getName().endsWith("#Facility"))
						System.err.println("MERGE RESULT " + newRule);					
					
					break;
				}				
			}
			rules.add(newRule);
			//System.out.println("AFTER CQC:\n" + rules);
		}
	}

	// end of the inner class



	private static void getObjectTMappings(Map<Predicate, TMappingIndexEntry> mappingIndex, 
			Map<Predicate, List<TMappingRule>> originalMappings,
			EquivalencesDAG<ObjectPropertyExpression> dag, 
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

					List<TMappingRule> childmappings = originalMappings.get(childproperty.getPredicate());
					if (childmappings == null)
						continue;
					
					for (TMappingRule childmapping : childmappings) {
						
						List<Term> terms = childmapping.getHeadTerms();

						Function newMappingHead;
						if (!requiresInverse) {
							if (!full)
								continue;
							newMappingHead = fac.getFunction(currentPredicate, terms);
						} 
						else {
							newMappingHead = fac.getFunction(currentPredicate, terms.get(1), terms.get(0));
						}
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping);				
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
			Map<Predicate, List<TMappingRule>> originalMappings,
			EquivalencesDAG<DataPropertyExpression> dag, 
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
						List<TMappingRule> childmappings = originalMappings.get(childproperty.getPredicate());
						if (childmappings == null)
							continue;
						
						for (TMappingRule childmapping : childmappings) {
							
							List<Term> terms = childmapping.getHeadTerms();

							Function newMappingHead = fac.getFunction(currentPredicate, terms);
							TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping);				
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
					Function newhead = fac.getFunction(p, currentNodeMapping.getHeadTerms());
					
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

		Map<Predicate, List<TMappingRule>> originalMappingIndex = new HashMap<>();
		
		/***
		 * Creates an index of all mappings based on the predicate of the head of
		 * the mapping. The returned map can be used for fast access to the mapping
		 * list.
		 */
		
		//CQContainmentCheckUnderLIDs cqc0 = new CQContainmentCheckUnderLIDs(null);
		
		for (CQIE mapping : originalMappings) {			
			TMappingRule rule = new TMappingRule(mapping.getHead(), mapping.getBody(), cqc);
			Predicate ruleIndex = mapping.getHead().getFunctionSymbol();
			List<TMappingRule> ms = originalMappingIndex.get(ruleIndex);
			if (ms == null) {
				ms = new LinkedList<>();
				originalMappingIndex.put(ruleIndex, ms);
			}
			ms.add(rule);
						
			TMappingIndexEntry set = getMappings(mappingIndex, ruleIndex);
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

		getObjectTMappings(mappingIndex, originalMappingIndex, reasoner.getObjectPropertyDAG(), full);
		getDataTMappings(mappingIndex, originalMappingIndex, reasoner.getDataPropertyDAG(), full);

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
					
					List<TMappingRule> childmappings = originalMappingIndex.get(childPredicate);
					if (childmappings == null)
						continue;
					
					for (TMappingRule childmapping : childmappings) {
						
						List<Term> terms = childmapping.getHeadTerms();

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
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping);				
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
		
		List<CQIE> tmappingsProgram = new LinkedList<>();
		for (Entry<Predicate, TMappingIndexEntry> entry : mappingIndex.entrySet()) 
			for (TMappingRule mapping : entry.getValue()) {
				CQIE cq = mapping.asCQIE();
				//System.out.println(cq);
				tmappingsProgram.add(cq);
			}
				
		//System.out.println(tmappingsProgram);
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
