package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * Splits a given mapping into builtin predicates (conditions)
 * and all other atoms (stripped), which are checked for containment
 * by the TMapping construction algorithm.
 */

public class TMappingRule {
	
	private final Function head;
	private final ImmutableList<DataAtom<AtomPredicate>> databaseAtoms;
	// an OR-connected list of AND-connected atomic filters
	private final ImmutableList<ImmutableList<Function>> filterAtoms;

	private final DatalogFactory datalogFactory;
	private final TermFactory termFactory;
	private final AtomFactory atomFactory;
	private final ImmutabilityTools immutabilityTools;

	/***
	 * Given a mappings in currentMapping, this method will
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
	
	public TMappingRule(Function head, List<Function> body, DatalogFactory datalogFactory, TermFactory termFactory, AtomFactory atomFactory, ImmutabilityTools immutabilityTools) {
        this.datalogFactory = datalogFactory;
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        this.immutabilityTools = immutabilityTools;

		ImmutableList.Builder<Function> filters = ImmutableList.builder();
		ImmutableList.Builder<DataAtom<AtomPredicate>> dbs = ImmutableList.builder();

        Map<Term, Variable> valueMap = new HashMap<>();

		for (Function atom : body) {
			if (!(atom.getFunctionSymbol() instanceof AtomPredicate)) {
				Function clone = (Function)atom.clone();
				filters.add(clone);
			}
			else {
				// database atom, we need to replace all constants by filters
				dbs.add(replaceConstants2(atom, filters, valueMap));
			}
		}
        this.databaseAtoms = dbs.build();

        this.head = replaceConstants(head, filters, valueMap);

        // System.out.println("M" + head + " := " + databaseAtoms);

		ImmutableList<Function> f = filters.build();
		this.filterAtoms = f.isEmpty() ? ImmutableList.of() : ImmutableList.of(f);
	}

	
	private Function replaceConstants(Function a, ImmutableList.Builder<Function> filters, Map<Term, Variable> valueMap) {
		Function atom = (Function)a.clone();

		for (int i = 0; i < atom.getTerms().size(); i++) {
			Term term = atom.getTerm(i);
			if (term instanceof Constant) {
				// Found a constant, replacing with a fresh variable
				// and adding the new equality atom
				Constant c = (Constant)term;
				Variable var = valueMap.get(c);
				if (var == null) {
					var = termFactory.getVariable("?FreshVar" + valueMap.keySet().size());
					valueMap.put(c, var);
					filters.add(termFactory.getFunctionEQ(var, c));
				}
				atom.setTerm(i, var);
			}
		}
		
		return atom;
	}

	private DataAtom<AtomPredicate> replaceConstants2(Function atom, ImmutableList.Builder<Function> filters, Map<Term, Variable> valueMap) {
		ImmutableList.Builder<VariableOrGroundTerm> builder = ImmutableList.builder();

		for (Term term : atom.getTerms()) {
			if (term instanceof Variable) {
				builder.add((Variable)term);
			}
			else {
				// Found a constant, replacing with a fresh variable
				// and adding the new equality atom
				Variable var = valueMap.get(term);
				if (var == null) {
					var = termFactory.getVariable("?FreshVar" + valueMap.keySet().size());
					valueMap.put(term, var);
					filters.add(termFactory.getFunctionEQ(var, term));
				}
				builder.add(var);
			}
		}

		return atomFactory.getDataAtom((AtomPredicate)atom.getFunctionSymbol(), builder.build());
	}

	TMappingRule(TMappingRule baseRule, ImmutableList<ImmutableList<Function>> filterAtoms) {
        this.datalogFactory = baseRule.datalogFactory;
        this.termFactory = baseRule.termFactory;
        this.atomFactory = baseRule.atomFactory;
        this.immutabilityTools = baseRule.immutabilityTools;

		this.databaseAtoms = baseRule.databaseAtoms;
		this.head = (Function)baseRule.head.clone();

		this.filterAtoms = filterAtoms;
	}
	
	
	TMappingRule(Function head, TMappingRule baseRule) {
        this.datalogFactory = baseRule.datalogFactory;
        this.termFactory = baseRule.termFactory;
		this.atomFactory = baseRule.atomFactory;
		this.immutabilityTools = baseRule.immutabilityTools;

		this.databaseAtoms = baseRule.databaseAtoms;
		this.head = (Function)head.clone();

        this.filterAtoms = baseRule.filterAtoms.stream()
                .map(list -> list.stream()
                        .map(atom -> (Function)atom.clone())
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList());
	}
	
	
	public Substitution computeHomomorphsim(TMappingRule other, CQContainmentCheckUnderLIDs cqc) {

		//System.out.println("CH: " + head + " v " + other.head);

		return cqc.computeHomomorphsim(head, databaseAtoms, other.head, other.getDatabaseAtoms());
	}
	
	public CQIE asCQIE() {
		List<Function> combinedBody;
		if (!filterAtoms.isEmpty()) {
			combinedBody = new ArrayList<>(databaseAtoms.size() + filterAtoms.size()); 
			combinedBody.addAll(getDatabaseAtoms());
			
			Iterator<ImmutableList<Function>> iterOR = filterAtoms.iterator();
			List<Function> list = iterOR.next(); // IMPORTANT: assume that conditions is non-empty
			Function mergedConditions = getMergedByAND(list);
			while (iterOR.hasNext()) {
				list = iterOR.next();
				Function e = getMergedByAND(list);
				mergedConditions = termFactory.getFunctionOR(e, mergedConditions);
			}
			
			combinedBody.add(mergedConditions);
		}
		else
			combinedBody = getDatabaseAtoms();
		
		return datalogFactory.getCQIE(head, combinedBody);
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
			mergedConditions = termFactory.getFunctionAND(e, mergedConditions);
		}		
		return mergedConditions;
	}
	
	
	public boolean isFact() {
		return databaseAtoms.isEmpty() && filterAtoms.isEmpty();
	}
	
    public Function getHead() {
        return head;
    }

    public ImmutableList<Function> getDatabaseAtoms() {
		return databaseAtoms.stream()
				.map(immutabilityTools::convertToMutableFunction)
				.collect(ImmutableCollectors.toList());
	}
	
	public ImmutableList<ImmutableList<Function>> getConditions() { return filterAtoms; }
	
	@Override
	public int hashCode() {
		return head.hashCode() ^ databaseAtoms.hashCode() ^ filterAtoms.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
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
