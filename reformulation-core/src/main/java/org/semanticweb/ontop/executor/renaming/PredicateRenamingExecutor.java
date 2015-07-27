package org.semanticweb.ontop.executor.renaming;

import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.IntermediateQueryUtils;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.PredicateRenamingProposal;

/**
 * TODO: explain
 */
public class PredicateRenamingExecutor implements StandardProposalExecutor<PredicateRenamingProposal> {

    @Override
    public IntermediateQuery apply(PredicateRenamingProposal proposal, IntermediateQuery inputQuery)
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
            return IntermediateQueryUtils.convertToBuilderAndTransform(inputQuery, renamer).build();
        }
        catch (IntermediateQueryBuilderException | QueryNodeTransformationException | NotNeededNodeException e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }
}
