package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.DataNode;
import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposal;
import it.unibz.inf.ontop.pivotalrepr.SubstitutionResults;
import it.unibz.inf.ontop.pivotalrepr.TrueNode;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

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
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {

        return atom.getArguments()
                .stream()
                .filter(Variable.class::isInstance)
                .map(Variable.class::cast)
                .collect(toImmutableSet());

//        ImmutableSet.Builder<Variable> variableBuilder = ImmutableSet.builder();
//        for (VariableOrGroundTerm term : atom.getArguments()) {
//            if (term instanceof Variable)
//                variableBuilder.add((Variable)term);
//        }
//        return variableBuilder.build();
    }

    protected static <T extends DataNode> SubstitutionResults<T> applySubstitution(
            T dataNode, ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        DataAtom newAtom = substitution.applyToDataAtom(dataNode.getProjectionAtom());
        T newNode = (T) dataNode.newAtom(newAtom);
        return new SubstitutionResultsImpl<>(newNode, substitution);
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        throw new UnsupportedOperationException("A DataNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        throw new UnsupportedOperationException("A DataNode is not expected to have a child");
    }

}
