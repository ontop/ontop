package it.unibz.inf.ontop.pivotalrepr.unfolding.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractor;

import java.util.HashSet;
import java.util.Set;

/**
 * Low-level !
 *
 * Can be extended.
 */
public class ProjectedVariableExtractorImpl implements ProjectedVariableExtractor {

    private final IntermediateQuery query;
    /**
     * Mutable
     */
    private final Set<Variable> collectedVariables;

    public ProjectedVariableExtractorImpl(IntermediateQuery query) {
        this.query = query;
        collectedVariables = new HashSet<>();
    }

    @Override
    public ImmutableSet<Variable> getCollectedProjectedVariables() {
        return ImmutableSet.copyOf(collectedVariables);
    }

    @Override
    public void visit(ConstructionNode constructionNode) {
        visitConstructionOrDataNode(constructionNode);
    }

    @Override
    public void visit(UnionNode unionNode) {
        visitChildren(unionNode);
    }

    @Override
    public void visit(InnerJoinNode innerJoinNode) {
        visitChildren(innerJoinNode);
    }

    @Override
    public void visit(LeftJoinNode leftJoinNode) {
        visitChildren(leftJoinNode);
    }

    @Override
    public void visit(FilterNode filterNode) {
        visitChildren(filterNode);
    }

    @Override
    public void visit(IntensionalDataNode intensionalDataNode) {
        visitConstructionOrDataNode(intensionalDataNode);
    }

    @Override
    public void visit(ExtensionalDataNode extensionalDataNode) {
        visitConstructionOrDataNode(extensionalDataNode);
    }

    @Override
    public void visit(GroupNode groupNode) {
        visitChildren(groupNode);
    }

    @Override
    public void visit(EmptyNode emptyNode) {
        collectedVariables.addAll(emptyNode.getProjectedVariables());
        // No need to visit its children
    }

    protected void visitConstructionOrDataNode(ConstructionOrDataNode node) {
        collectedVariables.addAll(node.getProjectedVariables());
        // No need to visit its children
    }

    protected void visitChildren(QueryNode node) {
        query.getChildren(node).stream()
                .forEach(child -> {
                    child.acceptVisitor(this);
                });
    }


}
