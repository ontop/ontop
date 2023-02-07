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

    /**
     * Renames the projected variables
     */
    private DistinctVariableOnlyDataAtom transformProjectionAtom(DistinctVariableOnlyDataAtom atom) {
        ImmutableList<Variable> newArguments = substitutionFactory.onVariables().apply(renamingSubstitution, atom.getArguments());

        return atomFactory.getDistinctVariableOnlyDataAtom(atom.getPredicate(), newArguments);
    }

    @Override
    public IQ transform(IQ originalQuery) {
        QueryNodeRenamer nodeTransformer = new QueryNodeRenamer(iqFactory, renamingSubstitution, atomFactory, substitutionFactory);
        HomogeneousIQTreeVisitingTransformer iqTransformer = new HomogeneousIQTreeVisitingTransformer(nodeTransformer, iqFactory);

        IQTree newIQTree = originalQuery.getTree().acceptTransformer(iqTransformer);
        DistinctVariableOnlyDataAtom newProjectionAtom = transformProjectionAtom(originalQuery.getProjectionAtom());

        return iqFactory.createIQ(newProjectionAtom, newIQTree);
    }
}
