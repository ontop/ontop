package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.F;
import fj.F2;
import fj.P;
import fj.P2;
import fj.data.List;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

import java.util.ArrayList;

/**
 * Pulls out equalities.
 *
 * Limit: JOIN meta-predicates are not considered (--> no possibility to specific ON conditions at the proper level).
 */
public class ExtractEqualityNormalizer {

    /**
     * TODO: explain
     *
     * High-level static method.
     *
     */
    public static CQIE extractEqualitiesAndNormalize(final CQIE initialRule) {
        CQIE newRule = initialRule.clone();
        ExtractEqNormResult result = normalizeSameLevelAtoms(List.iterableList(newRule.getBody()), new SubstitutionImpl());

        newRule.updateBody(new ArrayList(result.getAllAtoms().toCollection()));
        return newRule;
    }

    /**
     * TODO: implement it
     * TODO: explain
     */
    private static ExtractEqNormResult normalizeSameLevelAtoms(final List<Function> initialAtoms,
                                                               final Substitution initialSubstitution) {
        /**
         * TODO: explain
         *
         * TODO: find better names
         */
        List<ExtractEqNormResult> results = initialAtoms.foldLeft(new F2<List<ExtractEqNormResult>, Function,
                List<ExtractEqNormResult>>() {
            @Override
            public List<ExtractEqNormResult> f(List<ExtractEqNormResult> accumulatedResults, Function atom) {
                Substitution currentSubstitution = accumulatedResults.head().getCurrentSubstitution();
                ExtractEqNormResult atomResult = normalizeAtom(atom, currentSubstitution);
                // Appends to the head of the list
                return List.cons(atomResult, accumulatedResults);
            }
        }, List.<ExtractEqNormResult>nil()).reverse();

        /**
         * TODO: aggregates the extracted values.
         */
        return null;
    }

    /**
     * TODO: implement it
     */
    private static ExtractEqNormResult normalizeAtom(Function atom, Substitution currentSubstitution) {
        if (atom.isAlgebraFunction()) {
            Predicate functionSymbol = atom.getFunctionSymbol();
            if (functionSymbol.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
                return normalizeLeftJoin(atom, currentSubstitution);
            }
            else if (functionSymbol.equals(OBDAVocabulary.SPARQL_JOIN)) {
                throw new RuntimeException("Not yet implemented");
            }
        }
        /**
         * TODO: continue
         */
        return null;
    }


    /**
     * TODO: implement it
     *
     */
    private static ExtractEqNormResult normalizeLeftJoin(final Function leftJoinMetaAtom,
                                                                                final Substitution initialSubstitution) {

        /**
         * TODO: may change with JOIN meta-predicates
         */
        final P2<List<Function>, List<Function>> splittedAtoms = splitLeftJoinSubAtoms(leftJoinMetaAtom);

        final List<Function> initialLeftAtoms = splittedAtoms._1();
        final List<Function> initialRightAtoms = splittedAtoms._2();

        ExtractEqNormResult leftNormalizationResults = normalizeSameLevelAtoms(initialLeftAtoms, initialSubstitution);

        ExtractEqNormResult rightNormalizationResults = normalizeSameLevelAtoms(initialRightAtoms,
                leftNormalizationResults.getCurrentSubstitution());

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

}
