package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: explain
 */
public class PredicateRenamingChecker implements QueryNodeVisitor  {

    private final AtomPredicate forbiddenPredicate;

    private PredicateRenamingChecker(AtomPredicate forbiddenPredicate) {
        this.forbiddenPredicate = forbiddenPredicate;
    }

    /**
     * High-level method
     */
    public static void checkNonExistence(IntermediateQuery query, AtomPredicate forbiddenPredicate)
            throws AlreadyExistingPredicateException{
        PredicateRenamingChecker checker = new PredicateRenamingChecker(forbiddenPredicate);

        for (QueryNode node : query.getNodesInBottomUpOrder()) {
            // May throw an AlreadyExistingPredicateException
            node.acceptVisitor(checker);
        }
    }

    @Override
    public void visit(ConstructionNode constructionNode) throws AlreadyExistingPredicateException {
        checkAtom(constructionNode.getProjectionAtom());
    }

    @Override
    public void visit(UnionNode unionNode) {
        // Does nothing
    }

    @Override
    public void visit(InnerJoinNode innerJoinNode) {
        // Does nothing
    }

    @Override
    public void visit(LeftJoinNode leftJoinNode) {
        // Does nothing
    }

    @Override
    public void visit(FilterNode filterNode) {
        // Does nothing
    }

    @Override
    public void visit(OrdinaryDataNode ordinaryDataNode) throws AlreadyExistingPredicateException {
        checkAtom(ordinaryDataNode.getAtom());
    }

    @Override
    public void visit(TableNode tableNode) throws AlreadyExistingPredicateException {
        checkAtom(tableNode.getAtom());
    }

    @Override
    public void visit(GroupNode groupNode) {
        // Does nothing
    }

    private void checkAtom(DataAtom atom) throws AlreadyExistingPredicateException {
        if (atom.getPredicate().equals(forbiddenPredicate)) {
            throw new AlreadyExistingPredicateException(forbiddenPredicate.getName());
        }
    }
}

