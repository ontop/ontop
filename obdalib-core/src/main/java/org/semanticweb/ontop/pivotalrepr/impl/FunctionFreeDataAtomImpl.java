package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.NonFunctionalTerm;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.ImmutableFunctionalTermImpl;
import org.semanticweb.ontop.pivotalrepr.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class FunctionFreeDataAtomImpl extends ImmutableFunctionalTermImpl implements FunctionFreeDataAtom {

    private final AtomPredicate predicate;

    protected FunctionFreeDataAtomImpl(AtomPredicate predicate, ImmutableList<NonFunctionalTerm> nonFunctionalTerms) {
        super(predicate, (ImmutableList<ImmutableTerm>)(ImmutableList<?>)nonFunctionalTerms);
        this.predicate = predicate;
    }

    protected FunctionFreeDataAtomImpl(AtomPredicate predicate, NonFunctionalTerm... nonFunctionalTerms) {
        super(predicate, nonFunctionalTerms);
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
    public boolean isEquivalent(FunctionFreeDataAtom otherAtom) {
        if (!hasSamePredicateAndArity(otherAtom))
            return false;


        Set<Variable> localVariables = new HashSet<>();
        Set<Variable> otherVariables = new HashSet<>();
        // Local --> remote
        Map<Variable, Variable> varMappings = new HashMap<>();

        /**
         * Checks the arguments
         */
        int effectiveArity = getEffectiveArity();
        for (int i=0; i < effectiveArity ; i++ ) {
            NonFunctionalTerm localTerm = getTerm(i);
            NonFunctionalTerm otherTerm = otherAtom.getTerm(i);

            boolean localIsVariable = (localTerm instanceof Variable);
            boolean otherIsVariable = (otherTerm instanceof Variable);

            if (localIsVariable != otherIsVariable) {
                return false;
            }

            /**
             * Both are variables
             */
            if (localIsVariable) {
                Variable localVariable = (Variable) localTerm;
                Variable otherVariable = (Variable) otherTerm;

                boolean newLocalVariable = localVariables.contains(localVariable);
                boolean newOtherVariable = localVariables.contains(otherVariable);

                if (newLocalVariable != newOtherVariable)
                    return false;
                /**
                 * If both new: register the variables and the mapping
                 */
                if (!newLocalVariable) {
                    localVariables.add(localVariable);
                    otherVariables.add(otherVariable);
                    varMappings.put(localVariable, otherVariable);
                }
                /**
                 * If not new: check that the equivalence is kept
                 */
                else if (!varMappings.get(localVariable).equals(otherVariable))
                    return false;
            }
            /**
             * Both are constants: should be equal to be equivalent
             */
            else {
                if (!localTerm.equals(otherTerm))
                    return false;
            }
        }

        return true;
    }

    protected boolean hasSamePredicateAndArity(FunctionFreeDataAtom otherAtom) {
        if (!predicate.equals(otherAtom.getPredicate()))
            return false;

        if (getEffectiveArity() != otherAtom.getEffectiveArity())
            return false;

        return true;
    }

    @Override
    public ImmutableList<NonFunctionalTerm> getNonFunctionalTerms() {
        return (ImmutableList<NonFunctionalTerm>)(ImmutableList<?>)getImmutableTerms();
    }

    @Override
    public NonFunctionalTerm getTerm(int index) {
        return (NonFunctionalTerm) super.getTerm(index);
    }

    protected static boolean hasDuplicates(FunctionFreeDataAtom atom) {
        ImmutableList<NonFunctionalTerm> termList = atom.getNonFunctionalTerms();
        Set<NonFunctionalTerm> termSet = new HashSet<>(termList);

        return termSet.size() < termList.size();
    }
}
