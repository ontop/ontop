package it.unibz.inf.ontop.datalog;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLQueryFlattener {

	private static final Logger log = LoggerFactory.getLogger(SPARQLQueryFlattener.class);

    /*
     TODO: eliminate both instance variables
     */
    private final List<Predicate> irreducible = new LinkedList<>();
	private final DatalogProgram program;
	private final DatalogFactory datalogFactory;
	private final EQNormalizer eqNormalizer;
	private final UnifierUtilities unifierUtilities;
	private final SubstitutionUtilities substitutionUtilities;

	public SPARQLQueryFlattener(DatalogProgram program, DatalogFactory datalogFactory,
								EQNormalizer eqNormalizer, UnifierUtilities unifierUtilities,
								SubstitutionUtilities substitutionUtilities) {
		this.program = program;
		this.datalogFactory = datalogFactory;
		this.eqNormalizer = eqNormalizer;
		this.unifierUtilities = unifierUtilities;
		this.substitutionUtilities = substitutionUtilities;
	}

	/***
	 * Given a datalog program, this method will try to flatten the query
     * as much as possible by applying resolution steps
	 * exhaustively to every atom in the query against the rules in
	 * the program (partial evaluation of the program).
	 * <p>
	 * This is used to flatten the Datalog queries that are produced by the
	 * SparqlAlgebraToDatalogTranslator.
	 * 
	 * <p>
	 * Example: matching rule the unfolding program.
	 * <p>
	 * program<br>
	 * <br>
     * ans(x) :- A(x),B(x)<br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * B(x) :- table3(x,z)<br>
	 * <br>
	 * produces <br>
	 * ans(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * ans(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * <p>
	 * The strategy of this unfolding is simple, we cycle through all the
	 * queries and attempt to resolve atom 0 in the body against the rules in
	 * unfolding program. The resolution engine will generate 1 or more CQs as
	 * result. The original atom is removed, and the results are appended to the
	 * end of each query (hence its always safe to unfold atom 0). The new
	 * queries are kept for a next cycle. We stop when no new queries are
	 * produced.
	 * <p>
	 * The right side of left joins will never be touched.
	 *
	 * @return
	 */



	public List<CQIE> flatten(CQIE topLevelRule) {

        List<CQIE> workingSet = new LinkedList<>();
		workingSet.add(topLevelRule);

		ListIterator<CQIE> iterator = workingSet.listIterator();
		while (iterator.hasNext()) {
			CQIE rule = iterator.next(); 

			List<CQIE> result = computePartialEvaluation(rule.getBody(), rule, new Stack<Integer>());
            if (!result.isEmpty()) {
				// one more step in the partial evaluation was computed, we need to
				// remove the old query and add the result instead. Each of the new
				// queries could still require more steps of evaluation, so we
				// move to the previous position
				iterator.remove();
				for (CQIE newquery : result)
					if (!workingSet.contains(newquery)) {
						iterator.add(newquery);
						iterator.previous();
					}
			}
			// otherwise, the result is empty and so,
			// this rule is already a partial evaluation

            // add the irreducible rules
            for (Predicate p : irreducible) {
                for (CQIE def: program.getRules(p)) {
                    iterator.add(def);
                    iterator.previous();
                }
            }
            irreducible.clear();
		}
		
		// We need to enforce equality again, because at this point it is 
		//  possible that there is still some EQ(...) 
		for (CQIE query : workingSet) {
			eqNormalizer.enforceEqualities(query);
		}

		return workingSet;
	}

	/***
	 * Goes through each term, and recursively each inner term trying to resolve
	 * each atom. Returns an empty list if the partial evaluation is completed
	 * (no atoms can be resolved and each atom is a leaf atom), or a list with
	 * one or more queries if there was one atom that could be resolved against
	 * one or more rules. The list contains the result of the resolution steps
	 * against those rules.
	 * 
	 * @param atoms
	 * @param rule
	 * @param termidx
	 * @return
	 */

	private List<CQIE> computePartialEvaluation(List<Function> atoms, CQIE rule, Stack<Integer> termidx) {

		for (int atomIdx = 0; atomIdx < atoms.size(); atomIdx++) {
			Function atom = atoms.get(atomIdx);

			if (atom.isDataFunction()) {
				// This is a data atom, it should be unfolded with the usual resolution algorithm.
                termidx.push(atomIdx);
				List<CQIE> result = resolveDataAtom(atom, rule, termidx, false);
                termidx.pop();

				if (!result.isEmpty())
					return result;
			}			
			else if (atom.isAlgebraFunction()) {
				// These may contain data atoms that need to be unfolded, we need to recursively unfold each term.

                termidx.push(atomIdx);
                List<CQIE> result;
				if (atom.getFunctionSymbol().equals(datalogFactory.getSparqlLeftJoinPredicate()))
					result = computePartialEvaluationInLeftJoin(getSubAtoms(atom), rule, termidx);
				else
					result = computePartialEvaluation(getSubAtoms(atom), rule, termidx);
                termidx.pop();

				if (!result.isEmpty())
					return result;
			} 			
		}

		return Collections.emptyList();
	}

	private List<CQIE> computePartialEvaluationInLeftJoin(List<Function> atoms, CQIE rule, Stack<Integer> termidx) {

		int nonBooleanAtomCounter = 0;

		for (int atomIdx = 0; atomIdx < atoms.size(); atomIdx++) {
			Function atom = atoms.get(atomIdx);

			if (atom.isDataFunction()) {
				nonBooleanAtomCounter += 1;
				
				// This is a data atom, it should be unfolded with the usual resolution algorithm.
                termidx.push(atomIdx);
				List<CQIE> result = resolveDataAtom(atom, rule, termidx, true);
                // If there are none, the atom is logically empty, careful, LEFT JOIN alert!
                if (nonBooleanAtomCounter == 2) /* isLeftJoinSecondArgument */ {
                    if (result.size() > 1) {
                        // We had disjunction on the second atom of the leftjoin, that is,
                        // more than two rules that unified. LeftJoin is not
                        // distributable on the right component, hence, we cannot simply
                        // generate 2 rules for the second atom.
                        //
                        // The rules must be untouched, no partial evaluation is
                        // possible. We must return the original rule.
                        result = Collections.emptyList();
                        irreducible.add(atom.getFunctionSymbol());
                    }
                }
                termidx.pop();

                if (!result.isEmpty())
					return result;
			}			
			else if (atom.isAlgebraFunction()) {
				nonBooleanAtomCounter += 1;
				
				// These may contain data atoms that need to be unfolded, we need to recursively unfold each term.

                termidx.push(atomIdx);
				List<CQIE> result;
				if (atom.getFunctionSymbol().equals(datalogFactory.getSparqlLeftJoinPredicate()))
					result = computePartialEvaluationInLeftJoin(getSubAtoms(atom), rule, termidx);
				else
					result = computePartialEvaluation(getSubAtoms(atom), rule, termidx);
                termidx.pop();

				if (!result.isEmpty())
					return result;
			} 			
		}

		return Collections.emptyList();
	}
	

	/***
	 * Applies a resolution step over a non-boolean/non-algebra atom (i.e. data
	 * atoms). The resolution step will will try to match the <strong>focus atom
	 * a</strong> in the input <strong>rule r</strong> against the rules in
	 * <p>
	 * For each <strong>rule s</strong>, if the <strong>head h</strong> of s is
	 * unifiable with a, then this method will do the following:
	 * <ul>
	 * <li>Compute a most general unifier <strong>mgu</strong> for h and a (see
	 * {@link UnifierUtilities#getMGU(Function, Function)})</li>
	 * <li>Create a clone r' of r</li>
	 * <li>We replace a in r' with the body of s
	 * <li>
	 * <li>We apply mgu to r' (see {@code UnifierUtilities#applyUnifier(it.unibz.krdb.obda.model.CQIE, it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier)} )</li>
	 * <li>return r'
	 * </ul>
	 * 
	 * 
	 * 
	 * @param atom
	 *            The atom to be resolved.
	 * @param rule
	 *            The rule in which this atom resides
	 * @param termidx
	 *            The index of the atom in the rule (ifts nested, the stack
	 *            indicates the nesting, position by position, the first being
	 *            "list" positions (function term lists) and the last the focus
	 *            atoms position.
	 * @return <ul>
	 *         <li>empty list if the atom a is <strong>extensional
	 *         predicate (those that have no defining rules)</strong></li>
	 *         <li>a list with one ore more rules otherwise</li>
	 *         <ul>
	 * 
	 * @see UnifierUtilities
	 */
	private List<CQIE> resolveDataAtom(Function atom, CQIE rule, Stack<Integer> termidx,
                                       boolean isLeftJoin) {

		List<CQIE> definitions = program.getRules(atom.getFunctionSymbol());
		if (definitions == null)
            return Collections.emptyList();

        List<CQIE> result = new LinkedList<>();
        for (CQIE candidateRule : definitions) {
            CQIE freshRule = datalogFactory.getFreshCQIECopy(candidateRule);
            // IMPORTANT: getMGU changes arguments
            Substitution mgu = unifierUtilities.getMGU(freshRule.getHead(), atom);
            if (mgu == null) {
                continue; // Failed attempt
            }

            // We have a matching rule, now we prepare for the resolution step
            // if we are in a left join, we need to make sure the fresh rule
            // has only one data atom
            List<Function> freshRuleBody;
            if (isLeftJoin)
                freshRuleBody = foldJOIN(freshRule.getBody());
            else
                freshRuleBody = freshRule.getBody();

            // generating the new body of the rule
            CQIE partialEvaluation = rule.clone();

            // locating the list that contains the current Function (either body
            // or inner term) and replacing the current atom, with the body of
            // the matching rule.
            List<Function> atomsList = partialEvaluation.getBody();
            for (int d = 0; d < termidx.size() - 1; d++) {
                int i = termidx.get(d);
                Function f = atomsList.get(i);
                atomsList = getSubAtoms(f);
            }
            int pos = termidx.peek(); // at termidx.size() - 1
            atomsList.remove(pos);
            atomsList.addAll(pos, freshRuleBody);

            substitutionUtilities.applySubstitution(partialEvaluation, mgu, false);
            result.add(partialEvaluation);
        }// end for candidate matches

        return result;
	}

	/***
	 * * Normalizes a rule that has multiple data atoms to a rule in which there
	 * is one single Join atom by creating a nested Join structure. Required to
	 * resolve atoms in LeftJoins (any, left or right) to avoid ending up with
	 * left joins with more than 2 table definitions. If the body contains only
	 * one data atom (or none) it will return <strong>null</strong>. For
	 * example:
	 * 
	 * <pre>
	 * m(x,y) :- R(x,y), R(y,z), R(z,m)
	 * 
	 * into
	 * 
	 * m(x,y) :- Join(R(x,y), Join(R(y,z), R(z,m))
	 * </pre>
	 * 
	 * @param body
	 * @return a new list with the nested joins.
	 */
	private List<Function> foldJOIN(List<Function> body) {
		
		List<Function> dataAtomsList = new LinkedList<>();
		List<Function> otherAtomsList = new LinkedList<>();

		for (Function a : body) {
			if (a.isDataFunction() || a.isAlgebraFunction()) 
				dataAtomsList.add(a);
			else 
				otherAtomsList.add(a);
		}

		if (dataAtomsList.size() <= 1) 
			return body;
		
		Function foldedJoinAtom = dataAtomsList.remove(0);
		for (Function a : dataAtomsList)
			foldedJoinAtom = datalogFactory.getSPARQLJoin(foldedJoinAtom, a);
		
		otherAtomsList.add(0, foldedJoinAtom);
		
		return otherAtomsList;
	}
	

    private static List<Function> getSubAtoms(Function f) {
        return (List<Function>)(List)f.getTerms();
    }
}
