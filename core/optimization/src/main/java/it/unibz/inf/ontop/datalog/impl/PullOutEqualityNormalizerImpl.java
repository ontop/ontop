package it.unibz.inf.ontop.datalog.impl;

import com.google.inject.Inject;
import fj.*;
import fj.data.List;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.model.term.Function;

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
 */
public class PullOutEqualityNormalizerImpl implements PullOutEqualityNormalizer {

    private final DatalogTools datalogTools;

    @Inject
    private PullOutEqualityNormalizerImpl(DatalogTools datalogTools) {
        this.datalogTools = datalogTools;
    }

    @Override
    public P2<List<Function>, List<Function>> splitLeftJoinSubAtoms(List<Function> ljSubAtoms) {

        // TODO: make it static (performance improvement).
        F<Function, Boolean> isNotDataOrCompositeAtomFct = atom -> !(datalogTools.isDataOrLeftJoinOrJoinAtom(atom));

        /**
         * Left: left of the first data/composite atom (usually empty).
         *
         * The first data/composite atom is thus the first element of the right list.
         */
        P2<List<Function>, List<Function>> firstDataAtomSplit = ljSubAtoms.span(isNotDataOrCompositeAtomFct);
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
}
