package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQContainmentCheck;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.SubstitutionUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnionOfSqlQueries {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private final CQContainmentCheck cqc;
	
	private final List<SelectProjectJoinQuery> rules = new LinkedList<>();
	private final List<CQIE> nonSPJqueries = new LinkedList<>();
	
	public UnionOfSqlQueries(CQContainmentCheck cqContainmentCheck) {
		this.cqc = cqContainmentCheck;
	}

	public List<CQIE> asCQIE() {
		List<CQIE> list = new LinkedList<>();
		list.addAll(nonSPJqueries);
		for (SelectProjectJoinQuery rule : rules)
			list.add(rule.asCQIE());
		return list;
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
	public void add(CQIE cq) {
	
		//System.err.println("UCQ add: " +  cq);
		
		boolean nonSPJ = false;
		for (Function f : cq.getBody()) {
			if (!f.isDataFunction() && !f.isBooleanFunction()) {
				nonSPJ = true;
				break;
			}
			if (f.getFunctionSymbol() == OBDAVocabulary.NOT) {
				nonSPJ = true;
				break;
			}
			for (Term t : f.getTerms())
				if ((t instanceof ValueConstant) && ((ValueConstant)t).getValue().equals("null")) { 
					nonSPJ = true;
					break;
				}
		}
		for (Term t : cq.getHead().getTerms())
			if ((t instanceof ValueConstant) && ((ValueConstant)t).getValue().equals("null")) { 
				nonSPJ = true;
				break;
			}
		if (nonSPJ) {
			nonSPJqueries.add(cq);
			return;
		}
		
		SelectProjectJoinQuery newRule = new SelectProjectJoinQuery(cq.getHead(), cq.getBody());
		
		// Facts are just added
		if (newRule.isFact()) {
			rules.add(newRule);
			return;
		}
	
		//System.out.println("ADDING: " + newRule);
		
		Iterator<SelectProjectJoinQuery> mappingIterator = rules.iterator();
		while (mappingIterator.hasNext()) {

			SelectProjectJoinQuery currentRule = mappingIterator.next(); 
					
			boolean couldIgnore = false;
			
			Substitution toNewRule = newRule.computeHomomorphsim(currentRule);
			if (toNewRule != null) { 
				boolean conditions = currentRule.isConditionsEmpty();
				
				if ((currentRule.getConditions().size() == 1) && !newRule.isConditionsEmpty()){
					List<Function> currentConditions = currentRule.cloneList(currentRule.getConditions().iterator().next());
					List<Function> newConditions = newRule.getConditions().iterator().next(); 
					boolean notfound = false;
					for (Function f : currentConditions) {
						SubstitutionUtilities.applySubstitution(f, toNewRule);
						if (!newConditions.contains(f)) {
							notfound = true;
							break;
						}	
					}
					if (!notfound)
						conditions = true;
					//if (conditions)
					//	System.err.println("h: " + currentRule + " to " + newRule);
					//else
					//	System.err.println("NO h: " + currentRule + " to " + newRule);						
				}
				if (conditions) {
					if (newRule.databaseAtomsSize() < currentRule.databaseAtomsSize()) {
						couldIgnore = true;
					}
					else {
						// if the new mapping is redundant and there are no conditions then do not add anything		
						//System.out.println("    REDUNDANT1 FOR " + currentRule);						
						return;
					}
				}
			}
			
			Substitution fromNewRule = currentRule.computeHomomorphsim(newRule);		
			if (fromNewRule != null) { 
				boolean conditions = newRule.isConditionsEmpty();
				
				if ((newRule.getConditions().size() == 1) && !currentRule.isConditionsEmpty()){
					List<Function> newConditions = newRule.cloneList(newRule.getConditions().iterator().next());
					List<Function> currentConditions = currentRule.getConditions().iterator().next(); 
					boolean notfound = false;
					for (Function f : newConditions) {
						SubstitutionUtilities.applySubstitution(f, fromNewRule);
						if (!currentConditions.contains(f)) {
							notfound = true;
							break;
						}	
					}
					if (!notfound)
						conditions = true;
					//if (conditions)
					//	System.err.println("h2: " + newRule + " to " + currentRule);
					//else
					//	System.err.println("NO h2: " + newRule + " to " + currentRule);						
				}
				if (conditions) {
					// The existing query is more specific than the new query, so we
					// need to add the new query and remove the old	 
					mappingIterator.remove();
					//System.out.println("   SUBSUME: " + currentRule);
					continue;
				}
			}
			
			if (couldIgnore) {
				// if the new mapping is redundant and there are no conditions then do not add anything		
				//System.out.println("    REDUNDANT2 FOR " + currentRule);						
				return;					
			}
			
			if ((toNewRule != null) && (fromNewRule != null)) {
				// We found an equivalence, we will try to merge the conditions of
				// newRule into the currentRule
				//System.err.println("\n" + newRule + "\n v \n" + currentRule + "\n");
				
			 	// Here we can merge conditions of the new query with the one we have
				// just found
				// new map always has just one set of filters  !!
				List<Function> newconditions = newRule.cloneList(newRule.getConditions().get(0));
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
					if (found) {
						//System.out.println("    REDUNDANT3 FOR " + currentRule);						
						return;
					}
				}
				
                mappingIterator.remove();
                
				newRule = new SelectProjectJoinQuery(currentRule, newconditions);

				break;
			}				
		}
		rules.add(newRule);
		//System.out.println("    SUCCESS ");						
	}
	
	
	public class SelectProjectJoinQuery {
		
		private final Function head;
		private final List<Function> databaseAtoms;	
		private final CQIE stripped;
		// an OR-connected list of AND-connected atomic filters
		private final List<List<Function>> filterAtoms;	  

		
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
		 */
		
		public SelectProjectJoinQuery(Function head, List<Function> body) {
			this.databaseAtoms = new ArrayList<>(body.size()); // we estimate the size
			
			// to remove duplicates
			Set<Function> filters = new HashSet<>();
			
			for (Function atom : body) {
				if (atom.getFunctionSymbol() instanceof BuiltinPredicate) {
					Function clone = (Function)atom.clone();
					filters.add(clone);
				}
				else {
					// database atom, we need to replace all constants by filters
					databaseAtoms.add(replaceConstants(atom, filters));			
				}
			}
			if (filters.isEmpty()) 
				this.filterAtoms = Collections.emptyList();
			else {
				List<Function> f = new ArrayList<>(filters.size());
				f.addAll(filters);
				this.filterAtoms = Collections.singletonList(f);
			}
			
			this.head = replaceConstants(head, filters);
			this.stripped = fac.getCQIE(this.head, databaseAtoms);
		}

		
		private int freshVarCount = 0;
		private final Map<Constant, Variable> valueMap = new HashMap<>();
		
		private Function replaceConstants(Function a, Set<Function> filters) {
			Function atom = (Function)a.clone();
			
			for (int i = 0; i < atom.getTerms().size(); i++) {
				Term term = atom.getTerm(i);
				if (term instanceof Constant) {
					// Found a constant, replacing with a fresh variable
					// and adding the new equality atom
					Constant c = (Constant)term;
					Variable var = valueMap.get(c);
					if (var == null) {
						freshVarCount++;
						var = fac.getVariable("?FreshVar" + freshVarCount);
						valueMap.put(c, var);
						filters.add(fac.getFunctionEQ(var, c));
					}
					atom.setTerm(i, var);
				}
			}
			
			return atom;
		}
				
		public SelectProjectJoinQuery(SelectProjectJoinQuery baseRule, List<Function> conditionsOR) {
			this.databaseAtoms = cloneList(baseRule.databaseAtoms);
			this.head = (Function)baseRule.head.clone();

			this.filterAtoms = new ArrayList<>(baseRule.filterAtoms.size() + 1);
			for (List<Function> baseList: baseRule.filterAtoms)
				filterAtoms.add(cloneList(baseList));		
			filterAtoms.add(conditionsOR);

			this.stripped = fac.getCQIE(head, databaseAtoms);
		}
		
		
		public SelectProjectJoinQuery(Function head, SelectProjectJoinQuery baseRule) {
			this.filterAtoms = new ArrayList<>(baseRule.filterAtoms.size());
			for (List<Function> baseList: baseRule.filterAtoms)
				filterAtoms.add(cloneList(baseList));
			
			this.databaseAtoms = cloneList(baseRule.databaseAtoms);
			this.head = (Function)head.clone();
			
			this.stripped = fac.getCQIE(head, databaseAtoms);
		}
		
		
		public List<Function> cloneList(List<Function> list) {
			List<Function> newlist = new ArrayList<>(list.size());
			for (Function atom : list) {
				Function clone = (Function)atom.clone();
				newlist.add(clone);	
			}
			return newlist;
		}
		
		@Deprecated // TEST ONLY
		Function getHead() {
			return head;
		}
		
		public boolean isConditionsEmpty() {
			return filterAtoms.isEmpty();
		}
		
		public Substitution computeHomomorphsim(SelectProjectJoinQuery other) {
			return cqc.computeHomomorphsim(stripped, other.stripped);
		}
		
		public CQIE asCQIE() {
			List<Function> combinedBody;
			if (!filterAtoms.isEmpty()) {
				combinedBody = new ArrayList<>(databaseAtoms.size() + filterAtoms.size()); 
				combinedBody.addAll(databaseAtoms);
				
				Iterator<List<Function>> iterOR = filterAtoms.iterator();
				List<Function> list = iterOR.next(); // IMPORTANT: assume that conditions is non-empty
				Function mergedConditions = getMergedByAND(list);
				while (iterOR.hasNext()) {
					list = iterOR.next();
					Function e = getMergedByAND(list);
					mergedConditions = fac.getFunctionOR(e, mergedConditions);				
				}
				
				combinedBody.add(mergedConditions);
			}
			else
				combinedBody = databaseAtoms;
			
			CQIE cq = fac.getCQIE(head, combinedBody);
			EQNormalizer.enforceEqualities(cq);
			return cq;
		}
		
		/***
		 * Takes a list of boolean atoms and returns one single atom
		 * representing the conjunction 
		 * 
		 * ASSUMPTION: the list is non-empty
		 * 
		 * Example: A -> A
		 *          A, B -> AND(B,A)
		 *          A, B, C -> AND(C,AND(B,A))
		 * 
		 */
		
		private Function getMergedByAND(List<Function> list) {
			Iterator<Function> iterAND = list.iterator();
			Function mergedConditions = iterAND.next();
			while (iterAND.hasNext()) {
				Function e = iterAND.next();
				mergedConditions = fac.getFunctionAND(e, mergedConditions);				
			}		
			return mergedConditions;
		}
		
		
		public boolean isFact() {
			return databaseAtoms.isEmpty() && filterAtoms.isEmpty();
		}
		
		public List<Term> getHeadTerms() {
			return head.getTerms();
		}
		
		public int databaseAtomsSize() {
			return databaseAtoms.size();
		}
		
		public List<List<Function>> getConditions() {
			return filterAtoms;
		}
		
		@Override
		public int hashCode() {
			return head.hashCode() ^ databaseAtoms.hashCode() ^ filterAtoms.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof SelectProjectJoinQuery) {
				SelectProjectJoinQuery otherRule = (SelectProjectJoinQuery)other;
				return (head.equals(otherRule.head) && 
						databaseAtoms.equals(otherRule.databaseAtoms) && 
						filterAtoms.equals(otherRule.filterAtoms));
			}
			return false;
		}

		@Override 
		public String toString() {
			return head + " <- " + databaseAtoms + " AND " + filterAtoms;
		}
	}	
	
}
