package it.unibz.inf.ontop.spec.mapping.transformer.impl;

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

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.BuiltinPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.ontology.ClassExpression;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.DataSomeValuesFrom;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectSomeValuesFrom;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.EquivalencesDAG;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TMappingProcessor {

	// TODO: the implementation of EXCLUDE ignores equivalent classes / properties

	private class TMappingIndexEntry implements Iterable<TMappingRule> {
		private final List<TMappingRule> rules = new LinkedList<>();

		public TMappingIndexEntry copyOf(Predicate newPredicate) {
			TMappingIndexEntry copy = new TMappingIndexEntry();
			for (TMappingRule rule : rules) {
				Function newHead = termFactory.getFunction(newPredicate, rule.getHeadTerms());
				TMappingRule newRule = new TMappingRule(newHead, rule, datalogFactory, termFactory, eqNormalizer);
				copy.rules.add(newRule);
			}
			return copy;
		}

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
		 * Given a set of mappings for a class/property A in currentMappings
		 * , this method tries to add a the data coming from a new mapping for A in
		 * an optimal way, that is, this method will attempt to include the content
		 * of coming from newmapping by modifying an existing mapping
		 * instead of adding a new mapping.
		 * 
		 * <p/>
		 * 
		 * To do this, this method will strip newmapping from any
		 * (in)equality conditions that hold over the variables of the query,
		 * leaving only the raw body. Then it will look for another "stripped"
		 * mapping <bold>m</bold> in currentMappings such that m is
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
		 */
		public void mergeMappingsWithCQC(TMappingRule newRule) {
			
			// Facts are just added
			if (newRule.isFact()) {
				rules.add(newRule);
				return;
			}
		
			if (noCQC) {
				for (TMappingRule r : rules)
					if (r.equals(newRule))
						return;
				
				rules.add(newRule);
				return;
			}
			
			Iterator<TMappingRule> mappingIterator = rules.iterator();
			while (mappingIterator.hasNext()) {

				TMappingRule currentRule = mappingIterator.next(); 
				// ROMAN (14 Oct 2015): quick fix, but one has to be more careful with variables in filters
				if (currentRule.equals(newRule))
					return;
						
				boolean couldIgnore = false;
				
				Substitution toNewRule = newRule.computeHomomorphsim(currentRule);
				if ((toNewRule != null) && checkConditions(newRule, currentRule, toNewRule)) {
					if (newRule.databaseAtomsSize() < currentRule.databaseAtomsSize()) {
						couldIgnore = true;
					}
					else {
						// if the new mapping is redundant and there are no conditions then do not add anything		
						return;
					}
				}
				
				Substitution fromNewRule = currentRule.computeHomomorphsim(newRule);		
				if ((fromNewRule != null) && checkConditions(currentRule, newRule, fromNewRule)) {		
					// The existing query is more specific than the new query, so we
					// need to add the new query and remove the old	 
					mappingIterator.remove();
					continue;
				} 
				
				if (couldIgnore) {
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
						substitutionUtilities.applySubstitution(f, fromNewRule);

					List<List<Function>> existingconditions = currentRule.getConditions();
					List<List<Function>> filterAtoms = new ArrayList<>(existingconditions.size() + 1);
					
					for (List<Function> econd : existingconditions) {
						boolean found2 = true;
						for (Function ec : econd) 
							if (!newconditions.contains(ec)) {
								found2 = false;
								break;
							}
						// if each of the existing conditions is found then the new condition is redundant
						if (found2)
							return;
						
						boolean found = true;
						for (Function nc : newconditions)
							if (!econd.contains(nc)) { 
								found = false;
								break;
							}	
						// if each of the new conditions is found among econd then the old condition is redundant
						if (found) {
							//System.err.println(econd + " contains " + newconditions);
						}
						else
							filterAtoms.add(TMappingRule.cloneList(econd));		
					}

					filterAtoms.add(newconditions);	
					
	                mappingIterator.remove();
	                
					newRule = new TMappingRule(currentRule, filterAtoms, datalogFactory, termFactory, eqNormalizer);

					break;
				}				
			}
			rules.add(newRule);
		}
		
		private boolean checkConditions(TMappingRule rule1, TMappingRule rule2, Substitution toRule1) {
			if (rule2.getConditions().size() == 0)
				return true;
			if (rule2.getConditions().size() > 1 || rule1.getConditions().size() != 1)
				return false;
			
			List<Function> conjucntion1 = rule1.getConditions().get(0);
			List<Function> conjunction2 = TMappingRule.cloneList(rule2.getConditions().get(0));
			for (Function f : conjunction2)  {
				substitutionUtilities.applySubstitution(f, toRule1);
				if (!conjucntion1.contains(f))
					return false;
			}
			return true;
		}
	}

	// end of the inner class


	private static final boolean noCQC = false;
	private final AtomFactory atomFactory;
	private final TermFactory termFactory;
	private final DatalogFactory datalogFactory;
	private final SubstitutionUtilities substitutionUtilities;
	private final EQNormalizer eqNormalizer;

	@Inject
	private TMappingProcessor(AtomFactory atomFactory, TermFactory termFactory, DatalogFactory datalogFactory,
							  SubstitutionUtilities substitutionUtilities, EQNormalizer eqNormalizer) {
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
		this.datalogFactory = datalogFactory;
		this.substitutionUtilities = substitutionUtilities;
		this.eqNormalizer = eqNormalizer;
	}

	/**
	 * constructs the TMappings for object properties using DAG
	 * @param mappingIndex
	 * @param originalMappings
	 * @param dag
	 */
	private void getObjectTMappings(Map<Predicate, TMappingIndexEntry> mappingIndex,
			Map<Predicate, List<TMappingRule>> originalMappings,
			EquivalencesDAG<ObjectPropertyExpression> dag,
			TMappingExclusionConfig excludeFromTMappings) {

		for (Equivalences<ObjectPropertyExpression> propertySet : dag) {

			ObjectPropertyExpression representative = propertySet.getRepresentative();
			if (representative.isInverse())
				continue;

			if (excludeFromTMappings.contains(representative)) {
				continue;
			}

			/* Getting the current node mappings */
			Predicate currentPredicate = atomFactory.getObjectPropertyPredicate(representative.getIRI());
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);	

			for (Equivalences<ObjectPropertyExpression> descendants : dag.getSub(propertySet)) {
				for(ObjectPropertyExpression childproperty : descendants) {
					/*
					 * adding the mappings of the children as own mappings, the new
					 * mappings use the current predicate instead of the child's
					 * predicate and, if the child is inverse and the current is
					 * positive, it will also invert the terms in the head
					 */
					List<TMappingRule> childmappings = originalMappings.get(
							atomFactory.getObjectPropertyPredicate(childproperty.getIRI()));
					if (childmappings == null)
						continue;
					
					for (TMappingRule childmapping : childmappings) {
						List<Term> terms = childmapping.getHeadTerms();
						Function newMappingHead = !childproperty.isInverse()
							? termFactory.getFunction(currentPredicate, terms)
							: termFactory.getFunction(currentPredicate, terms.get(1), terms.get(0));

						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping, datalogFactory,
								termFactory, eqNormalizer);
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			/* Setting up mappings for the equivalent classes */
			for (ObjectPropertyExpression equivProperty : propertySet) {
				if (!equivProperty.isInverse())
					setMappings(mappingIndex, atomFactory.getObjectPropertyPredicate(equivProperty.getIRI()), currentNodeMappings);
			}
		} // Properties loop ended
		
	}

	/**
	 * constructs the TMappings for data properties using DAG
	 * @param mappingIndex
	 * @param originalMappings
	 * @param dag
	 */
	private void getDataTMappings(Map<Predicate, TMappingIndexEntry> mappingIndex,
			Map<Predicate, List<TMappingRule>> originalMappings,
			EquivalencesDAG<DataPropertyExpression> dag,
			TMappingExclusionConfig excludeFromTMappings) {
		
		for (Equivalences<DataPropertyExpression> propertySet : dag) {
			DataPropertyExpression representative = propertySet.getRepresentative();

			if (excludeFromTMappings.contains(representative)) {
				continue;
			}
			/* Getting the current node mappings */
			Predicate currentPredicate = atomFactory.getDataPropertyPredicate(representative.getIRI());
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);

			for (Equivalences<DataPropertyExpression> descendants : dag.getSub(propertySet)) {
				for(DataPropertyExpression childproperty : descendants) {

					/*
					 * adding the mappings of the children as own mappings, the new
					 * mappings use the current predicate instead of the child's
					 * predicate and, if the child is inverse and the current is
					 * positive, it will also invert the terms in the head
					 */
					List<TMappingRule> childmappings = originalMappings.get(atomFactory.getDataPropertyPredicate(
                            childproperty.getIRI()));
					if (childmappings == null)
						continue;

					for (TMappingRule childmapping : childmappings) {
						List<Term> terms = childmapping.getHeadTerms();

						Function newMappingHead = termFactory.getFunction(currentPredicate, terms);
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping, datalogFactory,
								termFactory, eqNormalizer);
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			/* Setting up mappings for the equivalent classes */
			for (DataPropertyExpression equivProperty : propertySet) {
				setMappings(mappingIndex, atomFactory.getDataPropertyPredicate(equivProperty.getIRI()), currentNodeMappings);
			}
		} // Properties loop ended
		
	}
	
	/**
	 * constructs the TMappings using DAG
	 * @param originalMappings
	 * @param reasoner
	 * @return
	 */

	public List<CQIE> getTMappings(List<CQIE> originalMappings, ClassifiedTBox reasoner, CQContainmentCheckUnderLIDs cqc, TMappingExclusionConfig excludeFromTMappings) {

		final boolean printouts = false;
		
		if (printouts)
			System.out.println("ORIGINAL MAPPING SIZE: " + originalMappings.size());
		
		if (excludeFromTMappings == null)
			throw new NullPointerException("excludeFromTMappings");
		
		Map<Predicate, TMappingIndexEntry> mappingIndex = new HashMap<>();

		Map<Predicate, List<TMappingRule>> originalMappingIndex = new HashMap<>();
		
		/***
		 * Creates an index of all mappings based on the predicate of the head of
		 * the mapping. The returned map can be used for fast access to the mapping
		 * list.
		 */
		
		//CQContainmentCheckUnderLIDs cqc0 = new CQContainmentCheckUnderLIDs(null);

		if (printouts)
			System.out.println("===CHECKING REDUNDANCY: " + cqc);
		for (CQIE mapping : originalMappings) {	

			if (!noCQC)
				mapping = cqc.removeRedundantAtoms(mapping);
			else {
				int c = 0;
				for (Function a : mapping.getBody()) 
					if (!(a.getFunctionSymbol() instanceof BuiltinPredicate))
						c++;
				
				if (c == 1)
					CQContainmentCheckUnderLIDs.oneAtomQs++;
				else if (c == 2)
					CQContainmentCheckUnderLIDs.twoAtomQs++;
			}	
			
			TMappingRule rule = new TMappingRule(mapping.getHead(), mapping.getBody(), cqc, datalogFactory, termFactory, eqNormalizer);
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
		if (printouts)
			System.out.println("===END OF CHECKING REDUNDANCY: " + CQContainmentCheckUnderLIDs.oneAtomQs + "/" + CQContainmentCheckUnderLIDs.twoAtomQs);
		

		/*
		 * We start with the property mappings, since class t-mappings require
		 * that these are already processed. 
		 * Processing mappings for all Properties
		 *
		 * We process the mappings for the descendants of the current node,
		 * adding them to the list of mappings of the current node as defined in
		 * the TMappings specification.
		 */

		getObjectTMappings(mappingIndex, originalMappingIndex, reasoner.objectPropertiesDAG(), excludeFromTMappings);
		getDataTMappings(mappingIndex, originalMappingIndex, reasoner.dataPropertiesDAG(), excludeFromTMappings);

		/*
		 * Property t-mappings are done, we now continue with class t-mappings.
		 */

		for (Equivalences<ClassExpression> classSet : reasoner.classesDAG()) {

			if (!(classSet.getRepresentative() instanceof OClass))
				continue;

			OClass representative = (OClass)classSet.getRepresentative();

			if (excludeFromTMappings.contains(representative)) {
				continue;
			}

			/* Getting the current node mappings */
			Predicate currentPredicate = atomFactory.getClassPredicate(representative.getIRI());
			TMappingIndexEntry currentNodeMappings = getMappings(mappingIndex, currentPredicate);

			for (Equivalences<ClassExpression> descendants : reasoner.classesDAG().getSub(classSet)) {
				for (ClassExpression childDescription : descendants) {

                    /* adding the mappings of the children as own mappings, the new
					 * mappings. There are three cases, when the child is a named
					 * class, or when it is an \exists P or \exists \inv P. 
					 */
					
					final int arg;
					final Predicate childPredicate;
					if (childDescription instanceof OClass) {
						childPredicate = atomFactory.getClassPredicate(((OClass) childDescription).getIRI());
						arg = 0;
					}
					else if (childDescription instanceof ObjectSomeValuesFrom) {
						ObjectPropertyExpression some = ((ObjectSomeValuesFrom) childDescription).getProperty();
						childPredicate = atomFactory.getObjectPropertyPredicate(some.getIRI());
						arg = some.isInverse() ? 1 : 0;
					} 
					else {
						assert (childDescription instanceof DataSomeValuesFrom);
						DataPropertyExpression some = ((DataSomeValuesFrom) childDescription).getProperty();
						childPredicate = atomFactory.getDataPropertyPredicate(some.getIRI());
						arg = 0; // can never be an inverse
					} 
					
					List<TMappingRule> childmappings = originalMappingIndex.get(childPredicate);
					if (childmappings == null)
						continue;
					
					for (TMappingRule childmapping : childmappings) {
						List<Term> terms = childmapping.getHeadTerms();
						Function newMappingHead = termFactory.getFunction(currentPredicate, terms.get(arg));
						TMappingRule newmapping = new TMappingRule(newMappingHead, childmapping, datalogFactory,
								termFactory, eqNormalizer);
						currentNodeMappings.mergeMappingsWithCQC(newmapping);
					}
				}
			}

			/* Setting up mappings for the equivalent classes */
			for (ClassExpression equiv : classSet) {
				if (equiv instanceof OClass)
					setMappings(mappingIndex, atomFactory.getClassPredicate(((OClass) equiv).getIRI()), currentNodeMappings);
			}
		}
		

		List<CQIE> tmappingsProgram = new LinkedList<>();
		for (Entry<Predicate, TMappingIndexEntry> entry : mappingIndex.entrySet()) {
			for (TMappingRule mapping : entry.getValue()) {
				CQIE cq = mapping.asCQIE();
				tmappingsProgram.add(cq);
			}
		}

		List<CQIE> nonOntologyRules = originalMappingIndex.entrySet().stream()
				.filter(e -> !mappingIndex.containsKey(e.getKey()))
				.flatMap(e -> e.getValue().stream())
				.map(m -> m.asCQIE())
				.collect(ImmutableCollectors.toList());

		tmappingsProgram.addAll(nonOntologyRules);

		if (printouts) {
			Map<Integer, Set<Predicate>> frequences = new HashMap<>();
			for (Entry<Predicate, TMappingIndexEntry> entry : mappingIndex.entrySet()) {
				if (!entry.getValue().rules.isEmpty()) {
					Set<Predicate> freq = frequences.get(entry.getValue().rules.size());
					if (freq == null) {
						freq = new HashSet<>();
						frequences.put(entry.getValue().rules.size(), freq);
					}
					freq.add(entry.getKey());
				}
			}
			System.out.println("T-MAPPING SIZE: " + tmappingsProgram.size());
			List<Integer> sorted = new ArrayList<>(frequences.keySet());
			Collections.sort(sorted);
			for (Integer idx : sorted) {
				for (Predicate p : frequences.get(idx)) {
					TMappingIndexEntry e = 	mappingIndex.get(p);
					System.out.println(p + " " + e.rules.size());
					for (TMappingRule r : e.rules) 
						System.out.println("    " + r.asCQIE());
				}
			}
			int total = 0;
			for (Integer idx: sorted) {
				System.out.println("   " + idx + ": " +  frequences.get(idx).size() + " " + frequences.get(idx));
				total += frequences.get(idx).size();
			}
			System.out.println("NUMBER OF PREDICATES: " + total);
			System.out.println("TMAP " + tmappingsProgram);
		}
				
		return tmappingsProgram;
	}

	
	private TMappingIndexEntry getMappings(Map<Predicate, TMappingIndexEntry> mappingIndex, Predicate current) {
		
		TMappingIndexEntry currentMappings = mappingIndex.get(current);	
		if (currentMappings == null) {
			currentMappings = new TMappingIndexEntry();
			mappingIndex.put(current, currentMappings);
		}
		return currentMappings;
	}

	private static void setMappings(Map<Predicate, TMappingIndexEntry> mappingIndex, Predicate predicate, TMappingIndexEntry mapping) {
		mappingIndex.put(predicate, mapping.copyOf(predicate));
	}

}
