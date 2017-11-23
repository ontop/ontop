package it.unibz.inf.ontop.iq.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;

public class IQImpl implements IQ {

    private final DistinctVariableOnlyDataAtom projectionAtom;
    private final IQTree tree;
    private final TermFactory termFactory;

    /**
     * Lazy (created on demand)
     */
    @Nullable
    private VariableGenerator variableGenerator;

    @AssistedInject
    private IQImpl(@Assisted DistinctVariableOnlyDataAtom projectionAtom, @Assisted IQTree tree,
                   TermFactory termFactory) {
        
        this.projectionAtom = projectionAtom;
        this.tree = tree;
        this.termFactory = termFactory;
        this.variableGenerator = null;
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
            variableGenerator = new VariableGenerator(tree.getKnownVariables(), termFactory);
        return variableGenerator;
    }

    /*
     * Assumes that trees declared as lifted will return themselves
     */
    @Override
    public IQ liftBinding() {
        IQTree newTree = tree.liftBinding(getVariableGenerator());
        return newTree == tree
                ? this
                : new IQImpl(projectionAtom, newTree, termFactory);
    }
}
