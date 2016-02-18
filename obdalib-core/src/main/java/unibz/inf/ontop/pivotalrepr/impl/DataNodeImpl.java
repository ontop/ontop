package unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import unibz.inf.ontop.model.DataAtom;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.Variable;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.DataNode;
import unibz.inf.ontop.pivotalrepr.SubstitutionResults;

/**
 *
 */
public abstract class DataNodeImpl extends QueryNodeImpl implements DataNode {

    private DataAtom atom;

    protected DataNodeImpl(DataAtom atom) {
        this.atom = atom;
    }

    @Override
    public DataAtom getProjectionAtom() {
        return atom;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        ImmutableSet.Builder<Variable> variableBuilder = ImmutableSet.builder();
        for (VariableOrGroundTerm term : atom.getArguments()) {
            if (term instanceof Variable)
                variableBuilder.add((Variable)term);
        }
        return variableBuilder.build();
    }

    protected static <T extends DataNode> SubstitutionResults<T> applySubstitution(
            T dataNode, ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {

        DataAtom newAtom = substitution.applyToDataAtom(dataNode.getProjectionAtom());
        T newNode = (T) dataNode.newAtom(newAtom);
        return new SubstitutionResultsImpl<>(newNode, substitution);
    }

}
