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
    //private final static TreeMap<VariableImpl,List<VariableImpl>> EMPTY_VARIABLE_MAP = TreeMap.empty(VARIABLE_ORD);
    //private final static TreeMap<VariableImpl, Constant> EMPTY_VARIABLE_CONSTANT_MAP = TreeMap.empty(VARIABLE_ORD);
    private final static List<P2<VariableImpl, Constant>> EMPTY_VARIABLE_CONSTANT_LIST = List.nil();
    private final static List<P2<VariableImpl, VariableImpl>> EMPTY_VARIABLE_RENAMING_LIST = List.nil();
    private final static List<Function> EMPTY_ATOM_LIST = List.nil();

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


        ExtractEqNormResult mainAtomsResult = normalizeDataOrCompositeAtoms(initialAtoms, variableDispatcher);

        final Substitution substitution = mainAtomsResult.getSubstitution();


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
    private static ExtractEqNormResult normalizeDataOrCompositeAtoms(final List<Function> sameLevelAtoms, final VariableDispatcher variableDispatcher) {

        /**
         * TODO: retrieves the result
         */
        normalizeDataAtoms(sameLevelAtoms, variableDispatcher);

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
        List<Function> firstNonPushableAtoms = compositeAtomResults.bind(new F<ExtractEqNormResult, List<Function>>() {
            @Override
            public List<Function> f(ExtractEqNormResult result) {
                return result.getNonPushableAtoms();
            }
        });
        List<Function> firstPushableAtoms = compositeAtomResults.bind(new F<ExtractEqNormResult, List<Function>>() {
            @Override
            public List<Function> f(ExtractEqNormResult result) {
                return result.getPushableAtoms();
            }
        });

        /**
         * TODO: also consider non pushable and pushable atoms of data atoms
         */

        /**
         * TODO: refactor. Consider both results for data atoms and substitution functions
         * from composite atoms.
         */
        List<P2<VariableImpl,VariableImpl>> variableRenamings = compositeAtomResults.bind(new F<ExtractEqNormResult, List<P2<VariableImpl, VariableImpl>>>() {
            @Override
            public List<P2<VariableImpl, VariableImpl>> f(ExtractEqNormResult result) {
                return result.getVariableRenamings();
            }
        });
        TreeMap<VariableImpl, List<VariableImpl>> variableRenamingMap = variableRenamings.groupBy(new F<P2<VariableImpl, VariableImpl>, VariableImpl>() {
            @Override
            public VariableImpl f(P2<VariableImpl, VariableImpl> pair) {
                return pair._1();
            }
        }, new F<P2<VariableImpl, VariableImpl>, VariableImpl>() {
            @Override
            public VariableImpl f(P2<VariableImpl, VariableImpl> pair) {
                return pair._2();
            }
        });

        /**
         * Variable-Variable equalities
         */
        List<Function> var2VarEqualities = generateVariableEqualities(variableRenamingMap);


        /**
         * TODO: refactor
         */
        List<Function> pushableAtoms = firstPushableAtoms.append(var2VarEqualities).append(varConstantEqualities);

        /**
         * TODO: complete
         */
        List<Function> nonPushableAtoms = firstNonPushableAtoms;

        return new ExtractEqNormResult(nonPushableAtoms, pushableAtoms, aggregatedSubstitution);
    }

    /**
     * TODO: find the type to return
     */
    private static void normalizeDataAtoms(final List<Function> sameLevelAtoms, final VariableDispatcher variableDispatcher) {

        List<Function> dataAtoms = sameLevelAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isDataFunction()
            }
        });

        /**
         * Variable-Constant equalities
         */
        List<P2<VariableImpl,Constant>> varConstantPairs  = dataAndCompositeAtomResults.bind(new F<ExtractEqNormResult, List<P2<VariableImpl, Constant>>>() {
            @Override
            public List<P2<VariableImpl, Constant>> f(ExtractEqNormResult result) {
                return result.getVariableConstantPairs();
            }
        });
        List<Function> varConstantEqualities = generateVariableConstantEqualities(varConstantPairs);

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
     * TODO: explain
     */
    private static List<Function> generateVariableEqualities(TreeMap<VariableImpl, List<VariableImpl>> variableRenamingMap) {

        List<Function> equalities = variableRenamingMap.toStream().bind(new F<P2<VariableImpl, List<VariableImpl>>, Stream<Function>>() {
            @Override
            public Stream<Function> f(P2<VariableImpl, List<VariableImpl>> variableRenamings) {
                VariableImpl initialVariable = variableRenamings._1();
                Set<VariableImpl> usedVariables = Set.iterableSet(VARIABLE_ORD, variableRenamings._2());

                /**
                 * Used variables and initial variables (that may still also used "locally").
                 */
                List<VariableImpl> allVariables = usedVariables.insert(initialVariable).toList();

                /**
                 * Pairs for equalities [(0,1), (1,2), (2,3), ...]
                 * Generates a EQ atom for each pair.
                 */
                List<P2<VariableImpl, VariableImpl>> variablePairs = allVariables.zip(allVariables.tail());
                return variablePairs.map(new F<P2<VariableImpl, VariableImpl>, Function>() {
                    @Override
                    public Function f(P2<VariableImpl, VariableImpl> pair) {
                        return DATA_FACTORY.getFunctionEQ(pair._1(), pair._2());
                    }
                }).toStream();
            }
        }).toList();

        return equalities;
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
         * TODO: explain. "Blocking" criteria.
         */
        List<Function> remainingLJAtoms = leftNormalizationResults.getNonPushableAtoms().append(rightNormalizationResults.getAllAtoms());
        List<Function> pushedUpAtoms = leftNormalizationResults.getPushableAtoms();

        return new ExtractEqNormResult(remainingLJAtoms, pushedUpAtoms, lastSubstitution);
    }


    /**
     * TODO: implement it
     */
    private static P2<List<Function>, List<Function>> splitLeftJoinSubAtoms(Function leftJoinMetaAtom) {
        return null;
    }

    /**
     * TODO: implement
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

    private static Function constructNewFunction(Predicate functionSymbol, List<Term> subTerms) {
        return DATA_FACTORY.getFunction(functionSymbol, new ArrayList<>(subTerms.toCollection()));
    }
}
