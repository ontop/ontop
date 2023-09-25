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
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;


public class QueryRenamerImpl implements QueryRenamer {

    private final InjectiveSubstitution<Variable> renamingSubstitution;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;

    /**
     * See {@link QueryTransformerFactory#createRenamer(InjectiveSubstitution)}
     */
    @AssistedInject
    private QueryRenamerImpl(@Assisted InjectiveSubstitution<Variable> injectiveVar2VarSubstitution,
                             IntermediateQueryFactory iqFactory, AtomFactory atomFactory, SubstitutionFactory substitutionFactory) {
        this.renamingSubstitution = injectiveVar2VarSubstitution;
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQ transform(IQ originalQuery) {
        if (renamingSubstitution.isEmpty())
            return originalQuery;

        HomogeneousIQTreeVisitingTransformer iqTransformer = getIQTransformer();
        IQTree newIQTree = originalQuery.getTree().acceptTransformer(iqTransformer);

        DistinctVariableOnlyDataAtom atom = originalQuery.getProjectionAtom();
        ImmutableList<Variable> newArguments = substitutionFactory.apply(renamingSubstitution, atom.getArguments());
        DistinctVariableOnlyDataAtom newProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(atom.getPredicate(), newArguments);

        return iqFactory.createIQ(newProjectionAtom, newIQTree);
    }

    @Override
    public IQTree transform(IQTree originalTree) {
        if (renamingSubstitution.isEmpty())
            return originalTree;

        HomogeneousIQTreeVisitingTransformer iqTransformer = getIQTransformer();
        return originalTree.acceptTransformer(iqTransformer);
    }

    private HomogeneousIQTreeVisitingTransformer getIQTransformer() {
        QueryNodeRenamer nodeTransformer = new QueryNodeRenamer(iqFactory, renamingSubstitution, atomFactory, substitutionFactory);
        return new HomogeneousIQTreeVisitingTransformer(nodeTransformer, iqFactory);
    }
}
