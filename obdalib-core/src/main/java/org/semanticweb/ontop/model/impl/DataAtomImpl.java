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
    public boolean isEquivalent(DataAtom otherAtom) {
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
            VariableOrGroundTerm localTerm = getTerm(i);
            VariableOrGroundTerm otherTerm = otherAtom.getTerm(i);

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
             * Both are ground terms: should be equal to be equivalent
             * TODO: make sure this works for functional ground terms!
             */
            else {
                if (!localTerm.equals(otherTerm))
                    return false;
            }
        }

        return true;
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
