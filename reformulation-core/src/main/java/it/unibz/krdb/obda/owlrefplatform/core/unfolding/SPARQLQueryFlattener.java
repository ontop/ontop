package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UnifierUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLQueryFlattener {

	
	private static final OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(SPARQLQueryFlattener.class);

	private final Map<Predicate, List<CQIE>> ruleIndex = new LinkedHashMap<>();

	/***
	 * Leaf predicates are those that do not appear in the head of any rule. If
	 * a predicate is a leaf predicate, it should not be unfolded, they indicate
	 * stop points to get partial evaluations.
	 * <p>
	 * Any atom that is not a leaf, and that cannot be unified with a rule
	 * (either cause of lack of MGU, or because of a rule for the predicate of
	 * the atom) is logically empty w.r.t. to the program.
	 */
	private final Set<Predicate> extensionalPredicates = new HashSet<>();

	private final List<CQIE> inputQueryRules = new LinkedList<>();
	private final OBDAQueryModifiers modifiers;
	
	public SPARQLQueryFlattener(DatalogProgram program) {
		
		modifiers = program.getQueryModifiers();
		
		// Creating a local index for the rules according to their predicate
		for (CQIE rule : program.getRules()) {
			Predicate headPredicate = rule.getHead().getFunctionSymbol();

			if (headPredicate.getName().equals(OBDAVocabulary.QUEST_QUERY)) {
				inputQueryRules.add(rule.clone());
			}
			else {
				List<CQIE> rules = ruleIndex.get(headPredicate);
				if (rules == null) {
					rules = new LinkedList<>();
					ruleIndex.put(headPredicate, rules);
				}
				rules.add(rule.clone());
			}
			
			// Collecting the predicates that appear in the body of rules
			// (first in extensionalPredicates, then we will remove all defined (intensional)
			for (Function atom : rule.getBody()) 
				collectPredicates(extensionalPredicates, atom);
		}

		// the predicates that do not appear in the head of rules are leaf
		// predicates
		extensionalPredicates.removeAll(ruleIndex.keySet());
	}

	private void collectPredicates(Set<Predicate> predicates, Function atom) {
		if (atom.isAlgebraFunction()) {
			for (Term innerTerm : atom.getTerms()) 
				if (innerTerm instanceof Function)
					collectPredicates(predicates, (Function) innerTerm);
		} 
		else if (!(atom.isOperation())) {
			Predicate pred = atom.getFunctionSymbol();
			predicates.add(pred);
		}
	}

	/***
	 * Given a query q and the {@link #unfoldingProgram}, this method will try
	 * to flatten the query as much as possible by applying resolution steps
	 * exhaustively to every atom in the query against the rules in
	 * 'unfoldingProgram'. This will is exactly to computing a partial
	 * evaluation of q w.r.t. unfolding program, that is, a specialized version
	 * of q w.r.t. to unfolding program that requires less steps to execute.
	 * <p>
	 * This is used to translate ontological queries to database queries in the
	 * when the unfolding program is a set of mapppings, and also to flatten the
	 * Datalog queries that are produced by the
	 * {@link SparqlAlgebraToDatalogTranslator} and in some other places.
	 * 
	 * <p>
	 * Example: matching rule the unfolding program.
	 * <p>
	 * Unfolding Program<br>
	 * <br>
	 * A(x) :- table1(x,y)<br>
	 * A(x) :- table2(x,z)<br>
	 * B(x) :- table3(x,z)<br>
	 * <br>
	 * Query<br>
	 * <br>
	 * Q(x) :- A(x),B(x)<br>
	 * <br>
	 * Initially produces: <br>
	 * Q(x1) :- table1(x1,y1),B(x1)<br>
	 * Q(x1) :- table2(x1,z1),B(x1)<br>
	 * Q(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * Q(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
	 * But the final result is<br>
	 * Q(x1) :- table1(x1,y1),table3(x1,z2)<br>
	 * Q(x1) :- table2(x1,z1),table3(x1,z2)<br>
	 * 
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
	 * <p>
	 * Currently the method also is aware of functional dependencies (Primary
	 * keys) and will produce queries in which redundant joins w.r.t. to these
	 * are avoided.
	 * 
	 * @param inputquery
	 * @return
	 */


	/***
	 * Generates a partial evaluation of the rules in <b>inputquery</b> with respect to the
	 * with respect to the program given when this unfolder was initialized. The goal for
	 * this partial evaluation is the predicate <b>ans1</b>
	 * 
	 * This method assumes that the inner term (termidx) of term is a data atom,
	 * or a nested atom.
	 * <p>
	 * If the term is a data atom, it returns all the new rule resulting from
	 * resolving that atom with unifiable rules in the unfoldign program. If
	 * there are no such rules it returns null (the atom is logically empty).
	 * <p>
	 * If the atom is a Join or LeftJoin (algebra operators) it will recursively
	 * call unfoldin into each term until one method returns something different
	 * than the rule itself.
	 * <p>
	 * If the term is not a data atom, e.g., datatype atom, variable, constant,
	 * boolean atom., the method returns the original rule, without change.
	 * 
	 * otherwise it does nothing (i.e., variables, constants, etc cannot be
	 * resolved against rule
	 */
	
	public DatalogProgram flatten() {

		List<CQIE> workingSet = new LinkedList<>();
		for (CQIE query : inputQueryRules)  {
			workingSet.add(query);
			EQNormalizer.enforceEqualities(query);
		}

		ListIterator<CQIE> iterator = workingSet.listIterator();
		
		while (iterator.hasNext()) {
			CQIE rule = iterator.next(); 

			List<CQIE> result = computePartialEvaluation(rule.getBody(), rule, new Stack<Integer>());

			if (result == null) {
				// if the result is null the rule is logically empty
				iterator.remove();
			} 
			else if (!result.isEmpty()) {
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
		}
		
		// We need to enforce equality again, because at this point it is 
		//  possible that there is still some EQ(...) 
		for (CQIE query : workingSet) {
			EQNormalizer.enforceEqualities(query);
		}
			
		DatalogProgram result = termFactory.getDatalogProgram(modifiers, workingSet);
		return result;
	}

	/***
	 * Goes through each term, and recursively each inner term trying to resolve
	 * each atom. Returns an empty list if the partial evaluation is completed
	 * (no atoms can be resolved and each atom is a leaf atom), null if there is
	 * at least one atom that is not leaf and cannot be resolved, or a list with
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
			termidx.push(atomIdx);

			Function atom = atoms.get(atomIdx);

			if (atom.isDataFunction()) {
				// This is a data atom, it should be unfolded with the usual resolution algorithm.
				
				List<CQIE> result = resolveDataAtom(atom, rule, termidx, false, false);
				if (result == null || !result.isEmpty())
					return result;
			}			
			else if (atom.isAlgebraFunction()) {
				// These may contain data atoms that need to be unfolded, we need to recursively unfold each term.
				
				List<Function> innerTerms = new ArrayList<>(3);
				for (Term t : atom.getTerms())
					innerTerms.add((Function)t);
				
				List<CQIE> result;
				if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN) 
					result = computePartialEvaluationInLeftJoin(innerTerms, rule, termidx);
				else
					result = computePartialEvaluation(innerTerms, rule, termidx);
					
				if (result == null || !result.isEmpty())
					return result;
			} 			
			termidx.pop();
		}

		return Collections.emptyList();
	}

	private List<CQIE> computePartialEvaluationInLeftJoin(List<Function> atoms, CQIE rule, Stack<Integer> termidx) {

		int nonBooleanAtomCounter = 0;

		for (int atomIdx = 0; atomIdx < atoms.size(); atomIdx++) {
			termidx.push(atomIdx);

			Function atom = atoms.get(atomIdx);

			if (atom.isDataFunction()) {
				nonBooleanAtomCounter += 1;
				
				// This is a data atom, it should be unfolded with the usual resolution algorithm.
				
				boolean isLeftJoinSecondArgument = nonBooleanAtomCounter == 2;
				List<CQIE> result = resolveDataAtom(atom, rule, termidx, true, isLeftJoinSecondArgument);
				if (result == null || !result.isEmpty())
					return result;
			}			
			else if (atom.isAlgebraFunction()) {
				nonBooleanAtomCounter += 1;
				
				// These may contain data atoms that need to be unfolded, we need to recursively unfold each term.
				
				List<Function> innerTerms = new ArrayList<>(3);
				for (Term t : atom.getTerms())
					innerTerms.add((Function)t);
				
				List<CQIE> result;
				if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN) 
					result = computePartialEvaluationInLeftJoin(innerTerms, rule, termidx);
				else
					result = computePartialEvaluation(innerTerms, rule, termidx);

				if (result == null || !result.isEmpty())
					return result;
			} 			
			termidx.pop();
		}

		return Collections.emptyList();
	}
	

	/***
	 * Applies a resolution step over a non-boolean/non-algebra atom (i.e. data
	 * atoms). The resolution step will will try to match the <strong>focus atom
	 * a</strong> in the input <strong>rule r</strong> againts the rules in
	 * {@link #unfoldingProgram}.
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
	 * @param focusAtom
	 *            The atom to be resolved.
	 * @param rule
	 *            The rule in which this atom resides
	 * @param termidx
	 *            The index of the atom in the rule (ifts nested, the stack
	 *            indicates the nesting, position by position, the first being
	 *            "list" positions (function term lists) and the last the focus
	 *            atoms position.
	 * @param atomindx
	 *            The location of the focustAtom in the currentlist
	 * @return <ul>
	 *         <li>null if there is no s whose head unifies with a, we return null.</li>
	 *         <li>empty list if the atom a is <strong>extensional
	 *         predicate (those that have no defining rules)</strong> or the
	 *         second data atom in a left join</li>
	 *         <li>a list with one ore more rules otherwise</li>
	 *         <ul>
	 * 
	 * @see UnifierUtilities
	 */
	private List<CQIE> resolveDataAtom(Function focusAtom, CQIE rule, Stack<Integer> termidx, boolean isLeftJoin,
			boolean isSecondAtomInLeftJoin) {

		/*
		 * Leaf predicates are ignored (as boolean or algebra predicates)
		 */
		Predicate pred = focusAtom.getFunctionSymbol();
		if (extensionalPredicates.contains(pred)) {
			// The atom is a leaf, that means that is a data atom that
			// has no resolvent rule, and marks the end points to compute
			// partial evaluations

			return Collections.emptyList();
		}
		/*
		 * This is a real data atom, it either generates something, or null
		 * (empty)
		 */

		List<CQIE> rulesDefiningTheAtom = ruleIndex.get(pred);
		if (rulesDefiningTheAtom == null) {
			// If there are none, the atom is logically empty, careful, LEFT JOIN alert!
			if (!isSecondAtomInLeftJoin)
				return null;
			else 
				return Collections.singletonList(generateNullBindingsForLeftJoin(focusAtom, rule, termidx));
		} 
		else {
			// Note, in this step result may get new CQIEs inside
			List<CQIE> result = generateResolutionResult(focusAtom, rule, termidx, rulesDefiningTheAtom, isLeftJoin,
					isSecondAtomInLeftJoin);
			
			if (result == null) {
				// this is the case for second atom in left join generating more
				// than one rule, we
				// must return an empty result indicating its already a partial
				// evaluation.
				return Collections.emptyList();
			} 
			else if (result.size() == 0) {
				if (!isSecondAtomInLeftJoin)
					return null;
				else 
					return Collections.singletonList(generateNullBindingsForLeftJoin(focusAtom, rule, termidx));
			}
			else 
				return result;
		}
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
	 * @param rule 
	 * @return a new query with the nested joins.
	 */
	private CQIE foldJOIN(CQIE rule) {
		
		List<Function> dataAtomsList = new LinkedList<>();
		List<Function> otherAtomsList = new LinkedList<>();

		for (Function a : rule.getBody()) {
			if (a.isDataFunction() || a.isAlgebraFunction()) 
				dataAtomsList.add(a);
			else 
				otherAtomsList.add(a);
		}

		if (dataAtomsList.size() <= 1) 
			return rule;
		
		Function foldedJoinAtom = dataAtomsList.remove(0);
		for (Function a : dataAtomsList)
			foldedJoinAtom = termFactory.getSPARQLJoin(foldedJoinAtom, a);
		
		otherAtomsList.add(0, foldedJoinAtom);
		
		CQIE newrule = termFactory.getCQIE(rule.getHead(), otherAtomsList);
		return newrule;
	}
	
	/***
	 * The list with all the successful resolutions against focusAtom. It
	 * will return a list with 0 ore more elements that result from successful
	 * resolution steps, or null if there are more than 1 successful resolution
	 * steps but focusAtom is the second atom of a left join (that is,
	 * isSecondAtomOfLeftJoin is true).
	 * 
	 * <p>
	 * Note the meaning of NULL in this method is different than the meaning of
	 * null and empty list in
	 * {@link #resolveDataAtom(it.unibz.krdb.obda.model.Function, it.unibz.krdb.obda.model.CQIE, java.util.Stack, int[], boolean, boolean)}  which is
	 * the caller method. The job of interpreting correctly the output of this
	 * method is done in the caller.
	 */

	private List<CQIE> generateResolutionResult(Function focusAtom, CQIE rule, Stack<Integer> termidx, 
			List<CQIE> rulesDefiningTheAtom, boolean isLeftJoin, boolean isSecondAtomOfLeftJoin) {

		List<CQIE> result = new LinkedList<>();

		int rulesGeneratedSoFar = 0;
		for (CQIE candidateRule : rulesDefiningTheAtom) {

			// getting a rule with unique variables 
			CQIE freshRule = termFactory.getFreshCQIECopy(candidateRule);
			Substitution mgu = UnifierUtilities.getMGU(freshRule.getHead(), focusAtom);
			if (mgu == null) {
				// Failed attempt 
				continue;
			}

			// We have a matching rule, now we prepare for the resolution step
			// if we are in a left join, we need to make sure the fresh rule
			// has only one data atom
			if (isLeftJoin) {
				freshRule = foldJOIN(freshRule);
			}

			// generating the new body of the rule
			CQIE partialEvalution = rule.clone();

			// locating the list that contains the current Function (either body
			// or inner term) and replacing the current atom, with the body of
			// the matching rule.
			List<Function> innerAtoms = getNestedList(termidx, partialEvalution);

			innerAtoms.remove((int) termidx.peek());
			innerAtoms.addAll((int) termidx.peek(), freshRule.getBody());

			SubstitutionUtilities.applySubstitution(partialEvalution, mgu, false);

			/***
			 * DONE WITH BASIC RESOLUTION STEP
			 */

			rulesGeneratedSoFar += 1;

			if (isSecondAtomOfLeftJoin && rulesGeneratedSoFar > 1) {
				// We had disjunction on the second atom of the leftjoin, that is,
				// more than two rules that unified. LeftJoin is not
				// distributable on the right component, hence, we cannot simply
				// generate 2 rules for the second atom.
				// 
				// The rules must be untouched, no partial evaluation is
				// possible. We must return the original rule.
				return null;
			}

			result.add(partialEvalution);
		}// end for candidate matches

		return result;
	}

	private CQIE generateNullBindingsForLeftJoin(Function focusLiteral, CQIE originalRuleWithLeftJoin, Stack<Integer> termidx) {

		log.debug("Empty evaluation - Data Function {}", focusLiteral);

		CQIE freshRule = originalRuleWithLeftJoin.clone();

		Stack<Integer> termidx1 = new Stack<>();
		termidx1.addAll(termidx);

		termidx1.pop();
		termidx1.add(0);
		List<Function> innerAtoms = getNestedList(termidx1, freshRule);

		int argumentAtoms = 0;
		List<Function> newbody = new LinkedList<>();
		Set<Variable> variablesArg1 = new HashSet<>();
		Set<Variable> variablesArg2 = new HashSet<>();

		// Here we build the new LJ body where we remove the 2nd
		// data atom
		for (Function atom : innerAtoms) {
			if (atom.isDataFunction() || atom.isAlgebraFunction()) {
				argumentAtoms++;
				// we found the first argument of the LJ, we need
				// the variables
				if (argumentAtoms == 1) {
					TermUtils.addReferencedVariablesTo(variablesArg1, atom);
					newbody.add(atom);
				} 
				else if (argumentAtoms == 2) {
					// Here we keep the variables of the second LJ
					// data argument
					TermUtils.addReferencedVariablesTo(variablesArg2, atom);

					// and we remove the variables that are in both arguments
					variablesArg2.removeAll(variablesArg1);
				} 
				else 
					newbody.add(atom);
			} 
			else 
				newbody.add(atom);
		}// end for rule body

		//freshRule.updateBody(newbody);
		replaceInnerLJ(freshRule, newbody, termidx1);
		
		Substitution unifier = SubstitutionUtilities.getNullifier(variablesArg2);

		// Now I need to add the null to the variables of the second
		// LJ data argument
		SubstitutionUtilities.applySubstitution(freshRule, unifier, false); // in-place unification
		
		return freshRule;
	}
	
	private static void replaceInnerLJ(CQIE rule, List<Function> replacementTerms, Stack<Integer> termidx) {
		
		if (termidx.size() > 1) {
			/*
			 * Its a nested term
			 */
			Function parentFunction = null;
			Term nestedTerm = null;
			for (int y = 0; y < termidx.size() - 1; y++) {
				int i = termidx.get(y);
				if (nestedTerm == null)
					nestedTerm = rule.getBody().get(i);
				else {
					parentFunction = (Function) nestedTerm;
					nestedTerm = ((Function) nestedTerm).getTerm(i);
				}
			}
			//Function focusFunction = (Function) nestedTerm;
			if (parentFunction == null) {
				//its just one Left Join, replace rule body directly
				rule.updateBody(replacementTerms);
				return;
			}
			List <Term> tempTerms = parentFunction.getTerms();
			tempTerms.remove(0);
			List <Term> newTerms = new LinkedList<Term>();
			newTerms.addAll(replacementTerms);
			newTerms.addAll(tempTerms);
			parentFunction.updateTerms(newTerms);
		} 
		else 
			throw new RuntimeException("Unexpected OPTIONAL condition!");
	}



	/***
	 * Returns the list of terms contained in the nested atom indicated by term
	 * idx. If termidx is empty, then this is the list of atoms in the body of
	 * the rule, otherwise the list correspond to the terms of the nested atom
	 * indicated by termidx viewed as a path of atoms. For example, if termidx =
	 * <2,4> then this atom returns the list of terms of the 4 atom, of the
	 * second atom in the body of the rule.
	 * 
	 * <p>
	 * Example two. IF the rule is q(x):-A(x), Join(R(x,y), Join(P(s),R(x,y) and
	 * termidx = <1,2>, then this method returns the the terms of the second
	 * join atom, ie.,
	 * <P(s),R(x,y)>
	 * ,
	 * 
	 * <p>
	 * note that this list is the actual list of terms of the atom, so
	 * manipulating the list will change the atom.
	 * 
	 * 
	 * @param termidx
	 * @param rule
	 * @return
	 */
	private static List<Function> getNestedList(Stack<Integer> termidx, CQIE rule) {

		if (termidx.size() > 1) {
			// it's a nested term
			int i0 = termidx.get(0);
			Function newfocusFunction = rule.getBody().get(i0);
			
			for (int y = 1; y < termidx.size() - 1; y++) {
				int i = termidx.get(y);
				newfocusFunction = (Function)newfocusFunction.getTerm(i);
			}

			return (List<Function>)(List)newfocusFunction.getTerms();
		} 
		else {
			// it's the body of the query
			return rule.getBody();
		}
	}

	private static Function getTerm(Stack<Integer> termidx, CQIE rule) {
		Function atom = null;
		if (termidx.size() > 1) {
			Stack<Integer> stack = new Stack<>();
			stack.addAll(termidx.subList(0, termidx.size() - 1));
			List<Function> innerTerms = getNestedList(stack, rule);
			atom = innerTerms.get(stack.peek());
		} 
		else {
			List<Function> body = rule.getBody();
			Integer peek = termidx.peek();
			if (peek >= body.size()) 
				return null;
			atom = body.get(peek);
		}
		return atom;
	}

}
