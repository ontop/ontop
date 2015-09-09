package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.model.impl.ImmutableFunctionalTermImpl;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DataAtom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DataAtomImpl extends ImmutableFunctionalTermImpl implements DataAtom {

    private final AtomPredicate predicate;

    protected DataAtomImpl(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
        this.predicate = predicate;
    }

    protected DataAtomImpl(AtomPredicate predicate, VariableOrGroundTerm... variableOrGroundTerms) {
        super(predicate, variableOrGroundTerms);
        this.predicate = predicate;
    }

    @Override
    public AtomPredicate getPredicate() {
        return predicate;
    }

    @Override
    public int getEffectiveArity() {
        return getTerms().size();
    }

    @Override
    public boolean hasSamePredicateAndArity(DataAtom otherAtom) {
        if (!predicate.equals(otherAtom.getPredicate()))
            return false;

        if (getEffectiveArity() != otherAtom.getEffectiveArity())
            return false;

        return true;
    }

    @Override
    public ImmutableList<VariableOrGroundTerm> getVariablesOrGroundTerms() {
        return (ImmutableList<VariableOrGroundTerm>)(ImmutableList<?>)getImmutableTerms();
    }

    @Override
    public VariableOrGroundTerm getTerm(int index) {
        return (VariableOrGroundTerm) super.getTerm(index);
    }

    protected static boolean hasDuplicates(DataAtom atom) {
        ImmutableList<VariableOrGroundTerm> termList = atom.getVariablesOrGroundTerms();
        Set<VariableOrGroundTerm> termSet = new HashSet<>(termList);

        return termSet.size() < termList.size();
    }
}
