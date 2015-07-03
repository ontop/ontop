package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.DataAtomImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.PureDataAtom;

public class PureDataAtomImpl extends DataAtomImpl implements PureDataAtom {

    protected PureDataAtomImpl(AtomPredicate predicate, ImmutableList<? extends VariableImpl> variables) {
        super(predicate, variables);
    }

    protected PureDataAtomImpl(AtomPredicate predicate, VariableImpl... variables) {
        super(predicate, variables);
    }

    @Override
    public ImmutableList<VariableImpl> getVariableTerms() {
        return (ImmutableList<VariableImpl>)(ImmutableList<?>)getImmutableTerms();
    }

    @Override
    public VariableImpl getTerm(int index) {
        return (VariableImpl) super.getTerm(index);
    }

    public static boolean isPureDataAtom(DataAtom atom) {
        if (atom instanceof PureDataAtom)
            return true;

        return isRespectingPureDataAtomConstraints(atom);
    }

    protected static boolean isRespectingPureDataAtomConstraints(DataAtom atom) {
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
