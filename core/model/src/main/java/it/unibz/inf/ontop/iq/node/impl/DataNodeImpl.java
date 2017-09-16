package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.SubstitutionResults;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

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
                .collect(ImmutableCollectors.toSet());

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
        return DefaultSubstitutionResults.newNode(newNode, substitution);
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        throw new UnsupportedOperationException("A DataNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        throw new UnsupportedOperationException("A DataNode is not expected to have a child");
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }
}
