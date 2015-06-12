package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.NonFunctionalTerm;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.PureDataAtom;

public class PureDataAtomImpl extends FunctionFreeDataAtomImpl implements PureDataAtom {

    protected PureDataAtomImpl(AtomPredicate predicate, ImmutableList<VariableImpl> variables) {
        super(predicate, (ImmutableList<NonFunctionalTerm>)(ImmutableList<?>)variables);

        // Check constraints
        if (hasDuplicates(this)) {
            throw new IllegalArgumentException(variables + " must not have duplicates.");
        }
    }

    protected PureDataAtomImpl(AtomPredicate predicate, VariableImpl... variables) {
        super(predicate, variables);

        // Check constraints
        if (hasDuplicates(this)) {
            throw new IllegalArgumentException(variables + " must not have duplicates.");
        }

    }

    @Override
    public ImmutableList<VariableImpl> getVariableTerms() {
        return (ImmutableList<VariableImpl>)(ImmutableList<?>)getImmutableTerms();
    }

    /**
     * Trivial since a PureDataAtom is only composed of variables.
     */
    @Override
    public boolean subsumes(FunctionFreeDataAtom otherAtom) {
        return hasSamePredicateAndArity(otherAtom);
    }

    @Override
    public VariableImpl getTerm(int index) {
        return (VariableImpl) super.getTerm(index);
    }

    @Override
    public boolean isEquivalent(FunctionFreeDataAtom otherAtom) {
        if (!hasSamePredicateAndArity(otherAtom))
            return false;

        return isPureDataAtom(otherAtom);
    }

    public static boolean isPureDataAtom(FunctionFreeDataAtom atom) {
        if (atom instanceof PureDataAtom)
            return true;

        return isRespectingPureDataAtomConstraints(atom);
    }

    protected static boolean isRespectingPureDataAtomConstraints(FunctionFreeDataAtom atom) {
        /**
         * No duplicate
         */
        if (hasDuplicates(atom))
            return false;

        /**
         * Only variable
         */
        for (ImmutableTerm term : atom.getImmutableTerms()) {
            if (! (term instanceof Variable))
                return false;
        }

        return true;
    }
}
