package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.*;
import fj.data.List;
import fj.data.Option;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.impl.FunctionalTermImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.ArrayList;
import java.util.Set;

/**
 * Pulls out equalities.
 *
 * Limit: JOIN meta-predicates are not considered (--> no possibility to have specific ON conditions at the proper level).
 */
public class ExtractEqualityNormalizer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * TODO: explain
     *
     * Entry point
     */
    public static CQIE extractEqualitiesAndNormalize(final CQIE initialRule) {
        CQIE newRule = initialRule.clone();


        /**
         * Mutable object.
         */
        final VariableGenerator variableGenerator = new VariableGenerator(initialRule);

        ExtractEqNormResult result = normalizeSameLevelAtoms(List.iterableList(newRule.getBody()), new SubstitutionImpl(), variableGenerator);

        newRule.updateBody(new ArrayList(result.getAllAtoms().toCollection()));
        return newRule;
    }

    /**
     * Builds out a ExtractEqNormResult by aggregating the results of atoms.
     *
     * The initialSubstitution is "updated" by each atom of this list. It maps
     * the old and new names of the variables  "pull out into equalities".
     */
    private static ExtractEqNormResult normalizeSameLevelAtoms(final List<Function> initialAtoms,
                                                               final Substitution initialSubstitution,
                                                               final VariableGenerator variableGenerator) {
        /**
         * Normalizes all the atoms while updating a "common" substitution.
         * Because this substitution, a foldLeft approach had to be adopted (instead of a simple map).
         *
         * Accumulates a list of result (initially null).
         *
         * TODO: replace by a map thanks to the mutable substitution?
         */
        List<ExtractEqNormResult> initialAccumulatedResults = null;
        List<ExtractEqNormResult> results = initialAtoms.foldLeft(new F2<List<ExtractEqNormResult>, Function,
                List<ExtractEqNormResult>>() {
            @Override
            public List<ExtractEqNormResult> f(List<ExtractEqNormResult> accumulatedResults, Function atom) {
                Substitution currentSubstitution;
                /**
                 * First atom: uses the initial substitution
                 */
                if (accumulatedResults == null) {
                    currentSubstitution = initialSubstitution;
                }
                /**
                 * Non-first atom: takes the substitution related to the previous atom.
                 */
                else {
                    currentSubstitution = accumulatedResults.head().getCurrentSubstitution();
                }
                ExtractEqNormResult atomResult = normalizeAtom(atom, currentSubstitution, variableGenerator);
                // Appends to the HEAD of the list
                return List.cons(atomResult, accumulatedResults);
            }
        }, initialAccumulatedResults)
                /**
                 * Because new results were added at the head, the list as to
                 * be reversed to follow the atom order.
                 */
                .reverse();

        /**
         * Aggregates the extracted results into one ExtractEqNormResult.
         *
         * Extracts the last substitution, all the non-pushable and pushable atoms.
         */
        // Last substitution
        Substitution lastSubstitution = results.last().getCurrentSubstitution();
        // All non pushable atoms
        List<Function> allNonPushableAtoms = results.bind(new F<ExtractEqNormResult, List<Function>>() {
            @Override
            public List<Function> f(ExtractEqNormResult result) {
                return result.getNonPushableAtoms();
            }
        });
        // All pushable atoms
        List<Function> allPushableAtoms = results.bind(new F<ExtractEqNormResult, List<Function>>() {
            @Override
            public List<Function> f(ExtractEqNormResult result) {
                return result.getPushableAtoms();
            }
        });

        return new ExtractEqNormResult(allNonPushableAtoms, allPushableAtoms, lastSubstitution);
    }

    /**
     * Applies the "pull out equalities" normalization to an atom according to its type.
     */
    private static ExtractEqNormResult normalizeAtom(Function atom, Substitution currentSubstitution,
                                                     VariableGenerator variableGenerator) {
        /**
         * Meta-atoms (algebra)
         */
        if (atom.isAlgebraFunction()) {
            Predicate functionSymbol = atom.getFunctionSymbol();
            if (functionSymbol.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
                return normalizeLeftJoin(atom, currentSubstitution, variableGenerator);
            }
            else if (functionSymbol.equals(OBDAVocabulary.SPARQL_JOIN)) {
                throw new RuntimeException("Not implemented (yet). We do not expect JOIN meta-predicates at " +
                        "the pull-out equalities level.");
            }
        }
        /**
         * Data atom
         */
        else if (atom.isDataFunction()) {
            return normalizeDataAtom(atom, currentSubstitution, variableGenerator);
        }
        /**
         * Filter atom (boolean)
         */
        else if (atom.isBooleanFunction()) {
            return normalizeFilterAtom(atom, currentSubstitution);
        }
        /**
         * Other atom type: treat it as non-pushable.
         */
        return ExtractEqNormResult.constructFromNonPushableAtom(atom, currentSubstitution);
    }

    /**
     * TODO: implement it.
     */
    private static ExtractEqNormResult normalizeDataAtom(Function atom, AppendableSubstitution currentSubstitution,
                                                         final VariableGenerator variableGenerator) {

        //TODO: change it
        P3<List<Term>, List<Option<Function>>, Substitution> initialDecomposition = null;

        /**
         * TODO: Create a MutableSubstitution class and use map instead of fold.
         */
        P3<List<Term>, List<Option<Function>>, Substitution> decomposition = List.iterableList(atom.getTerms()).foldLeft(new F2<P3<List<Term>, List<Option<Function>>, Substitution>, Term, P3<List<Term>, List<Option<Function>>, Substitution>>() {
            @Override
            public P3<List<Term>, List<Option<Function>>, Substitution> f(P3<List<Term>, List<Option<Function>>, Substitution> accumulatedDecomposition, Term term) {
                P3<Term, Option<Function>, Substitution> termTriple = normalizeTerm(term, accumulatedDecomposition._3(), variableGenerator);

                /**
                 * Converts
                 * TODO: explain
                 **
                 *
                 */
                List<Term> inversedTerms = List.cons(termTriple._1(), accumulatedDecomposition._1());
                List<Option<Function>> inversedEqAtoms = List.cons(termTriple._2(), accumulatedDecomposition._2());
                return P.p(inversedTerms, inversedEqAtoms, termTriple._3());
            }
        }, initialDecomposition);

        java.util.List<Term> newSubTerms = new ArrayList<>(decomposition._1().reverse().toCollection());
        Function newDataAtom = DATA_FACTORY.getFunction(atom.getFunctionSymbol(), newSubTerms);
        List<Function> nonPushableAtoms = List.cons(newDataAtom, List.<Function>nil());

        List<Function> eqAtoms = Option.somes(decomposition._2().reverse());

        return new ExtractEqNormResult(nonPushableAtoms, eqAtoms, decomposition._3());
    }

    /**
     * Filter (boolean) atom: --> proposed it as pushable.
     */
    private static ExtractEqNormResult normalizeFilterAtom(Function atom, Substitution currentSubstitution) {
        return ExtractEqNormResult.constructFromPushableAtom(atom, currentSubstitution);
    }


    /**
     * TODO: explain it
     *
     */
    private static ExtractEqNormResult normalizeLeftJoin(final Function leftJoinMetaAtom,
                                                         final Substitution initialSubstitution,
                                                         final VariableGenerator variableGenerator) {

        /**
         * TODO: may change with JOIN meta-predicates
         */
        final P2<List<Function>, List<Function>> splittedAtoms = splitLeftJoinSubAtoms(leftJoinMetaAtom);

        final List<Function> initialLeftAtoms = splittedAtoms._1();
        final List<Function> initialRightAtoms = splittedAtoms._2();

        ExtractEqNormResult leftNormalizationResults = normalizeSameLevelAtoms(initialLeftAtoms, initialSubstitution, variableGenerator);

        ExtractEqNormResult rightNormalizationResults = normalizeSameLevelAtoms(initialRightAtoms,
                leftNormalizationResults.getCurrentSubstitution(), variableGenerator);

        /**
         * TODO: explain. "Blocking" criteria.
         */
        List<Function> remainingLJAtoms = leftNormalizationResults.getNonPushableAtoms().append(rightNormalizationResults.getAllAtoms());
        List<Function> pushedUpAtoms = leftNormalizationResults.getPushableAtoms();
        Substitution lastSubstitution = rightNormalizationResults.getCurrentSubstitution();

        return new ExtractEqNormResult(remainingLJAtoms, pushedUpAtoms, lastSubstitution);
    }


    /**
     * TODO: implement it
     */
    private static P2<List<Function>, List<Function>> splitLeftJoinSubAtoms(Function leftJoinMetaAtom) {
        return null;
    }

    /**
     * TODO: implement it
     *
     */
    private static P3<Term, Option<Function>, Substitution> normalizeTerm(Term term, AppendableSubstitution mutableSubstitution,
                                                                          VariableGenerator variableGenerator, boolean inDataAtom) {
        /**
         * Variable: rename it and adds equalities (if needed for a JOIN or LEFT-JOIN).
         */
        if (term instanceof VariableImpl) {
            return normalizeVariable((VariableImpl) term, mutableSubstitution, variableGenerator);
        }
        /**
         * Constant: replaces it by a variable in the atom and creates a equality.
         */
        else if (term instanceof Constant) {
                /*
                 * This case was necessary for query 7 in BSBM
                 */
            /**
             * A('c') Replacing the constant with a fresh variable x and
             * adding an quality atom ,e.g., A(x), x = 'c'
             */
            // only relevant if in data function?
            if (inDataAtom) {
                Variable var = variableGenerator.generateNewVariable();
                Function equality = DATA_FACTORY.getFunctionEQ(var, term);
                return P.p((Term) var, Option.some(equality), (Substitution) mutableSubstitution);
            }
        }
        /**
         * Functional term
         */
        else if (term instanceof Function) {
            Predicate head = ((Function) term).getFunctionSymbol();

            if (head.isDataTypePredicate()) {

                // This case is for the ans atoms that might have
                // functions in the head. Check if it is needed.
                Set<Variable> subtermsset = term.getReferencedVariables();

                // Right now we support only unary functions!!
                /**
                 * TODO: continue refactoring!
                 */
                for (Variable var : subtermsset) {
                    renameTerm(substitution, eqList, atom, subterms, j, (Function) subTerm, var, variableGenerator);
                }
            }
        }
        /**
         * By default
         */
        return P.p(term, Option.<Function>none(), (Substitution) mutableSubstitution);
    }

    private static P3<Term, Option<Function>, Substitution> normalizeVariable(VariableImpl variable,
                                                                              AppendableSubstitution mutableSubstitution,
                                                                              VariableGenerator variableGenerator) {
        return null;
    }

    /**
     * SIDE-EFFECT function!
     *
     * Renames variables and adds the corresponding equalities to the eqList.
     *
     * Do not access to sub-terms through the atom BUT through the separated subterms list.
     *
     * TODO: refactor!!!!
     *
     *
     */
    private static void renameVariables(AppendableSubstitution substitution, java.util.List<Function> eqList, Function atom, java.util.List<Term> subterms,
                                        VariableGenerator variableGenerator) {
        /**
         * For each sub-term (of the current atom).
         */
        for (int j = 0; j < subterms.size(); j++) {
            Term subTerm = subterms.get(j);
            /**
             * Variable: rename it and adds equalities (if needed for a JOIN or LEFT-JOIN).
             */
            if (subTerm instanceof Variable) {

                //mapVarAtom.put((Variable)subTerm, atom);
                renameVariable(substitution, eqList, atom,	subterms, j, subTerm, variableGenerator);

            }
            /**
             * Constant: replaces it by a variable in the atom and creates a equality.
             */
            else if (subTerm instanceof Constant) {
                /*
                 * This case was necessary for query 7 in BSBM
                 */
                /**
                 * A('c') Replacing the constant with a fresh variable x and
                 * adding an quality atom ,e.g., A(x), x = 'c'
                 */
                // only relevant if in data function?
                if (atom.isDataFunction()) {
                    Variable var = variableGenerator.generateNewVariable();
                    Function equality = DATA_FACTORY.getFunctionEQ(var, subTerm);
                    subterms.set(j, var);
                    eqList.add(equality);
                }

            }
            /**
             * Functional term
             */
            else if (subTerm instanceof Function) {
                Predicate head = ((Function) subTerm).getFunctionSymbol();

                if (head.isDataTypePredicate()) {

                    // This case is for the ans atoms that might have
                    // functions in the head. Check if it is needed.
                    Set<Variable> subtermsset = subTerm
                            .getReferencedVariables();

                    // Right now we support only unary functions!!
                    for (Variable var : subtermsset) {
                        renameTerm(substitution, eqList, atom, subterms, j, (Function) subTerm, var, variableGenerator);
                    }
                }
            }
        } // end for subterms
    }


    /**
     * TODO: What is the difference with renameVariable ???
     *
     */
    private static void renameTerm(Substitution substitution, java.util.List<Function> eqList, Function atom,
                                   java.util.List<Term> subterms, int j, Function subTerm, VariableImpl var1,
                                   VariableGenerator variableGenerator) {
        Predicate head = subTerm.getFunctionSymbol();
        Variable var2 = (Variable) substitution.get(var1);

        if (var2 == null) {
			/*
			 * No substitution exists, hence, no action but generate a new
			 * variable and register in the substitutions, and replace the
			 * current value with a fresh one.
			 */
            var2 = variableGenerator.generateNewVariableFromVar(var1);

            FunctionalTermImpl newFuncTerm = (FunctionalTermImpl) DATA_FACTORY.getFunction(head, var1);
            subterms.set(j, var2);

            Function equality = DATA_FACTORY.getFunctionEQ(var2, newFuncTerm);
            eqList.add(equality);

        } else {

			/*
			 * There already exists one, so we generate a fresh, replace the
			 * current value, and add an equality between the substitution and
			 * the new value.
			 */

            if (atom.isDataFunction()) {
                Variable newVariable = variableGenerator.generateNewVariableFromVar(var1);

                subterms.set(j, newVariable);
                FunctionalTermImpl newFuncTerm = (FunctionalTermImpl) DATA_FACTORY.getFunction(head, var2);

                Function equality = DATA_FACTORY.getFunctionEQ(newVariable, newFuncTerm);
                eqList.add(equality);

            } else { // if its not data function, just replace
                // variable
                subterms.set(j, var2);
            }
        }
    }

    /**
     * TODO: explain
     *
     * Seems to rely on the ordering of atoms (data atoms first, filter atoms later).
     *
     *
     * Side-effects:
     *  - eqList (append-only)
     *  - newVarCounter (increment)
     *  - subterms (change j-th entry)
     *
     */
    private static void renameVariable(AppendableSubstitution substitution, java.util.List<Function> eqList, Function atom,
                                         java.util.List<Term> subterms, int j,   Term subTerm, VariableGenerator variableGenerator) {
        VariableImpl var1 = (VariableImpl) subTerm;
        VariableImpl var2 = (VariableImpl) substitution.get(var1);


        /**
         * No substitution for var1 --> creates a new one.
         *
         * No equality created.
         *
         */
        if (var2 == null) {
			/*
			 * No substitution exists, hence, no action but generate a new
			 * variable and register in the substitutions, and replace the
			 * current value with a fresh one.
			 */
//			int randomNum = rand.nextInt(20) + 1;
            //+ randomNum
            var2 = variableGenerator.generateNewVariableFromVar(var1);

            /**
             * TODO: build a new substitution (immutable style) and returns it.
             */
            substitution.put(var1, var2);
            //substitutionsTotal.put(var1, var2);
            subterms.set(j, var2);

        }
        /**
         * Existing substitution(s) (for var1 and also maybe for var2).
         * Does NOT create a substitution.
         *
         * Sets an equality between var2 and a newly created variable IF THE ATOM
         * IS A DATA ONE.
         */
        else {

			/*
			 * There already exists one, so we generate a fresh, replace the
			 * current value, and add an equality between the substitution and
			 * the new value.
			 */
            //FIXME: very suspicious
//			while (substitutions.containsKey(var2)){
//				VariableImpl variable = (VariableImpl) substitutions.get(var2);
//				var2=variable;
//			}
            /**
             * If a substitution also exists for var2, substitutes it.
             * TODO: explain why it is that needed.
             */
            if (substitution.get(var2) != null){
                VariableImpl variable = (VariableImpl) substitution.get(var2);
                var2=variable;
            }

            /**
             * If is in a data atom, sets an equality between var2 and a newly created variable.
             * Replaces in the subTerms list with the new one.
             */
            if (atom.isDataFunction()) {

                Variable newVariable = variableGenerator.generateNewVariableFromVar(var1);

                //replace the variable name
                subterms.set(j, newVariable);

                //record the change
                //substitutionsTotal.put(var2, newVariable);

                //create the equality
                Function equality = DATA_FACTORY.getFunctionEQ(var2, newVariable);
                eqList.add(equality);

            }

            /**
             * If its not data function, just replace the variable
             */
            else {
                subterms.set(j, var2);
            }
        }
    }

}
