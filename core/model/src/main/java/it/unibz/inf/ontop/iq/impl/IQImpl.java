package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;

public class IQImpl implements IQ {

    private final DistinctVariableOnlyDataAtom projectionAtom;
    private final IQTree tree;
    private final IntermediateQueryFactory iqFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    /**
     * Lazy (created on demand)
     */
    @Nullable
    private VariableGenerator variableGenerator;

    @AssistedInject
    private IQImpl(@Assisted DistinctVariableOnlyDataAtom projectionAtom, @Assisted IQTree tree,
                   IntermediateQueryFactory iqFactory,
                   CoreUtilsFactory coreUtilsFactory, OntopModelSettings settings) {
        
        this.projectionAtom = projectionAtom;
        this.tree = tree;
        this.iqFactory = iqFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.variableGenerator = null;

        if (settings.isTestModeEnabled())
            validate();
    }


    @Override
    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return projectionAtom;
    }

    @Override
    public IQTree getTree() {
        return tree;
    }

    @Override
    public synchronized VariableGenerator getVariableGenerator() {
        if (variableGenerator == null)
            variableGenerator = coreUtilsFactory.createVariableGenerator(tree.getKnownVariables());
        return variableGenerator;
    }

    /*
     * Assumes that trees declared as lifted will return themselves
     */
    @Override
    public IQ normalizeForOptimization() {
        IQTree newTree = tree.normalizeForOptimization(getVariableGenerator());
        return newTree == tree
                ? this
                : iqFactory.createIQ(projectionAtom, newTree);
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
        validateProjectedVariables();

        tree.validate();
    }

    private void validateProjectedVariables() throws InvalidIntermediateQueryException {
        ImmutableSet<Variable> projectedVariables = tree.getVariables();
        if (!projectedVariables.equals(projectionAtom.getVariables())) {
            throw new InvalidIntermediateQueryException("The variables projected by the root node"
                    +  projectedVariables + " do not match the projection atom " + projectionAtom.getVariables());
        }
    }

    @Override
    public String toString() {
        return projectionAtom + "\n" + tree.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        IQImpl other = (IQImpl)o;
        return projectionAtom.equals(other.projectionAtom) && tree.equals(other.tree);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
