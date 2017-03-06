package it.unibz.inf.ontop.pivotalrepr.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryNodeRenamer;
import it.unibz.inf.ontop.pivotalrepr.transform.QueryRenamer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class QueryRenamerImpl extends NodeBasedQueryTransformer implements QueryRenamer {

    private final InjectiveVar2VarSubstitution renamingSubstitution;

    @Inject
    private QueryRenamerImpl(@Assisted InjectiveVar2VarSubstitution injectiveVar2VarSubstitution,
                             IntermediateQueryFactory iqFactory) {
        super(new QueryNodeRenamer(iqFactory, injectiveVar2VarSubstitution));
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
