package org.semanticweb.ontop.executor.renaming;

import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.QueryNodeVisitorImpl;

/**
 * TODO: explain
 */
public class PredicateRenamingChecker extends QueryNodeVisitorImpl {

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
    public void visit(IntensionalDataNode intensionalDataNode) throws AlreadyExistingPredicateException {
        checkAtom(intensionalDataNode.getAtom());
    }

    @Override
    public void visit(ExtensionalDataNode extensionalDataNode) throws AlreadyExistingPredicateException {
        checkAtom(extensionalDataNode.getAtom());
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

