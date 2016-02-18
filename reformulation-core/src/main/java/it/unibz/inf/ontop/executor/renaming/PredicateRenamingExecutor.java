package it.unibz.inf.ontop.executor.renaming;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.NotNeededNodeException;
import it.unibz.inf.ontop.pivotalrepr.StandardProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.impl.IntermediateQueryUtils;
import it.unibz.inf.ontop.pivotalrepr.proposal.PredicateRenamingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;

/**
 * TODO: explain
 */
public class PredicateRenamingExecutor implements StandardProposalExecutor<PredicateRenamingProposal, ProposalResults> {

    @Override
    public ProposalResults apply(PredicateRenamingProposal proposal, IntermediateQuery inputQuery)
            throws InvalidQueryOptimizationProposalException {
        AtomPredicate newPredicate = proposal.getNewPredicate();
        AtomPredicate formerPredicate = proposal.getFormerPredicate();

        try {
            PredicateRenamingChecker.checkNonExistence(inputQuery, newPredicate);
        } catch(AlreadyExistingPredicateException e) {
            throw new InvalidQueryOptimizationProposalException(e.getMessage());
        }
        PredicateRenamer renamer = new PredicateRenamer(formerPredicate, newPredicate);
        try {
            IntermediateQuery newQuery = IntermediateQueryUtils.convertToBuilderAndTransform(inputQuery, renamer).build();
            return new ProposalResultsImpl(newQuery);
        }
        catch (NotNeededNodeException e) {
            throw new IllegalStateException("PredicateRenamingExecutor should not remove any node");
        }
    }
}
