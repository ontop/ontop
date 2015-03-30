package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.*;
import fj.data.*;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.ArrayList;

/**
 * Default implementation of PullOutEqualityNormalizer. Is Left-Join aware.
 *
 * Immutable class (instances have no attribute).
 *
 * Main challenge: putting the equalities at the "right" (good) place.
 * Rules for accepting/rejecting to move up boolean conditions:
 *   - Left of the LJ: ACCEPT. Why? If they appeared as ON conditions of the LJ, they would "filter" ONLY the right part,
 *                             NOT THE LEFT.
 *   - Right of the LJ: REJECT. Boolean conditions have to be used as ON conditions of the LOCAL LJ.
 *   - "Real" JOIN (joins between two tables): REJECT. Local ON conditions are (roughly) equivalent to the "global" WHERE
 *     conditions.
 *   - "Fake" JOIN (one data atoms and filter conditions). ACCEPT. Need a JOIN/LJ for being used as ON conditions.
 *                  If not blocked later, they will finish as WHERE conditions (atoms not embedded in a META-one).
 *
 *  TODO: create static "function" objects for performance improving
 *
 */
public class PullOutEqualityNormalizerImpl implements PullOutEqualityNormalizer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Ord<VariableImpl> VARIABLE_ORD = Ord.hashEqualsOrd();
    private final static List<P2<VariableImpl, Constant>> EMPTY_VARIABLE_CONSTANT_LIST = List.nil();
    private final static List<P2<VariableImpl, VariableImpl>> EMPTY_VARIABLE_RENAMING_LIST = List.nil();
    private final static List<Function> EMPTY_ATOM_LIST = List.nil();
    private final static Function TRUE_EQ = DATA_FACTORY.getFunctionEQ(OBDAVocabulary.TRUE, OBDAVocabulary.TRUE);


    /**
     * High-level method.
     *
     * Returns a new normalized rule.
     */
    @Override
    public CQIE normalizeByPullingOutEqualities(final CQIE initialRule) {
        CQIE newRule = initialRule.clone();

        // Mutable object
        final VariableDispatcher variableDispatcher = new VariableDispatcher(initialRule);

        /**
         * Result for the top atoms of the rule.
         */
        PullOutEqLocalNormResult result = normalizeSameLevelAtoms(List.iterableList(newRule.getBody()), variableDispatcher);

        newRule.updateBody(new ArrayList(result.getAllAtoms().toCollection()));
        return newRule;
    }

    /**
     * Builds out a ExtractEqNormResult by aggregating the results of atoms at one given level
     *    (top level, inside a Join, a LJ).
     *
     */
    private static PullOutEqLocalNormResult normalizeSameLevelAtoms(final List<Function> initialAtoms,
                                                               final VariableDispatcher variableDispatcher) {

        /**
         * First, it normalizes the data and composite atoms.
         */
        PullOutEqLocalNormResult mainAtomsResult = normalizeDataAndCompositeAtoms(initialAtoms, variableDispatcher);

        /**
         * Applies the substitution resulting from the data and composite atoms to the other atoms (filter, group atoms).
         */
        final Var2VarSubstitution substitution = mainAtomsResult.getVar2VarSubstitution();
        List<Function> otherAtoms = initialAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return !isDataOrLeftJoinOrJoinAtom(atom);
            }
        }).map(new F<Function, Function>() {
                    @Override
                    public Function f(Function atom) {
                        Function newAtom = (Function) atom.clone();
                        // SIDE-EFFECT on the newly created object
                        SubstitutionUtilities.applySubstitution(newAtom, substitution);
                        return newAtom;
            }
        });

        /**
         * Splits all the atoms into a non-pushable and a pushable group.
         */
        P2<List<Function>, List<Function>> otherAtomsP2 = splitPushableAtoms(otherAtoms);
        List<Function> nonPushableAtoms = mainAtomsResult.getNonPushableAtoms().append(otherAtomsP2._1());
        List<Function> pushableAtoms = mainAtomsResult.getPushableBoolAtoms().append(otherAtomsP2._2());

        return new PullOutEqLocalNormResult(nonPushableAtoms, pushableAtoms, substitution);
    }

    /**
     * Normalizes the data and composite atoms and merges them into a PullOutEqLocalNormResult.
     */
    private static PullOutEqLocalNormResult normalizeDataAndCompositeAtoms(final List<Function> sameLevelAtoms,
                                                                           final VariableDispatcher variableDispatcher) {

        /**
         * Normalizes the data atoms.
         */
        P3<List<Function>, List<Function>, Var2VarSubstitution> dataAtomResults = normalizeDataAtoms(sameLevelAtoms, variableDispatcher);
        List<Function> firstNonPushableAtoms = dataAtomResults._1();
        List<Function> firstPushableAtoms = dataAtomResults._2();
        Var2VarSubstitution dataAtomSubstitution = dataAtomResults._3();

        /**
         * Normalizes the composite atoms.
         */
        List<PullOutEqLocalNormResult> compositeAtomResults = sameLevelAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isLeftJoinOrJoinAtom(atom);
            }
        }).map(new F<Function, PullOutEqLocalNormResult>() {
            @Override
            public PullOutEqLocalNormResult f(Function atom) {
                return normalizeCompositeAtom(atom, variableDispatcher);
            }
        });
        List<Function> secondNonPushableAtoms = compositeAtomResults.bind(new F<PullOutEqLocalNormResult, List<Function>>() {
            @Override
            public List<Function> f(PullOutEqLocalNormResult result) {
                return result.getNonPushableAtoms();
            }
        });
        List<Function> secondPushableAtoms = compositeAtomResults.bind(new F<PullOutEqLocalNormResult, List<Function>>() {
            @Override
            public List<Function> f(PullOutEqLocalNormResult result) {
                return result.getPushableBoolAtoms();
            }
        });

        /**
         * Merges all the substitutions (one per composite atom and one for all data atoms) into one.
         *
         * Additional equalities might be produced during this process.
         */
        List<Var2VarSubstitution> substitutionsToMerge = compositeAtomResults.map(new F<PullOutEqLocalNormResult, Var2VarSubstitution>() {
            @Override
            public Var2VarSubstitution f(PullOutEqLocalNormResult result) {
                return result.getVar2VarSubstitution();
            }
        }).snoc(dataAtomSubstitution);
        P2<Var2VarSubstitution, List<Function>> substitutionResult = mergeSubstitutions(substitutionsToMerge);
        Var2VarSubstitution mergedSubstitution = substitutionResult._1();
        List<Function> additionalEqualities = substitutionResult._2();


        /**
         * Groups the non-pushable and pushable atoms.
         */
        List<Function> nonPushableAtoms = firstNonPushableAtoms.append(secondNonPushableAtoms);
        List<Function> pushableAtoms = firstPushableAtoms.append(secondPushableAtoms).append(additionalEqualities);

        return new PullOutEqLocalNormResult(nonPushableAtoms, pushableAtoms, mergedSubstitution);
    }

    /**
     * Normalizes the data atoms among same level atoms.
     *
     * Returns the normalized data atoms, the pushable atoms produced (equalities) and the produced substitution.
     */
    private static P3<List<Function>, List<Function>, Var2VarSubstitution> normalizeDataAtoms(final List<Function> sameLevelAtoms,
                                                                                       final VariableDispatcher variableDispatcher) {
        /**
         * Normalizes all the data atoms.
         */
        List<Function> dataAtoms = sameLevelAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isDataFunction();
            }
        });
        List<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>> atomResults = dataAtoms.map(
                new F<Function, P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>>() {
                    @Override
                    public P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> f(Function atom) {
                        // Uses the fact atoms are encoded as functional terms
                        return normalizeFunctionalTermInDataAtom(atom, variableDispatcher);
                    }
                });
        List<Function> normalizedDataAtoms = atomResults.map(new F<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>, Function>() {
            @Override
            public Function f(P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> triple) {
                return (Function) triple._1();
            }
        });
        // Variable-Variable equalities
        List<P2<VariableImpl, VariableImpl>> variableRenamings = atomResults.bind(
                P3.<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>__2());

        /**
         * Merges the variable renamings into a substitution and a list of variable-to-variable equalities.
         */
        P2<Var2VarSubstitution, List<Function>> renamingResult = mergeVariableRenamings(variableRenamings);
        Var2VarSubstitution substitution = renamingResult._1() ;
        List<Function> var2varEqualities = renamingResult._2();

        /**
         * Constructs variable-constant equalities.
         */
        List<P2<VariableImpl,Constant>> varConstantPairs  = atomResults.bind(
                P3.<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>__3());
        List<Function> varConstantEqualities = generateVariableConstantEqualities(varConstantPairs);

        /**
         * All the produced equalities form the pushable atoms.
         */
        List<Function> pushableAtoms = var2varEqualities.append(varConstantEqualities);

        return P.p(normalizedDataAtoms, pushableAtoms, substitution);
    }

    /**
     * Normalizes a left-join or join meta-atom.
     */
    private static PullOutEqLocalNormResult normalizeCompositeAtom(Function atom, VariableDispatcher variableDispatcher) {
        /**
         * Meta-atoms (algebra)
         */
        if (atom.isAlgebraFunction()) {
            Predicate functionSymbol = atom.getFunctionSymbol();
            if (functionSymbol.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
                return normalizeLeftJoin(atom, variableDispatcher);
            } else if (functionSymbol.equals(OBDAVocabulary.SPARQL_JOIN)) {
                return normalizeJoin(atom, variableDispatcher);
            }
        }

        throw new IllegalArgumentException("A composite (join, left join) atom was expected, not " + atom);
    }

    /**
     * Normalizes a Left-join meta-atom.
     *
     * Currently works with the arbitrary n-ary.
     * Could be simplified when the 3-arity of LJ will be enforced.
     *
     * Blocks pushable atoms from the right part --> they remain local.
     *
     */
    private static PullOutEqLocalNormResult normalizeLeftJoin(final Function leftJoinMetaAtom,
                                                              final VariableDispatcher variableDispatcher) {
        /**
         * Splits the left and the right atoms.
         * This could be simplified once the 3-arity will be enforced.
         *
         */
        final P2<List<Function>, List<Function>> splittedAtoms = splitLeftJoinSubAtoms(leftJoinMetaAtom);
        final List<Function> initialLeftAtoms = splittedAtoms._1();
        final List<Function> initialRightAtoms = splittedAtoms._2();

        /**
         * Normalizes the left and the right parts separately.
         */
        PullOutEqLocalNormResult leftNormalizationResults = normalizeSameLevelAtoms(initialLeftAtoms, variableDispatcher);
        PullOutEqLocalNormResult rightNormalizationResults = normalizeSameLevelAtoms(initialRightAtoms, variableDispatcher);

        /**
         * Merges the substitutions produced by the left and right parts into one substitution.
         * Variable-to-variable equalities might be produced during this process.
         */
        List<Var2VarSubstitution> substitutionsToMerge = List.<Var2VarSubstitution>nil()
                .snoc(leftNormalizationResults.getVar2VarSubstitution())
                .snoc(rightNormalizationResults.getVar2VarSubstitution());
        P2<Var2VarSubstitution, List<Function>> substitutionResult = mergeSubstitutions(substitutionsToMerge);

        Var2VarSubstitution mergedSubstitution = substitutionResult._1();
        List<Function> joiningEqualities = substitutionResult._2();

        /**
         * Builds the normalized left-join meta atom.
         *
         * Currently separates the left and the right part by the atom EQ(t,t).
         * Only one atom is presumed on the left part.
         *
         * This part would have to be updated if we want to enforce the 3-arity of a LJ
         * (currently not respected).
         * --> Joining conditions would have to be fold into a AND(...) boolean expression.
         *
         */
        List<Function> remainingLJAtoms = leftNormalizationResults.getNonPushableAtoms().snoc(TRUE_EQ).
                append(rightNormalizationResults.getAllAtoms()).append(joiningEqualities);
        // TODO: add a proper method in the data factory
        Function normalizedLeftJoinAtom = DATA_FACTORY.getFunction(OBDAVocabulary.SPARQL_LEFTJOIN,
                new ArrayList<Term>(remainingLJAtoms.toCollection()));

        /**
         * The only pushable atoms are those of the left part.
         */
        List<Function> pushedAtoms = leftNormalizationResults.getPushableBoolAtoms();

        return new PullOutEqLocalNormResult(List.cons(normalizedLeftJoinAtom, EMPTY_ATOM_LIST), pushedAtoms, mergedSubstitution);
    }

    /**
     * Normalizes a Join meta-atom.
     *
     * If is a real join (join between two data/composite atoms), blocks the join conditions.
     * Otherwise, propagates the pushable atoms and replaces its data/composite sub-atom.
     *
     * The 3-arity is respected for real joins. This involves Join and boolean folding.
     */
    private static PullOutEqLocalNormResult normalizeJoin(final Function joinMetaAtom,
                                                          final VariableDispatcher variableDispatcher) {
        List<Function> subAtoms = List.iterableList((java.util.List<Function>)(java.util.List<?>) joinMetaAtom.getTerms());
        PullOutEqLocalNormResult normalizationResult = normalizeSameLevelAtoms(subAtoms, variableDispatcher);

        /**
         * Real join
         */
        if (isRealJoin(subAtoms)) {

            /**
             * Folds the joining conditions (they will remain in the JOIN meta-atom, they are not pushed)
             * and finally folds the Join meta-atom to respected its 3-arity.
             */
            Function joiningCondition = foldBooleanConditions(normalizationResult.getPushableBoolAtoms());
            Function normalizedJoinMetaAtom = foldJoin(normalizationResult.getNonPushableAtoms(),joiningCondition);

            /**
             * A real JOIN is blocking --> no pushable boolean atom.
             */
            return new PullOutEqLocalNormResult(List.cons(normalizedJoinMetaAtom, EMPTY_ATOM_LIST), EMPTY_ATOM_LIST,
                    normalizationResult.getVar2VarSubstitution());
        }
        /**
         * Fake join: no need to create a new result
         * because only have one data/join/LJ atom that will remain (filter conditions are pushed)
         *
         */
        else {
            return normalizationResult;
        }
    }

    /**
     * Normalizes a Term found in a data atom.
     *
     * According to this concrete type, delegates the normalization to a sub-method.
     *
     * Returns
     *   - the normalized term,
     *   - a list of variable-to-variable renamings,
     *   - a list of variable-to-constant pairs.
     *
     * TODO: This would be much nicer with as a Visitor.
     */
    private static  P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeTermInDataAtom(
            Term term, VariableDispatcher variableDispatcher) {
        if (term instanceof VariableImpl) {
            return normalizeVariableInDataAtom((VariableImpl) term, variableDispatcher);
        }
        else if (term instanceof Constant) {
            return normalizeConstantInDataAtom((Constant) term, variableDispatcher);
        }
        else if (term instanceof Function) {
            return normalizeFunctionalTermInDataAtom((Function) term, variableDispatcher);
        }
        else {
            throw new IllegalArgumentException("Unexpected term inside a data atom: " + term);
        }
    }

    /**
     * Normalizes a Variable found in a data atom by renaming it.
     *
     * Renaming is delegated to the variable dispatcher.
     * The latter guarantees the variable with be used ONLY ONCE in a data atom of the body of the rule.
     *
     *  Returns
     *   - the new variable,
     *   - a list composed of the produced variable-to-variable renaming,
     *   - an empty list of variable-to-constant pairs.
     *
     */
    private static P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeVariableInDataAtom(
            VariableImpl previousVariable, VariableDispatcher variableDispatcher) {

        VariableImpl newVariable = variableDispatcher.renameDataAtomVariable(previousVariable);
        List<P2<VariableImpl, VariableImpl>> variableRenamings = List.cons(P.p(previousVariable, newVariable), EMPTY_VARIABLE_RENAMING_LIST);

        return P.p((Term) newVariable, variableRenamings, EMPTY_VARIABLE_CONSTANT_LIST);
    }

    /**
     * Normalizes a Constant found in a data atom by replacing it by a variable and creating a variable-to-constant pair.
     *
     * Returns
     *   - the new variable,
     *   - an empty list of variable-to-variable renamings,
     *   - a list composed of the created variable-to-constant pair.
     */
    private static P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeConstantInDataAtom(
            Constant constant, VariableDispatcher variableDispatcher) {
        VariableImpl newVariable = variableDispatcher.generateNewVariable();

        List<P2<VariableImpl, Constant>> variableConstantPairs = List.cons(P.p(newVariable, constant), EMPTY_VARIABLE_CONSTANT_LIST);

        return  P.p((Term) newVariable, EMPTY_VARIABLE_RENAMING_LIST, variableConstantPairs);
    }


    /**
     * Normalizes a functional term found in a data atom or that is the data atom itself (trick!).
     *
     * Basically, normalizes all its sub-atoms (may be recursive) and rebuilds an updated functional term.
     *
     * All the variable-to-variable renamings and variable-to-constant pairs are concatenated
     * into their two respective lists.
     *
     *
     * Returns
     *   - the normalized functional term,
     *   - a list of variable-to-variable renamings,
     *   - a list of variable-to-constant pairs.
     */
    private static P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeFunctionalTermInDataAtom(
                        Function functionalTerm, final VariableDispatcher variableDispatcher) {

        /**
         * Normalizes sub-terms.
         */
        List<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>> subTermResults =
                List.iterableList(functionalTerm.getTerms()).map(new F<Term, P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>>() {
                    @Override
                    public P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> f(Term term) {
                        return normalizeTermInDataAtom(term, variableDispatcher);
                    }
                });

        /**
         * Retrieves normalized sub-terms.
         */
        List<Term> newSubTerms = subTermResults.map(new F<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>, Term>() {
            @Override
            public Term f(P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> p3) {
                return p3._1();
            }
        });
        Function newFunctionalTerm = constructNewFunction(functionalTerm.getFunctionSymbol(), newSubTerms);

        /**
         * Concatenates variable-to-variable renamings and variable-to-constant pairs.
         */
        List<P2<VariableImpl, VariableImpl>> variableRenamings = subTermResults.bind(new F<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>, List<P2<VariableImpl, VariableImpl>>>() {
            @Override
            public List<P2<VariableImpl, VariableImpl>> f(P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> p3) { return p3._2();
            }
        });
        List<P2<VariableImpl, Constant>> variableConstantPairs = subTermResults.bind(new F<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>, List<P2<VariableImpl, Constant>>>() {
            @Override
            public List<P2<VariableImpl, Constant>> f(P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> p3) { return p3._3();
            }
        });


        return P.p((Term) newFunctionalTerm, variableRenamings, variableConstantPairs);
    }

    /**
     * Merges a list of substitution into one and also returns a list of generated variable-to-variable equalities.
     *
     * See mergeVariableRenamings for further details.
     *
     */
    private static P2<Var2VarSubstitution, List<Function>> mergeSubstitutions(List<Var2VarSubstitution> substitutionsToMerge) {

        /**
         * Transforms the substitutions into list of variable-to-variable pairs and concatenates them.
         */
        List<P2<VariableImpl, VariableImpl>> renamingPairs = substitutionsToMerge.bind(
                new F<Var2VarSubstitution, List<P2<VariableImpl, VariableImpl>>>() {
            @Override
            public List<P2<VariableImpl, VariableImpl>> f(Var2VarSubstitution substitution) {
                // Transforms the map of the substitution in a list of pairs
                return TreeMap.fromMutableMap(VARIABLE_ORD, substitution.getVar2VarMap()).toStream().toList();
            }
        });

        /**
         * Merges renaming variable-to-variable pairs into a substitution
         * and retrieves generated variable-to-variable equalities.
         */
        return mergeVariableRenamings(renamingPairs);
    }

    /**
     * Merges renaming variable-to-variable pairs into a substitution and also returns
     *     a list of generated variable-to-variable equalities.
     *
     * Since a variable-to-variable substitution maps a variable to ONLY one variable, merging
     * involves selecting the target variable among a set.
     *
     * The first variable (lowest) is selecting according a hash ordering.
     */
    private static P2<Var2VarSubstitution, List<Function>> mergeVariableRenamings(
            List<P2<VariableImpl, VariableImpl>> renamingPairs) {
        /**
         * Groups pairs according to the initial variable
         */
        TreeMap<VariableImpl, Set<VariableImpl>> commonMap = renamingPairs.groupBy(P2.<VariableImpl, VariableImpl>__1(),
                P2.<VariableImpl, VariableImpl>__2()).
                /**
                 * and converts the list of target variables into a set.
                 */
                map(new F<List<VariableImpl>, Set<VariableImpl>>() {
            @Override
            public Set<VariableImpl> f(List<VariableImpl> equivalentVariables) {
                return Set.iterableSet(VARIABLE_ORD, equivalentVariables);
            }
        });

        /**
         * Generates equalities between the target variables
         */
        List<Function> newEqualities = commonMap.values().bind(new F<Set<VariableImpl>, List<Function>>() {
            @Override
            public List<Function> f(Set<VariableImpl> equivalentVariables) {
                return generateVariableEqualities(equivalentVariables);
            }
        });

        /**
         * Selects the target variables for the one-to-one substitution map.
         *
         * Selection consists in taking the first element of set.
         */
        TreeMap<VariableImpl, VariableImpl> mergedMap = commonMap.map(new F<Set<VariableImpl>, VariableImpl>() {
            @Override
            public VariableImpl f(Set<VariableImpl> variables) {
                return variables.toList().head();
            }
        });
        Var2VarSubstitution mergedSubstitution = new Var2VarSubstitutionImpl(mergedMap);

        return P.p(mergedSubstitution, newEqualities);
    }

    /**
     * Converts the variable-to-constant pairs into a list of equalities.
     */
    private static List<Function> generateVariableConstantEqualities(List<P2<VariableImpl, Constant>> varConstantPairs) {
        return varConstantPairs.map(new F<P2<VariableImpl, Constant>, Function>() {
            @Override
            public Function f(P2<VariableImpl, Constant> pair) {
                return DATA_FACTORY.getFunctionEQ(pair._1(), pair._2());
            }
        });
    }

    /**
     * Converts the variable-to-variable pairs into a list of equalities.
     */
    private static List<Function> generateVariableEqualities(Set<VariableImpl> equivalentVariables) {
        List<VariableImpl> variableList = equivalentVariables.toList();
        List<P2<VariableImpl, VariableImpl>> variablePairs = variableList.zip(variableList.tail());
        return variablePairs.map(new F<P2<VariableImpl, VariableImpl>, Function>() {
            @Override
            public Function f(P2<VariableImpl, VariableImpl> pair) {
                return DATA_FACTORY.getFunctionEQ(pair._1(), pair._2());
            }
        });
    }

    private static Boolean isDataOrLeftJoinOrJoinAtom(Function atom) {
        return atom.isDataFunction() || isLeftJoinOrJoinAtom(atom);
    }

    private static Boolean isLeftJoinOrJoinAtom(Function atom) {
        Predicate predicate = atom.getFunctionSymbol();
        return predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN) ||
                predicate.equals(OBDAVocabulary.SPARQL_JOIN);
    }

    /**
     * Folds a list of boolean atoms into one AND(AND(...)) boolean atom.
     */
    private static Function foldBooleanConditions(List<Function> booleanAtoms) {
        if (booleanAtoms.length() == 0)
            return TRUE_EQ;

        Function firstBooleanAtom = booleanAtoms.head();
        return booleanAtoms.tail().foldLeft(new F2<Function, Function, Function>() {
            @Override
            public Function f(Function previousAtom, Function currentAtom) {
                return DATA_FACTORY.getFunctionAND(previousAtom, currentAtom);
            }
        }, firstBooleanAtom);
    }

    /**
     * Folds a list of data/composite atoms and joining conditions into a JOIN(...) with a 3-arity.
     *
     */
    private static Function foldJoin(List<Function> dataOrCompositeAtoms, Function joiningCondition) {
        int length = dataOrCompositeAtoms.length();
        if (length < 2) {
            throw new IllegalArgumentException("At least two atoms should be given");
        }

        Function firstAtom = dataOrCompositeAtoms.head();
        Function secondAtom = foldJoin(dataOrCompositeAtoms.tail());

        return DATA_FACTORY.getSPARQLJoin(firstAtom, secondAtom, joiningCondition);
    }

    /**
     * Folds a list of data/composite atoms into a JOIN (if necessary)
     * by adding EQ(t,t) as a joining condition.
     */
    private static Function foldJoin(List<Function> dataOrCompositeAtoms) {
        int length = dataOrCompositeAtoms.length();
        if (length == 1)
            return dataOrCompositeAtoms.head();
        else if (length == 0)
            throw new IllegalArgumentException("At least one atom should be given.");
        else {
            Function firstAtom = dataOrCompositeAtoms.head();
            /**
             * Folds the data/composite atom list into a JOIN(JOIN(...)) meta-atom.
             */
            return dataOrCompositeAtoms.tail().foldLeft(new F2<Function, Function, Function>() {
                @Override
                public Function f(Function firstAtom, Function secondAtom) {
                    return DATA_FACTORY.getSPARQLJoin(firstAtom, secondAtom, TRUE_EQ);
                }
            }, firstAtom);
        }
    }

    /**
     * Practical criterion for detecting a real join: having more data/composite atoms.
     *
     * May produces some false negatives for crazy abusive nested joins of boolean atoms (using JOIN instead of AND).
     */
    private static boolean isRealJoin(List<Function> subAtoms) {
        //TODO: reuse a shared static filter fct object.
        List<Function> dataAndCompositeAtoms = subAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isDataOrLeftJoinOrJoinAtom(atom);
            }
        });

        return dataAndCompositeAtoms.length() > 1;
    }

    /**
     * HEURISTIC for split the left join sub atoms.
     *
     * Left: left of the SECOND data atom.
     * Right: the rest.
     *
     * Will cause problem if the left part is supposed to have multiple data/composite atoms.
     * However, if the 3-arity of the LJ is respected and a JOIN is used for the left part, no problem.
     *
     */
    private static P2<List<Function>, List<Function>> splitLeftJoinSubAtoms(Function leftJoinMetaAtom) {
        List<Function> subAtoms = List.iterableList(
                (java.util.List<Function>)(java.util.List<?>) leftJoinMetaAtom.getTerms());

        // TODO: make it static (performance improvement).
        F<Function, Boolean> isNotDataOrCompositeAtomFct = new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return !(isDataOrLeftJoinOrJoinAtom(atom));
            }
        };

        /**
         * Left: left of the first data/composite atom (usually empty).
         *
         * The first data/composite atom is thus the first element of the right list.
         */
        P2<List<Function>, List<Function>> firstDataAtomSplit = subAtoms.span(isNotDataOrCompositeAtomFct);
        Function firstDataAtom = firstDataAtomSplit._2().head();

        /**
         * Left: left of the second data/composite atom starting just after the first data/composite atom.
         *
         * Right: right part of the left join (includes the joining conditions, no problem).
         */
        P2<List<Function>, List<Function>> secondDataAtomSplit = firstDataAtomSplit._2().tail().span(
                isNotDataOrCompositeAtomFct);

        List<Function> leftAtoms = firstDataAtomSplit._1().snoc(firstDataAtom).append(secondDataAtomSplit._1());
        List<Function> rightAtoms = secondDataAtomSplit._2();

        return P.p(leftAtoms, rightAtoms);
    }

    /**
     * Only boolean atoms are pushable.
     */
    private static P2<List<Function>,List<Function>> splitPushableAtoms(List<Function> atoms) {
        List<Function> nonPushableAtoms = atoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return !isPushable(atom);
            }
        });
        List<Function> pushableAtoms = atoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isPushable(atom);
            }
        });

        return P.p(nonPushableAtoms, pushableAtoms);
    }

    /**
     * Only boolean atoms are pushable.
     */
    private static boolean isPushable(Function atom) {
        if (atom.isBooleanFunction())
            return true;
        return false;
    }

    private static Function constructNewFunction(Predicate functionSymbol, List<Term> subTerms) {
        return DATA_FACTORY.getFunction(functionSymbol, new ArrayList<>(subTerms.toCollection()));
    }
}
