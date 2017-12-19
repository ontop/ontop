package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.impl.QueryNodeRenamer;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class QueryRenamerImpl extends NodeBasedQueryTransformer implements QueryRenamer {

    private final InjectiveVar2VarSubstitution renamingSubstitution;
    private final AtomFactory atomFactory;

    @Inject
    private QueryRenamerImpl(@Assisted InjectiveVar2VarSubstitution injectiveVar2VarSubstitution,
                             IntermediateQueryFactory iqFactory, AtomFactory atomFactory) {
        super(new QueryNodeRenamer(iqFactory, injectiveVar2VarSubstitution, atomFactory));
        renamingSubstitution = injectiveVar2VarSubstitution;
        this.atomFactory = atomFactory;
    }

    /**
     * Renames the projected variables
     */
    @Override
    protected DistinctVariableOnlyDataAtom transformProjectionAtom(DistinctVariableOnlyDataAtom atom) {
        ImmutableList<Variable> newArguments = atom.getArguments().stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toList());

        return atomFactory.getDistinctVariableOnlyDataAtom(atom.getPredicate(), newArguments);
    }
}
