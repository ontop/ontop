package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.QueryNodeRenamer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public class QueryRenamerImpl implements QueryRenamer {

    private final InjectiveVar2VarSubstitution renamingSubstitution;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;

    /**
     * See {@link QueryTransformerFactory#createRenamer(InjectiveVar2VarSubstitution)}
     */
    @AssistedInject
    private QueryRenamerImpl(@Assisted InjectiveVar2VarSubstitution injectiveVar2VarSubstitution,
                             IntermediateQueryFactory iqFactory, AtomFactory atomFactory) {
        renamingSubstitution = injectiveVar2VarSubstitution;
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
    }

    /**
     * Renames the projected variables
     */
    private DistinctVariableOnlyDataAtom transformProjectionAtom(DistinctVariableOnlyDataAtom atom) {
        ImmutableList<Variable> newArguments = atom.getArguments().stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toList());

        return atomFactory.getDistinctVariableOnlyDataAtom(atom.getPredicate(), newArguments);
    }

    @Override
    public IQ transform(IQ originalQuery) {
        QueryNodeRenamer nodeTransformer = new QueryNodeRenamer(iqFactory, renamingSubstitution, atomFactory);
        HomogeneousIQTreeVisitingTransformer iqTransformer = new HomogeneousIQTreeVisitingTransformer(nodeTransformer, iqFactory);

        IQTree newIQTree = originalQuery.getTree().acceptTransformer(iqTransformer);
        DistinctVariableOnlyDataAtom newProjectionAtom = transformProjectionAtom(originalQuery.getProjectionAtom());

        return iqFactory.createIQ(newProjectionAtom, newIQTree);
    }
}
