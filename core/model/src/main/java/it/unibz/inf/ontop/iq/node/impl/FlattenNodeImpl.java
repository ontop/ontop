package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.QueryNodeVisitor;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

public abstract class FlattenNodeImpl<N extends FlattenNode> extends QueryNodeImpl implements FlattenNode {

    private final Variable arrayVariable;
    private final int arrayIndexIndex;
    private final DataAtom<RelationPredicate> dataAtom;
    protected final ImmutableList<Boolean> argumentNullability;

    protected FlattenNodeImpl(Variable arrayVariable, int arrayIndexIndex, DataAtom<RelationPredicate> dataAtom,
        ImmutableList<Boolean> argumentNullability) {
        this.arrayVariable = arrayVariable;
        this.arrayIndexIndex = arrayIndexIndex;
        this.dataAtom = dataAtom;

        if ((arrayIndexIndex >= dataAtom.getArguments().size())
                || arrayIndexIndex < 0)
            throw new IllegalArgumentException("The array index index must correspond to an argument of the data atom");
        this.argumentNullability = argumentNullability;

        if (argumentNullability.size() != dataAtom.getArity())
            throw new IllegalArgumentException("A nullability entry must be provided for each argument in the atom");

        if (this.argumentNullability.get(arrayIndexIndex))
            throw new IllegalArgumentException("The array index term must not be nullable");
    }

    @Override
    public Variable getArrayVariable() {
        return arrayVariable;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        throw new UnsupportedOperationException("TODO: support it");
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return Stream.concat(
                Stream.of(arrayVariable),
                dataAtom.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }
//    @Override
//    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
//        throw new UnsupportedOperationException("TODO: support it");
//    }
//
//    @Override
//    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
//        throw new UnsupportedOperationException("TODO: support it");
//    }

    protected String toString(String prefix) {
        return prefix + " " + arrayVariable + " -> " + dataAtom;
    }

    @Override
    public VariableOrGroundTerm getArrayIndexTerm() {
        return dataAtom.getTerm(arrayIndexIndex);
    }

    @Override
    public DataAtom<RelationPredicate> getProjectionAtom() {
        return dataAtom;
    }

    @Override
    public int getArrayIndexIndex() {
        return arrayIndexIndex;
    }

    //    @Override
//    public SubstitutionResults<N> applyDescendingSubstitution(
//            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
//        return applySubstitution(substitution);
//    }

//    @Override
//    public SubstitutionResults<N> applyAscendingSubstitution(
//            ImmutableSubstitution<? extends ImmutableTerm> substitution, QueryNode childNode, IntermediateQuery query) {
//        return applySubstitution(substitution);
//    }

//    protected SubstitutionResults<N> applySubstitution(
//            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
//
//        DataAtom newAtom = substitution.applyToDataAtom(getAtom());
//        ImmutableTerm newArrayTerm = substitution.apply(getArrayVariable());
//        if (!(newArrayTerm instanceof Variable))
//            throw new InvalidIntermediateQueryException("The array of a FlattenNode must remain a variable");
//
//        return new SubstitutionResultsImpl<>(newFlattenNode((Variable) newArrayTerm, newAtom), substitution);
//    }

//    @Override
//    public N rename(InjectiveVar2VarSubstitution renamingSubstitution) {
//        //noinspection OptionalGetWithoutIsPresent
//        return applySubstitution(renamingSubstitution).getOptionalNewNode().get();
//    }

    @Override
    public abstract N clone();

//    protected abstract N newFlattenNode(Variable newArrayVariable, DataAtom newAtom);

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of(arrayVariable);
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return dataAtom.getVariables();
    }

    @Override
    public ImmutableList<Boolean> getArgumentNullability() {
        return argumentNullability;
    }
}
