package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class IntensionalDataNodeImpl extends DataNodeImpl<AtomPredicate> implements IntensionalDataNode {

    private static final String INTENSIONAL_DATA_NODE_STR = "INTENSIONAL";

    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;

    @AssistedInject
    private IntensionalDataNodeImpl(@Assisted DataAtom<AtomPredicate> atom,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory, SubstitutionFactory substitutionFactory) {
        super(atom, iqTreeTools, iqFactory, coreUtilsFactory);
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
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
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        return applyDescendingSubstitutionWithoutOptimizing(freshRenamingSubstitution);
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
    public FunctionalDependencies inferFunctionalDependencies() {
        return FunctionalDependencies.empty();
    }

    @Override
    public int hashCode() {
        return getProjectionAtom().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass()
                && getProjectionAtom().equals(((IntensionalDataNode) obj).getProjectionAtom());
    }

    @Override
    public String toString() {
        return INTENSIONAL_DATA_NODE_STR + " " + getProjectionAtom();
    }

    @Override
    public IntensionalDataNode newAtom(DataAtom<AtomPredicate> newAtom) {
        return iqFactory.createIntensionalDataNode(newAtom);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        return applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution);
    }

    private IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        DataAtom<AtomPredicate> atom = getProjectionAtom();
        DataAtom<AtomPredicate> newAtom = atomFactory.getDataAtom(atom.getPredicate(), substitutionFactory.onVariableOrGroundTerms().applyToTerms(descendingSubstitution, atom.getArguments()));
        return newAtom(newAtom);
    }

    /**
     * All the variables are required, because an intensional data node cannot be sparse.
     */
    @Override
    public synchronized VariableNonRequirement getVariableNonRequirement() {
        return VariableNonRequirement.empty();
    }
}
