package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class IntensionalDataNodeImpl extends DataNodeImpl<AtomPredicate> implements IntensionalDataNode {

    private static final String INTENSIONAL_DATA_NODE_STR = "INTENSIONAL";
    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private IntensionalDataNodeImpl(@Assisted DataAtom<AtomPredicate> atom,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory) {
        super(atom, iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public IntensionalDataNode clone() {
        return iqFactory.createIntensionalDataNode(getProjectionAtom());
    }

    @Override
    public IntensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformIntensionalData(this);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformIntensionalData(this, context);
    }

    /**
     * Intensional data nodes are assumed to correspond to triple/quad patterns, which are distinct by definition
     */
    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitIntensionalData(this);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyDescendingSubstitution(freshRenamingSubstitution, Optional.empty());
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution);
    }

    @Override
    public VariableNullability getVariableNullability() {
        return coreUtilsFactory.createEmptyVariableNullability(getVariables());
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        return ImmutableSet.of(getVariables());
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof IntensionalDataNode)
                && ((IntensionalDataNode) node).getProjectionAtom().equals(this.getProjectionAtom());
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof IntensionalDataNode)
                && getProjectionAtom().equals(((IntensionalDataNode) queryNode).getProjectionAtom());
    }

    @Override
    public String toString() {
        return INTENSIONAL_DATA_NODE_STR + " " + getProjectionAtom();
    }

    @Override
    public IntensionalDataNode newAtom(DataAtom newAtom) {
        return iqFactory.createIntensionalDataNode(newAtom);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        DataAtom novelAtom = descendingSubstitution.applyToDataAtom(getProjectionAtom());
        return newAtom(novelAtom);
    }

    /**
     * All the variables are required, because an intensional data node cannot be sparse.
     */
    @Override
    public synchronized ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        return ImmutableSet.of();
    }
}
