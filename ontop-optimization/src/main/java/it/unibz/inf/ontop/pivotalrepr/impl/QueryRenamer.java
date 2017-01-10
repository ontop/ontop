package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class QueryRenamer extends NodeBasedQueryTransformer {

    private final InjectiveVar2VarSubstitution renamingSubstitution;

    public QueryRenamer(InjectiveVar2VarSubstitution injectiveVar2VarSubstitution) {
        super(new QueryNodeRenamer(injectiveVar2VarSubstitution));
        renamingSubstitution = injectiveVar2VarSubstitution;
    }

    /**
     * Renames the projected variables
     */
    @Override
    protected DistinctVariableOnlyDataAtom transformProjectionAtom(DistinctVariableOnlyDataAtom atom) {
        ImmutableList<Variable> newArguments = atom.getArguments().stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toList());

        return DATA_FACTORY.getDistinctVariableOnlyDataAtom(atom.getPredicate(), newArguments);
    }
}
