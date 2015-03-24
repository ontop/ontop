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
 * Pulls out equalities.
 *
 * Limit: JOIN meta-predicates are not considered (--> no possibility to have specific ON conditions at the proper level).
 *
 * TODO: explain.
 */
public class ExtractEqualityNormalizer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Ord<VariableImpl> VARIABLE_ORD = Ord.hashEqualsOrd();
    private final static List<P2<VariableImpl, Constant>> EMPTY_VARIABLE_CONSTANT_LIST = List.nil();
    private final static List<P2<VariableImpl, VariableImpl>> EMPTY_VARIABLE_RENAMING_LIST = List.nil();

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
        final VariableDispatcher variableDispatcher = new VariableDispatcher(initialRule);

        ExtractEqNormResult result = normalizeSameLevelAtoms(List.iterableList(newRule.getBody()), variableDispatcher);

        newRule.updateBody(new ArrayList(result.getAllAtoms().toCollection()));
        return newRule;
    }

    /**
     * Builds out a ExtractEqNormResult by aggregating the results of atoms.
     *
     * The initialSubstitution is "updated" by each atom of this list. It maps
     * the old and new names of the variables  "pull out into equalities".
     *
     * TODO: update this comment.
     */
    private static ExtractEqNormResult normalizeSameLevelAtoms(final List<Function> initialAtoms,
                                                               final VariableDispatcher variableDispatcher) {


        ExtractEqNormResult mainAtomsResult = normalizeDataAndCompositeAtoms(initialAtoms, variableDispatcher);

        final Var2VarSubstitution substitution = mainAtomsResult.getVar2VarSubstitution();


        /**
         * Applies the substitution to the other atoms (filter, group atoms)
         */
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
         * TODO: explain
         *
         */
        P2<List<Function>, List<Function>> otherAtomsP2 = splitPushableAtoms(otherAtoms);

        List<Function> nonPushableAtoms = mainAtomsResult.getNonPushableAtoms().append(otherAtomsP2._1());
        List<Function> pushableAtoms = mainAtomsResult.getPushableAtoms().append(otherAtomsP2._2());

        return new ExtractEqNormResult(nonPushableAtoms, pushableAtoms, substitution);
    }

    /**
     * TODO: explain
     *
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
     * TODO: explain
     *
     */
    private static boolean isPushable(Function atom) {
        if (atom.isBooleanFunction())
            return true;

        return false;
    }


    /**
     * TODO: describe
     */
    private static ExtractEqNormResult normalizeDataAndCompositeAtoms(final List<Function> sameLevelAtoms, final VariableDispatcher variableDispatcher) {

        /**
         * TODO: describe
         */
        P3<List<Function>, List<Function>, Var2VarSubstitution> dataAtomResults = normalizeDataAtoms(sameLevelAtoms, variableDispatcher);
        List<Function> firstNonPushableAtoms = dataAtomResults._1();
        List<Function> firstPushableAtoms = dataAtomResults._2();
        Var2VarSubstitution dataAtomSubstitution = dataAtomResults._3();

        List<Function> compositeAtoms = sameLevelAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isLeftJoinOrJoinAtom(atom);
            }
        });


        List<ExtractEqNormResult> compositeAtomResults = compositeAtoms.map(new F<Function, ExtractEqNormResult>() {
            @Override
            public ExtractEqNormResult f(Function atom) {
                return normalizeCompositeAtom(atom, variableDispatcher);
            }
        });


        /**
         * Aggregates the extracted results into one ExtractEqNormResult.
         *
         * Extracts the last substitution, all the non-pushable and pushable atoms.
         *
         * TODO: update this comment
         */
        List<Function> secondNonPushableAtoms = compositeAtomResults.bind(new F<ExtractEqNormResult, List<Function>>() {
            @Override
            public List<Function> f(ExtractEqNormResult result) {
                return result.getNonPushableAtoms();
            }
        });
        List<Function> secondPushableAtoms = compositeAtomResults.bind(new F<ExtractEqNormResult, List<Function>>() {
            @Override
            public List<Function> f(ExtractEqNormResult result) {
                return result.getPushableAtoms();
            }
        });

        /**
         * TODO: describe!!!
         */
        List<Var2VarSubstitution> substitutionsToMerge = compositeAtomResults.map(new F<ExtractEqNormResult, Var2VarSubstitution>() {
            @Override
            public Var2VarSubstitution f(ExtractEqNormResult result) {
                return result.getVar2VarSubstitution();
            }
        }).snoc(dataAtomSubstitution);

        P2<Var2VarSubstitution, List<Function>> substitutionResult = mergeSubstitutions(substitutionsToMerge);
        Var2VarSubstitution mergedSubstitution = substitutionResult._1();
        List<Function> additionalEqualities = substitutionResult._2();


        /**
         * TODO: describe
         */
        List<Function> pushableAtoms = firstPushableAtoms.append(secondPushableAtoms).append(additionalEqualities);

        /**
         * TODO: describe
         */
        List<Function> nonPushableAtoms = firstNonPushableAtoms.append(secondNonPushableAtoms);

        return new ExtractEqNormResult(nonPushableAtoms, pushableAtoms, mergedSubstitution);
    }

    /**
     * TODO: describe
     */
    private static P3<List<Function>, List<Function>, Var2VarSubstitution> normalizeDataAtoms(final List<Function> sameLevelAtoms,
                                                                                       final VariableDispatcher variableDispatcher) {

        List<Function> dataAtoms = sameLevelAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isDataFunction();
            }
        });

        /**
         * TODO: describe
         */
        List<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>> atomResults = dataAtoms.map(
                new F<Function, P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>>() {
                    @Override
                    public P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> f(Function atom) {
                        // Uses the fact atoms are encoded as functional terms
                        return normalizeDataAtomFunctionalTerm(atom, variableDispatcher);
                    }
                });

        /**
         * Normalized atoms
         */
        List<Function> normalizedDataAtoms = atomResults.map(new F<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>, Function>() {
            @Override
            public Function f(P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> triple) {
                return (Function) triple._1();
            }
        });

        /**
         * Variable-Variable equalities
         */
        List<P2<VariableImpl, VariableImpl>> variableRenamings = atomResults.bind(
                P3.<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>__2());

        /**
         * TODO: describe
         */
        P2<Var2VarSubstitution, List<Function>> renamingResult = mergeVariableRenamings(variableRenamings);
        Var2VarSubstitution substitution = renamingResult._1() ;
        List<Function> var2varEqualities = renamingResult._2();

        /**
         * Variable-Constant equalities
         */
        List<P2<VariableImpl,Constant>> varConstantPairs  = atomResults.bind(
                P3.<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>__3());

        List<Function> varConstantEqualities = generateVariableConstantEqualities(varConstantPairs);


        /**
         * TODO: describe
         */
        List<Function> pushableAtoms = var2varEqualities.append(varConstantEqualities);

        return P.p(normalizedDataAtoms, pushableAtoms, substitution);
    }

    /**
     * TODO: describe
     */
    private static P2<Var2VarSubstitution, List<Function>> mergeSubstitutions(List<Var2VarSubstitution> substitutionsToMerge) {
        List<P2<VariableImpl, VariableImpl>> renamingPairs = substitutionsToMerge.bind(new F<Var2VarSubstitution, List<P2<VariableImpl, VariableImpl>>>() {
            @Override
            public List<P2<VariableImpl, VariableImpl>> f(Var2VarSubstitution substitution) {
                // Transforms the map of the substitution in a list of pairs
                return TreeMap.fromMutableMap(VARIABLE_ORD, substitution.getVar2VarMap()).toStream().toList();
            }
        });

        return mergeVariableRenamings(renamingPairs);
    }

    /**
     * TODO: explain
     */
    private static P2<Var2VarSubstitution, List<Function>> mergeVariableRenamings( List<P2<VariableImpl, VariableImpl>> renamingPairs) {
        TreeMap<VariableImpl, Set<VariableImpl>> commonMap = renamingPairs.groupBy(P2.<VariableImpl, VariableImpl>__1(),
                P2.<VariableImpl, VariableImpl>__2()).
                /**
                 * TODO: describe
                 */
                map(new F<List<VariableImpl>, Set<VariableImpl>>() {
            @Override
            public Set<VariableImpl> f(List<VariableImpl> equivalentVariables) {
                return Set.iterableSet(VARIABLE_ORD, equivalentVariables);
            }
        });

        List<Function> newEqualities = commonMap.values().bind(new F<Set<VariableImpl>, List<Function>>() {
            @Override
            public List<Function> f(Set<VariableImpl> equivalentVariables) {
                return generateVariableEqualities(equivalentVariables);
            }
        });

        /**
         * TODO: describe the variable selection strategy.
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


    private static List<Function> generateVariableConstantEqualities(List<P2<VariableImpl, Constant>> varConstantPairs) {
        return varConstantPairs.map(new F<P2<VariableImpl, Constant>, Function>() {
            @Override
            public Function f(P2<VariableImpl, Constant> pair) {
                return DATA_FACTORY.getFunctionEQ(pair._1(), pair._2());
            }
        });
    }

    /**
     * TODO: describe
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
     * Applies the "pull out equalities" normalization to a left-join or join atom.
     *
     */
    private static ExtractEqNormResult normalizeCompositeAtom(Function atom, VariableDispatcher variableDispatcher) {
        /**
         * Meta-atoms (algebra)
         */
        if (atom.isAlgebraFunction()) {
            Predicate functionSymbol = atom.getFunctionSymbol();
            if (functionSymbol.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
                return normalizeLeftJoin(atom, variableDispatcher);
            } else if (functionSymbol.equals(OBDAVocabulary.SPARQL_JOIN)) {
                throw new RuntimeException("Not implemented (yet). We do not expect JOIN meta-predicates at " +
                        "the pull-out equalities level.");
            }
        }

        throw new IllegalArgumentException("A data or composite (join, left join) atom was expected, not " + atom);
    }


    /**
     * TODO: explain it
     *
     */
    private static ExtractEqNormResult normalizeLeftJoin(final Function leftJoinMetaAtom,
                                                         final VariableDispatcher variableDispatcher) {

        /**
         * TODO: may change with JOIN meta-predicates
         */
        final P2<List<Function>, List<Function>> splittedAtoms = splitLeftJoinSubAtoms(leftJoinMetaAtom);

        final List<Function> initialLeftAtoms = splittedAtoms._1();
        final List<Function> initialRightAtoms = splittedAtoms._2();

        ExtractEqNormResult leftNormalizationResults = normalizeSameLevelAtoms(initialLeftAtoms, variableDispatcher);
        ExtractEqNormResult rightNormalizationResults = normalizeSameLevelAtoms(initialRightAtoms, variableDispatcher);

        /**
         * TODO: explain
         */
        List<Var2VarSubstitution> substitutionsToMerge = List.cons(leftNormalizationResults.getVar2VarSubstitution(),
                List.cons(rightNormalizationResults.getVar2VarSubstitution(), List.<Var2VarSubstitution>nil()));
        P2<Var2VarSubstitution, List<Function>> substitutionResult = mergeSubstitutions(substitutionsToMerge);
        Var2VarSubstitution mergedSubstitution = substitutionResult._1();
        List<Function> joiningEqualities = substitutionResult._2();

        /**
         * TODO: explain. "Blocking" criteria.
         */
        List<Function> remainingLJAtoms = leftNormalizationResults.getNonPushableAtoms().append(rightNormalizationResults.getAllAtoms()).append(joiningEqualities);
        List<Function> pushedUpAtoms = leftNormalizationResults.getPushableAtoms();


        return new ExtractEqNormResult(remainingLJAtoms, pushedUpAtoms, mergedSubstitution);
    }


    /**
     * Split the left join sub atoms.
     *
     * Left: left of the SECOND data atom.
     * Right: the rest.
     */
    private static P2<List<Function>, List<Function>> splitLeftJoinSubAtoms(Function leftJoinMetaAtom) {
        List<Function> subAtoms = List.iterableList((java.util.List<Function>)(java.util.List<?>) leftJoinMetaAtom.getTerms());

        F<Function, Boolean> isNotDataAtomFct = new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return !atom.isDataFunction();
            }
        };

        P2<List<Function>, List<Function>> firstDataAtomSplit = subAtoms.span(isNotDataAtomFct);
        P2<List<Function>, List<Function>> secondDataAtomSplit = firstDataAtomSplit._2().span(isNotDataAtomFct);

        List<Function> leftAtoms = firstDataAtomSplit._1().append(secondDataAtomSplit._1());
        List<Function> rightAtoms = secondDataAtomSplit._2();

        return P.p(leftAtoms, rightAtoms);
    }

    /**
     * TODO: explain
     *
     * TODO: this would be much nice with as a Visitor.
     */
    private static  P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeDataAtomTerm(Term term, VariableDispatcher variableDispatcher) {
        if (term instanceof VariableImpl) {
            return normalizeDataAtomVariable((VariableImpl) term, variableDispatcher);
        }
        else if (term instanceof Constant) {
            return normalizeDataAtomConstant((Constant) term, variableDispatcher);
        }
        else if (term instanceof Function) {
            return normalizeDataAtomFunctionalTerm((Function) term, variableDispatcher);
        }
        else {
            throw new IllegalArgumentException("Unexpected term inside a data atom: " + term);
        }
    }

    /**
     * TODO: explain
     */
    private static P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeDataAtomVariable(VariableImpl previousVariable,
                                                                                                                               VariableDispatcher variableDispatcher) {
        /**
         * TODO: explain the magic here.
         */
        VariableImpl newVariable = variableDispatcher.renameDataAtomVariable(previousVariable);
        List<P2<VariableImpl, VariableImpl>> variableRenamings = List.cons(P.p(previousVariable, newVariable), EMPTY_VARIABLE_RENAMING_LIST);

        return P.p((Term) newVariable, variableRenamings, EMPTY_VARIABLE_CONSTANT_LIST);
    }

    /**
     * TODO: explain
     */
    private static P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeDataAtomConstant(Constant constant, VariableDispatcher variableDispatcher) {
        VariableImpl newVariable = variableDispatcher.generateNewVariable();

        List<P2<VariableImpl, Constant>> variableConstantPairs = List.cons(P.p(newVariable, constant), EMPTY_VARIABLE_CONSTANT_LIST);

        return  P.p((Term) newVariable, EMPTY_VARIABLE_RENAMING_LIST, variableConstantPairs);
    }


    /**
     * TODO: explain
     */
    private static P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> normalizeDataAtomFunctionalTerm(Function functionalTerm,
                                                                                                                                    final VariableDispatcher variableDispatcher) {
        List<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>> subTermResults =
                List.iterableList(functionalTerm.getTerms()).map(new F<Term, P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>>() {
                    @Override
                    public P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> f(Term term) {
                        return normalizeDataAtomTerm(term, variableDispatcher);
                    }
                });

        List<Term> newSubTerms = subTermResults.map(new F<P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>>, Term>() {
            @Override
            public Term f(P3<Term, List<P2<VariableImpl, VariableImpl>>, List<P2<VariableImpl, Constant>>> p3) {
                return p3._1();
            }
        });
        Function newFunctionalTerm = constructNewFunction(functionalTerm.getFunctionSymbol(), newSubTerms);

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
     * TODO: explain
     */
    private static Function constructNewFunction(Predicate functionSymbol, List<Term> subTerms) {
        return DATA_FACTORY.getFunction(functionSymbol, new ArrayList<>(subTerms.toCollection()));
    }
}
