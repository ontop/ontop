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

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DatalogNormalizer {

	private final static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/***
	 * Normalizes all the rules in a Datalog program, pushing equalities into
	 * the atoms of the queries, when possible
	 * 
	 * @param dp
	 */
	public static DatalogProgram normalizeDatalogProgram(DatalogProgram dp) {
		DatalogProgram clone = fac.getDatalogProgram();
		clone.setQueryModifiers(dp.getQueryModifiers());
		for (CQIE cq : dp.getRules()) {
			CQIE normalized = normalizeCQIE(cq);
			if (normalized != null) {
				clone.appendRule(normalized);
			}
		}
		return clone;
	}

	public static CQIE normalizeCQIE(CQIE query) {
		CQIE result = unfoldANDTrees(query);
		// result = normalizeEQ(result);
		result = unfoldJoinTrees(result);
		result = pullUpNestedReferences(result, true);
		if (result == null)
			return null;
		return result;
	}

	/***
	 * This expands all AND trees into individual comparison atoms in the body
	 * of the query. Nested AND trees inside Join or LeftJoin atoms are not
	 * touched.
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE unfoldANDTrees(CQIE query) {
		CQIE result = query.clone();
		List<Function> body = result.getBody();
		/* Collecting all necessary conditions */
		for (int i = 0; i < body.size(); i++) {
			Function currentAtom = body.get(i);
			if (currentAtom.getFunctionSymbol() == OBDAVocabulary.AND) {
				body.remove(i);
				body.addAll(getUnfolderAtomList(currentAtom));
			}
		}
		return result;
	}

	/***
	 * This expands all Join that can be directly added as conjuncts to a
	 * query's body. Nested Join trees inside left joins are not touched.
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE unfoldJoinTrees(CQIE query) {
		return unfoldJoinTrees(query, true);
	}

	/***
	 * This expands all Join that can be directly added as conjuncts to a
	 * query's body. Nested Join trees inside left joins are not touched.
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE unfoldJoinTrees(CQIE query, boolean clone) {
		if (clone)
			query = query.clone();
		List<Function> body = query.getBody();
		unfoldJoinTrees(body, true);
		return query;
	}

	/***
	 * This expands all Join that can be directly added as conjuncts to a
	 * query's body. Nested Join trees inside left joins are not touched.
	 * <p>
	 * In addition, we will remove any Join atoms that only contain one single
	 * data atom, i.e., the join is not a join, but a table reference with
	 * conditions. These kind of atoms can result from the partial evaluation
	 * process and should be eliminated. The elimination takes all the atoms in
	 * the join (the single data atom plus possibly extra boolean conditions and
	 * adds them to the node that is the parent of the join).
	 * 
	 * @param query
	 * @return
	 */
	public static void unfoldJoinTrees(List body, boolean isJoin) {
		/* Collecting all necessary conditions */
		for (int i = 0; i < body.size(); i++) {
			Function currentAtom = (Function) body.get(i);
			if (!currentAtom.isAlgebraFunction())
				continue;
			if (currentAtom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN)
				unfoldJoinTrees(currentAtom.getTerms(), false);
			if (currentAtom.getFunctionSymbol() == OBDAVocabulary.SPARQL_JOIN) {
				unfoldJoinTrees(currentAtom.getTerms(), true);
				int dataAtoms = countDataItems(currentAtom.getTerms());
				if (isJoin || dataAtoms == 1) {
					body.remove(i);
					for (int j = currentAtom.getTerms().size() - 1; j >= 0; j--) {
						Term term = currentAtom.getTerm(j);
						Function asAtom = (Function)term;
						if (!body.contains(asAtom))
							body.add(i, asAtom);
					}
					i -= 1;
				}
			}
		}
	}

	public static CQIE foldJoinTrees(CQIE query, boolean clone) {
		if (clone)
			query = query.clone();
		List<Function> body = query.getBody();
		foldJoinTrees(body, false);
		return query;
	}

	public static void foldJoinTrees(List atoms, boolean isJoin) {
		List<Function> dataAtoms = new LinkedList<>();
		List<Function> booleanAtoms = new LinkedList<>();

		/*
		 * Collecting all data and boolean atoms for later processing. Calling
		 * recursively fold Join trees on any algebra function.
		 */
		for (Object o : atoms) {
			Function atom = (Function) o;
			if (atom.isBooleanFunction()) {
				booleanAtoms.add(atom);
			} else {
				dataAtoms.add(atom);
				if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN)
					foldJoinTrees(atom.getTerms(), false);
				if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_JOIN)
					foldJoinTrees(atom.getTerms(), true);
			}

		}

		if (!isJoin || dataAtoms.size() <= 2)
			return;

		/*
		 * We process all atoms in dataAtoms to make only BINARY joins. Taking
		 * two at a time and replacing them for JOINs, until only two are left.
		 * All boolean conditions of the original join go into the first join
		 * generated. It always merges from the left to the right.
		 */
		while (dataAtoms.size() > 2) {
			Function joinAtom = fac.getSPARQLJoin(dataAtoms.remove(0), dataAtoms.remove(0));
			joinAtom.getTerms().addAll(booleanAtoms);
			booleanAtoms.clear();

			dataAtoms.add(0, joinAtom);
		}
		atoms.clear();
		atoms.addAll(dataAtoms);

	}

	/***
	 * Counts the number of data atoms in this list of terms. Not recursive.
	 * 
	 * @param terms
	 * @return
	 */
	public static int countDataItems(List<?extends Term> terms) {
		int count = 0;
		for (Term currentTerm : terms) {
			Function currentAtom = (Function)currentTerm;
			if (!currentAtom.isBooleanFunction())
				count += 1;
		}
		return count;
	}



	/***
	 * This method introduces new variable names in each data atom and
	 * equalities to account for JOIN operations. This method is called before
	 * generating SQL queries and allows to avoid cross refrences in nested
	 * JOINs, which generate wrong ON or WHERE conditions.
	 * 
	 * 
	 * @param currentTerms
	 * @param substitutions
	 */
	public static void pullOutEqualities(CQIE query) {
		Substitution substitutions = new SubstitutionImpl();
		int[] newVarCounter = { 1 };

		Set<Function> booleanAtoms = new HashSet<>();
		List<Function> equalities = new LinkedList<>();
		pullOutEqualities(query.getBody(), substitutions, equalities, newVarCounter, false);
		List<Function> body = query.getBody();
		body.addAll(equalities);

		/*
		 * All new variables have been generated, the substitutions also, we
		 * need to apply them to the equality atoms and to the head of the
		 * query.
		 */

		SubstitutionUtilities.applySubstitution(query, substitutions, false);

	}

//	private static BranchDepthSorter sorter = new BranchDepthSorter();

	/***
	 * Compares two atoms by the depth of their JOIN/LEFT JOIN branches. This is
	 * used to sort atoms in a query bodybased on the depth, to assure the
	 * depesth branches are visited first.
	 * 
	 * @author mariano
	 * 
	 */
/*	
	private static class BranchDepthSorter implements Comparator<Function> {

		public int getDepth(Function term) {
			int max = 0;
			if (term.isDataFunction() || term.isBooleanFunction() || term.isDataTypeFunction()) {
				return 0;
			} else {
				List<Term> innerTerms = term.getTerms();

				for (Term innerTerm : innerTerms) {
					int depth = getDepth((Function) innerTerm);
					max = Math.max(max, depth);
				}
				max += 1;
			}

			// System.out.println("MAX: " + max);
			return max;
		}

		@Override
		public int compare(Function arg0, Function arg1) {
			return getDepth(arg1) - getDepth(arg0);
		}
	}
*/
	
	/***
	 * Adds a trivial equality to a LeftJoin in case the left join doesn't have
	 * at least one boolean condition. This is necessary to have syntactically
	 * correct LeftJoins in SQL.
	 * 
	 * @param leftJoin
	 */
	private static void addMinimalEqualityToLeftJoin(Function leftJoin) {
		int booleanAtoms = 0;
		boolean isLeftJoin = leftJoin.isAlgebraFunction();
		for (Term term : leftJoin.getTerms()) {
			Function f = (Function) term;
			if (f.isAlgebraFunction()) {
				addMinimalEqualityToLeftJoin(f);
			}
			if (f.isBooleanFunction())
				booleanAtoms += 1;
		}
		if (isLeftJoin && booleanAtoms == 0) {
			Function trivialEquality = fac.getFunctionEQ(fac.getConstantLiteral("1", COL_TYPE.INTEGER),
					fac.getConstantLiteral("1", COL_TYPE.INTEGER));
			leftJoin.getTerms().add(trivialEquality);
		}
	}

	public static void addMinimalEqualityToLeftJoin(CQIE query) {
		for (Function f : query.getBody()) {
			if (f.isAlgebraFunction()) {
				addMinimalEqualityToLeftJoin(f);
			}
		}
	}

	/***
	 * This method introduces new variable names in each data atom and
	 * equalities to account for JOIN operations. This method is called before
	 * generating SQL queries and allows to avoid cross references in nested
	 * JOINs, which generate wrong ON or WHERE conditions.
	 * 
	 * 
	 * @param currentTerms
	 * @param substitutions
	 */
	private static void pullOutEqualities(List currentTerms, Substitution substitutions, List<Function> eqList,
			int[] newVarCounter, boolean isLeftJoin) {

		for (int i = 0; i < currentTerms.size(); i++) {

			Term term = (Term) currentTerms.get(i);

			/*
			 * We don't expect any functions as terms, data atoms will only have
			 * variables or constants at this level. This method is only called
			 * exactly before generating the SQL query.
			 */
			if (!(term instanceof Function))
				throw new RuntimeException("Unexpected term found while normalizing (pulling out equalities) the query.");

			Function atom = (Function) term;
			List<Term> subterms = atom.getTerms();

			if (atom.isAlgebraFunction()) {
				if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN)
					pullOutEqualities(subterms, substitutions, eqList, newVarCounter, true);
				else
					pullOutEqualities(subterms, substitutions, eqList, newVarCounter, false);

			} else if (atom.isBooleanFunction()) {
				continue;

			}

			// rename/substitute variables

			for (int j = 0; j < subterms.size(); j++) {
				Term subTerm = subterms.get(j);
				if (subTerm instanceof Variable) {

					VariableImpl var1 = (VariableImpl) subTerm;
					Variable var2 = (Variable) substitutions.get(var1);

					if (var2 == null) {
						/*
						 * No substitution exists, hence, no action but generate
						 * a new variable and register in the substitutions, and
						 * replace the current value with a fresh one.
						 */
						var2 = fac.getVariable(var1.getName() + "f" + newVarCounter[0]);

						substitutions.put(var1, var2);
						subterms.set(j, var2);

					} else {

						/*
						 * There already exists one, so we generate a fresh,
						 * replace the current value, and add an equality
						 * between the substitution and the new value.
						 */

						if (atom.isDataFunction()) {
							Variable newVariable = fac.getVariable(var1.getName() + newVarCounter[0]);

							subterms.set(j, newVariable);
							Function equality = fac.getFunctionEQ(var2, newVariable);
							eqList.add(equality);

						} else { // if its not data function, just replace
									// variable
							subterms.set(j, var2);
						}
					}
					newVarCounter[0] += 1;
				} else if (subTerm instanceof Constant) {
					/*
					 * This case was necessary for query 7 in BSBM
					 */
					/**
					 * A('c') Replacing the constant with a fresh variable x and
					 * adding an quality atom ,e.g., A(x), x = 'c'
					 */
					// only relevant if in data function?
					if (atom.isDataFunction()) {
						Variable var = fac.getVariable("f" + newVarCounter[0]);
						newVarCounter[0] += 1;
						Function equality = fac.getFunctionEQ(var, subTerm);
						subterms.set(j, var);
						eqList.add(equality);
					}

				}
			}

			currentTerms.addAll(i + 1, eqList);
			i = i + eqList.size();
			eqList.clear();
		}
	}

	// Saturate equalities list to explicitly state JOIN conditions and
	// therefore avoid having
	// to rely on DBMS for nested JOIN optimisations (PostgreSQL case for BSBM
	// Q3)
	private static void saturateEqualities(Set<Function> boolSet) {
		List<Set<Term>> equalitySets = new ArrayList<>();
		Iterator<Function> iter = boolSet.iterator();
		while (iter.hasNext()) {
			Function eq = iter.next();
			if (eq.getFunctionSymbol() != OBDAVocabulary.EQ)
				continue;
			Term v1 = eq.getTerm(0);
			Term v2 = eq.getTerm(1);
			if (equalitySets.size() == 0) {
				Set<Term> firstSet = new LinkedHashSet<>();
				firstSet.add(v1);
				firstSet.add(v2);
				equalitySets.add(firstSet);
				continue;
			}
			for (int k = 0; k < equalitySets.size(); k++) {
				Set<Term> set = equalitySets.get(k);
				if (set.contains(v1)) {
					set.add(v2);
					continue;
				}
				if (set.contains(v2)) {
					set.add(v1);
					continue;
				}
				if (k == equalitySets.size() - 1) {
					Set<Term> newSet = new LinkedHashSet<>();
					newSet.add(v1);
					newSet.add(v2);
					equalitySets.add(newSet);
					break;
				}
			}

		}

		for (int k = 0; k < equalitySets.size(); k++) {
			List<Term> varList = new ArrayList<>(equalitySets.get(k));
			for (int i = 0; i < varList.size() - 1; i++) {
				for (int j = i + 1; j < varList.size(); j++) {
					Function equality = fac.getFunctionEQ(varList.get(i), varList.get(j));
					boolSet.add(equality);
				}
			}
		}
	}

	/****
	 * Gets all the variables that are defined in this list of atoms, except in
	 * atom i
	 * 
	 * @param atoms
	 * @return
	 */
	private static Set<Variable> getDefinedVariables(List atoms) {
		Set<Variable> currentLevelVariables = new HashSet<Variable>();
		for (Object l : atoms) {
			Function atom = (Function) l;
			if (atom.isBooleanFunction()) {
				continue;
			} else if (atom.isAlgebraFunction()) {
				currentLevelVariables.addAll(getDefinedVariables(atom.getTerms()));
			} else {
				currentLevelVariables.addAll(atom.getReferencedVariables());
			}
		}
		return currentLevelVariables;
	}

	/***
	 * Collects all the variables that appear in all other branches (these are
	 * atoms in the list of atoms) except in focusBranch.
	 * <p>
	 * Variables are considered problematic because they are out of the scope of
	 * focusBranch. There are not visible in an SQL algebra tree.
	 * <p>
	 * Note that this method should only be called after callin pushEqualities
	 * and pullOutEqualities on the CQIE. This is to assure that there are no
	 * transitive equalities to take care of and that each variable in a data
	 * atom is unique.
	 * 
	 * @param atoms
	 * @param branch
	 * @return
	 */
	private static Set<Variable> getProblemVariablesForBranchN(List atoms, int focusBranch) {
		Set<Variable> currentLevelVariables = new HashSet<Variable>();
		for (int i = 0; i < atoms.size(); i++) {
			if (i == focusBranch)
				continue;
			Function atom = (Function) atoms.get(i);
			if (atom.isDataFunction()) {
				currentLevelVariables.addAll(atom.getReferencedVariables());
			} else if (atom.isAlgebraFunction()) {
				currentLevelVariables.addAll(getDefinedVariables(atom.getTerms()));
			} else {
				// noop
			}
		}
		return currentLevelVariables;
	}

	/***
	 * This will
	 * 
	 * @param query
	 * @return
	 */
	public static CQIE pullUpNestedReferences(CQIE query, boolean clone) {

		if (clone)
			query = query.clone();

		List<Function> body = query.getBody();

		Function head = query.getHead();
		/*
		 * This set is only for reference
		 */
		Set<Variable> currentLevelVariables = new HashSet<Variable>();
		/*
		 * This set will be modified in the process
		 */
		Set<Function> resultingBooleanConditions = new HashSet<Function>();

		/*
		 * Analyze each atom that is a Join or LeftJoin, the process will
		 * replace everything needed.
		 */
		int[] freshVariableCount = { 0 };
		pullUpNestedReferences(body, head, currentLevelVariables, resultingBooleanConditions, freshVariableCount);

		/*
		 * Adding any remiding boolean conditions to the top level.
		 */
		for (Function condition : resultingBooleanConditions) {
			body.add(condition);
		}

		return query;
	}

	private static void pullUpNestedReferences(List currentLevelAtoms, Function head, Set<Variable> problemVariables,
			Set<Function> booleanConditions, int[] freshVariableCount) {

		/*
		 * Call recursively on each atom that is a Join or a LeftJoin passing
		 * the variables of this level
		 */
		for (int focusBranch = 0; focusBranch < currentLevelAtoms.size(); focusBranch++) {
			Object l = currentLevelAtoms.get(focusBranch);

			Function atom = (Function) l;
			if (!(atom.getFunctionSymbol() instanceof AlgebraOperatorPredicate))
				continue;
			// System.out
			// .println("======================== INTO ALGEBRA =====================");

			List<Term> terms = atom.getTerms();

			Set<Variable> nestedProblemVariables = new HashSet<Variable>();

			nestedProblemVariables.addAll(problemVariables);
			nestedProblemVariables.addAll(getProblemVariablesForBranchN(currentLevelAtoms, focusBranch));

			pullUpNestedReferences(terms, head, nestedProblemVariables, booleanConditions, freshVariableCount);
		}

		// Here we need to saturate Equalities
		saturateEqualities(booleanConditions);

		/*
		 * Add the resulting equalities that belong to the current level. An
		 * equality belongs to this level if ALL its variables are defined at
		 * the current level and not at the upper levels.
		 */
		Set<Function> removedBooleanConditions = new HashSet<Function>();
		// System.out.println("Checking boolean conditions: "
		// + booleanConditions.size());
		for (Function equality : booleanConditions) {
			Set<Variable> atomVariables = equality.getReferencedVariables();

			boolean belongsToThisLevel = true;
			for (Variable var : atomVariables) {
				if (!problemVariables.contains(var))
					continue;
				belongsToThisLevel = false;
			}
			if (!belongsToThisLevel)
				continue;

			currentLevelAtoms.add(equality);
			removedBooleanConditions.add(equality);
		}
		booleanConditions.removeAll(removedBooleanConditions);

		/*
		 * Review the atoms of the current level and generate any variables,
		 * equalities needed at this level (no further recursive calls).
		 * Generate new variables for each variable that appears at this level,
		 * and also appears at a top level. We do this only for data atoms.
		 * 
		 * We do this by creating a substitution for each of the, and then
		 * applying the substitution. We also add an equality for each
		 * substitution we created.
		 */

		/*
		 * Review the current boolean atoms, if the refer to upper level
		 * variables then remove them from the current level and add them to the
		 * equalities set for the upper level.
		 * 
		 * If an contains at least 1 variable that is mentioned in an upper
		 * level, then this condition is removed from the current level and
		 * moved forward by adding it to the booleanConditions set.
		 */

		for (int index = 0; index < currentLevelAtoms.size(); index++) {
			// System.out.println(index);
			// System.out.println(currentLevelAtoms.size());
			Term l = (Term) currentLevelAtoms.get(index);
			Function atom = (Function) l;
			// System.out
			// .println(atom.getFunctionSymbol().getClass() + " " + atom);
			if (!(atom.getFunctionSymbol() instanceof BooleanOperationPredicate))
				continue;
			Set<Variable> variables = atom.getReferencedVariables();
			boolean belongsUp = false;

			search: for (Variable var : variables) {
				if (problemVariables.contains(var)) {

					// /*
					// * looking for an equality that might indicate that in
					// fact,
					// * the atom doesn't belong up because there is an equality
					// * at this level that mentiones the "unsafe variable" and
					// a
					// * "safe variable" at the same time. (this pattern happens
					// * due to the call to
					// DatalogNormalizer.pullOutEqualities()
					// * that happens before pullingUp
					// */
					// for (int idx2 = 0; idx2 < currentLevelAtoms.size();
					// idx2++) {
					// NewLiteral l2 = (NewLiteral) currentLevelAtoms
					// .get(idx2);
					// if (!(l2 instanceof Function))
					// continue;
					// Function f2 = (Function) l2;
					// if (f2.getPredicate() != OBDAVocabulary.EQ)
					// continue;
					// List<NewLiteral> equalityVariables = f2.getTerms();
					// if (equalityVariables.contains(var)) {
					// // NewLiteral var2 = equalityVariables.get(0);
					// // if (!(var2 instanceof Variable))
					// // continue;
					// if (!(problemVariables
					// .containsAll(equalityVariables))) {
					// /*
					// * we found that var is acutally safe, there is
					// * an equality that bounds it to a data atom in
					// * the current level
					// */
					// continue search;
					// }
					// }
					//
					// }

					belongsUp = true;
					break;
				}
			}

			if (!belongsUp)
				continue;

			// Belongs up, removing and pushing up

			// System.out.println("REMOVED!!!!");

			currentLevelAtoms.remove(index);
			index -= 1;
			booleanConditions.add(atom);
		}

	}

	/***
	 * Takes an AND atom and breaks it into a list of individual condition
	 * atoms.
	 * 
	 * @param atom
	 * @return
	 */
	public static List<Function> getUnfolderAtomList(Function atom) {
		if (atom.getFunctionSymbol() != OBDAVocabulary.AND) {
			throw new InvalidParameterException();
		}
		List<Term> innerFunctionalTerms = new LinkedList<Term>();
		for (Term term : atom.getTerms()) {
			innerFunctionalTerms.addAll(getUnfolderTermList((Function) term));
		}
		List<Function> newatoms = new LinkedList<Function>();
		for (Term innerterm : innerFunctionalTerms) {
			Function f = (Function) innerterm;
			Function newatom = fac.getFunction(f.getFunctionSymbol(), f.getTerms());
			newatoms.add(newatom);
		}
		return newatoms;
	}

	/***
	 * Takes an AND atom and breaks it into a list of individual condition
	 * atoms.
	 * 
	 * @param atom
	 * @return
	 */
	public static List<Term> getUnfolderTermList(Function term) {

		List<Term> result = new LinkedList<Term>();

		if (term.getFunctionSymbol() != OBDAVocabulary.AND) {
			result.add(term);
		} else {
			List<Term> terms = term.getTerms();
			for (Term currentterm : terms) {
				if (currentterm instanceof Function) {
					result.addAll(getUnfolderTermList((Function) currentterm));
				} else {
					result.add(currentterm);
				}
			}
		}

		return result;
	}

	// THE FOLLOWING COMMENT IS TAKEN FROM THE CODE ABOVE, THE FUNCTIONALITY IT
	// DESCRIBES
	// WAS IMPLEMENTED BELLOW
	/*
	 * Here we collect boolean atoms that have conditions of atoms on the left
	 * of left joins. These cannot be put in the conditions of LeftJoin(...)
	 * atoms as inside terms, since these conditions have to be applied no
	 * matter what. Keeping them there makes them "optional", i.e., or else
	 * return NULL. Hence these conditions have to be pulled up to the nearest
	 * JOIN in the upper levels in the branches. The pulloutEqualities method
	 * iwll do this, however if there are still remaiing some by the time it
	 * finish, we must add them to the body of the CQIE as normal conditions to
	 * the query (WHERE clauses)
	 */

	public static void pullOutLeftJoinConditions(CQIE query) {
		Set<Function> booleanAtoms = new HashSet<Function>();
		Set<Function> tempBooleans = new HashSet<Function>();
		List<Function> body = query.getBody();
		
		pullOutLJCond(body, booleanAtoms, false, tempBooleans, false);
		body.addAll(booleanAtoms);
	}

	private static void pullOutLJCond(List currentTerms, Set<Function> leftConditionBooleans, boolean isLeftJoin,
			Set<Function> currentBooleans, boolean isSecondJoin) {
		boolean firstDataAtomFound = false;
		boolean secondDataAtomFound = false;
		boolean is2 = false;
		List tempTerms = new LinkedList<>();
		tempTerms.addAll(currentTerms);
		Set<Function> tempConditionBooleans = new HashSet<Function>();
		
		if (currentTerms.size() == 0) {
			/*
			 * This can happen when there are mappings with no body (facts)
			 */
			return;
		}
		
		Term firstT = (Term) currentTerms.get(0);
		if (!(firstT instanceof Function))
			throw new RuntimeException("Unexpected term found while normalizing (pulling out conditions) the query.");

		Function f = (Function) firstT;

		for (int i = 0; i < currentTerms.size(); i++) {
			Term term = (Term) currentTerms.get(i);

			Function atom = (Function) term;
			List<Term> subterms = atom.getTerms();

			// if we are in left join then pull out boolean conditions that
			// correspond to first data atom

			if (atom.isDataFunction() || atom.isAlgebraFunction()) {
				// if an atom is a Join then go inside, otherwise its
				// Data Function
				if (atom.isAlgebraFunction()) {
					if (i != 0)
						is2 = true;
					if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN)
						pullOutLJCond(subterms, leftConditionBooleans, true, currentBooleans, is2);
					else
						pullOutLJCond(subterms, leftConditionBooleans, false, currentBooleans, is2);

				}

				// if first data atom is found already then this is the second
				if (firstDataAtomFound)
					secondDataAtomFound = true;
				// if both are false then its the first data atom
				if (!firstDataAtomFound && !secondDataAtomFound) {
					firstDataAtomFound = true;
				}

				if (secondDataAtomFound && isLeftJoin) {
					tempTerms.addAll(currentBooleans);
				}

			} else { // its boolean atom
				if (firstDataAtomFound && !secondDataAtomFound) {
					// they need to be pulled out of LEFT
					// JOINs ON clause
					if (isLeftJoin) {
						tempTerms.remove(atom);
						// currentTerms.remove(atom);
						// i--;
						tempConditionBooleans.add(atom);
						// leftConditionBooleans.add(atom);
					}

				}
			}

		}
		// tempTerms is currentTerms with "bad" boolean conditions removed
		// now add all these removed conditions at the end
		// and update current terms

		// if we are at the top level Left Join then push booleans into
		// where clause
		// otherwise push it into upper Left Join ON clause

		// if we are in a Join that is a second data atom, then dont push it all
		// the way up
		if (!isSecondJoin) {
			leftConditionBooleans.addAll(tempConditionBooleans);
		}
		currentTerms.clear();
		currentTerms.addAll(tempTerms);
		// currentTerms.addAll(currentBooleans);
		currentBooleans.clear();
		currentBooleans.addAll(tempConditionBooleans);

	}
}
