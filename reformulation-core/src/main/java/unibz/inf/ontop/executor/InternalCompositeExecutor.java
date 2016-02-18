package unibz.inf.ontop.executor;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;

import java.util.Iterator;

/**
 * TODO: explain
 */
public abstract class InternalCompositeExecutor<P extends QueryOptimizationProposal<R>, R extends ProposalResults>
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
