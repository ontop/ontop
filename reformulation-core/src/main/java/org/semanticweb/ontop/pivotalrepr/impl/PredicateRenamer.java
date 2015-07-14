package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: explain
 *
 * Immutable
 */
public class PredicateRenamer implements QueryNodeOptimizer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final AtomPredicate formerPredicate;
    private final AtomPredicate newPredicate;
    private final IntermediateQuery intermediateQuery;


    public PredicateRenamer(IntermediateQuery intermediateQuery, AtomPredicate formerPredicate, AtomPredicate newPredicate) {
        this.intermediateQuery = intermediateQuery;
        this.formerPredicate = formerPredicate;
        this.newPredicate = newPredicate;
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(InnerJoinNode node) {
        return Optional.absent();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(LeftJoinNode leftJoinNode) {
        return Optional.absent();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(FilterNode filterNode) {
        return Optional.absent();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(ConstructionNode formerNode) {
        DataAtom currentAtom = formerNode.getProjectionAtom();
        AtomPredicate currentPredicate = currentAtom.getPredicate();

        /**
         * Makes a replacement proposal for the construction node
         */
        if (currentPredicate.equals(formerPredicate)) {
            DataAtom newDataAtom = DATA_FACTORY.getDataAtom(newPredicate,
                    formerNode.getProjectionAtom().getVariablesOrGroundTerms());

            ConstructionNode newNode = new ConstructionNodeImpl(newDataAtom, formerNode.getSubstitution(),
                    formerNode.getOptionalModifiers());

            LocalOptimizationProposal renamingProposal = new ReplaceNodeProposalImpl(intermediateQuery,
                    formerNode, newNode);

            return Optional.of(renamingProposal);
        }
        else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(UnionNode unionNode) {
        return Optional.absent();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(OrdinaryDataNode ordinaryDataNode) {
        return Optional.absent();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(TableNode tableNode) {
        return Optional.absent();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(GroupNode groupNode) {
        return Optional.absent();
    }
}
