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
public abstract class InternalCompositeExecutor<T extends QueryOptimizationProposal> implements InternalProposalExecutor<T> {

    @Override
    public ProposalResults apply(final T initialProposal, IntermediateQuery query, final QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        ImmutableList<InternalProposalExecutor<T>> executors = createExecutors();
        Iterator<InternalProposalExecutor<T>> executorIterator = executors.iterator();

        // Non-final
        Optional<T> optionalProposal = Optional.of(initialProposal);

        ProposalResults results;
        do {
            InternalProposalExecutor<T> executor = executorIterator.next();

            results = executor.apply(optionalProposal.get(), query, treeComponent);
            optionalProposal = createNewProposal(results);
            query = results.getResultingQuery();

        } while(optionalProposal.isPresent() && executorIterator.hasNext());

        return results;
    }

    protected abstract Optional<T> createNewProposal(ProposalResults results);

    protected abstract ImmutableList<InternalProposalExecutor<T>> createExecutors();


}
