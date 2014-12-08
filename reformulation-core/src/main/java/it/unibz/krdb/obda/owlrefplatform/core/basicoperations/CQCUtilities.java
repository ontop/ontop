package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

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

import it.unibz.krdb.obda.model.*;

import java.util.*;

/***
 * A class that allows you to perform different operations related to query
 * containment on conjunctive queries.
 * 
 * Two usages: 
 *    - simplifying queries with DL atoms
 *    - simplifying mapping queries with SQL atoms
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class CQCUtilities {

	public static final Comparator<CQIE> ComparatorCQIE = new Comparator<CQIE>() {
		@Override
		public int compare(CQIE o1, CQIE o2) {
			return o2.getBody().size() - o1.getBody().size();
		}
	};

	public static final CQContainmentCheckSyntactic SYNTACTIC_CHECK = new CQContainmentCheckSyntactic();
	

	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedIn(CQIE q1, CQIE 2). 
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	
	public static void removeContainedQueries(List<CQIE> queries, CQContainmentCheck containment) {

		{
			Iterator<CQIE> iterator = queries.iterator();
			while (iterator.hasNext()) {
				CQIE query = iterator.next();
				ListIterator<CQIE> iterator2 = queries.listIterator(queries.size());
				while (iterator2.hasPrevious()) {
					CQIE query2 = iterator2.previous(); 
					if (query2 == query)
						break;
					if (containment.isContainedIn(query, query2)) {
						iterator.remove();
						break;
					}
				}
			}
		}
		{
			// second pass from the end
			ListIterator<CQIE> iterator = queries.listIterator(queries.size());
			while (iterator.hasPrevious()) {
				CQIE query = iterator.previous();
				Iterator<CQIE> iterator2 = queries.iterator();
				while (iterator2.hasNext()) {
					CQIE query2 = iterator2.next();
					if (query2 == query)
						break;
					if (containment.isContainedIn(query, query2)) {
						iterator.remove();
						break;
					}
				}
			}
		}
	}

	public static void optimizeQueryWithSigmaRules(List<Function> atoms, LinearInclusionDependencies sigma) {
				
		// for each atom in query body
		for (int i = 0; i < atoms.size(); i++) {
			Function atom = atoms.get(i);

			Set<Function> derivedAtoms = new HashSet<Function>();
			// collect all derived atoms
			for (CQIE rule : sigma.getRules(atom.getFunctionSymbol())) {
				// try to unify current query body atom with tbox rule body atom
				// ESSENTIAL THAT THE RULES IN SIGMA ARE "FRESH" -- see LinearInclusionDependencies.addRule				
				Function ruleBody = rule.getBody().get(0);
				Substitution theta = UnifierUtilities.getMGU(ruleBody, atom);
				if (theta == null || theta.isEmpty()) {
					continue;
				}
				// if unifiable, apply to head of tbox rule
				Function copyRuleHead = (Function) rule.getHead().clone();
				SubstitutionUtilities.applySubstitution(copyRuleHead, theta);

				derivedAtoms.add(copyRuleHead);
			}

			Iterator<Function> iterator = atoms.iterator();
			while (iterator.hasNext()) {
				Function current = iterator.next();
				if (current == atom)   // if they are not the SAME element
					continue;
				
				if (derivedAtoms.contains(current)) 
					iterator.remove();
			}
		}
	}	

	/***
	 * Removes all atoms that are redundant w.r.t to query containment.This is
	 * done by going through all unifiable atoms, attempting to unify them. If
	 * they unify with a MGU that is empty, then one of the atoms is redundant.
	 * 
	 * 
	 * @param q
	 */
	public static void removeRundantAtoms(CQIE q) {
		CQIE result = q;
		for (int i = 0; i < result.getBody().size(); i++) {
			Function currentAtom = result.getBody().get(i);
			for (int j = i + 1; j < result.getBody().size(); j++) {
				Function nextAtom = result.getBody().get(j);
				Substitution map = UnifierUtilities.getMGU(currentAtom, nextAtom);
				if (map != null && map.isEmpty()) {
					result = UnifierUtilities.unify(result, i, j);
				}
			}
		}
	}
	
}
