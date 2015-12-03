package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;

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
        for (VariableOrGroundTerm term : atom.getVariablesOrGroundTerms()) {
            if (term instanceof Variable)
                variableBuilder.add((Variable)term);
        }
        return variableBuilder.build();
    }
}
