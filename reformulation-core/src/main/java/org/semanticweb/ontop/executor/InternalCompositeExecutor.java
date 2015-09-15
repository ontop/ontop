package org.semanticweb.ontop.executor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.*;

import java.util.Iterator;

/**
 * TODO: explain
 */
public abstract class InternalCompositeExecutor<P extends QueryOptimizationProposal, R extends ProposalResults>
        implements InternalProposalExecutor<P, R> {

    @Override
    public R apply(final P initialProposal, IntermediateQuery query, final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        ImmutableList<? extends InternalProposalExecutor<P, R>> executors = createExecutors();
        Iterator<? extends InternalProposalExecutor<P, R>> executorIterator = executors.iterator();

        // Non-final
        Optional<P> optionalProposal = Optional.of(initialProposal);

        R results;
        do {
            InternalProposalExecutor<P, R> executor = executorIterator.next();

            results = executor.apply(optionalProposal.get(), query, treeComponent);
            optionalProposal = createNewProposal(results);
            query = results.getResultingQuery();

        } while(optionalProposal.isPresent() && executorIterator.hasNext());

        return results;
    }

    protected abstract Optional<P> createNewProposal(R results);

    protected abstract ImmutableList<? extends InternalProposalExecutor<P, R>> createExecutors();


}
