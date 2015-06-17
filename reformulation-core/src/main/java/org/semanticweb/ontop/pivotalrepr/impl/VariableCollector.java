package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.NonFunctionalTerm;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.List;

/**
 * Collects all the variables found in the nodes.
 */
public class VariableCollector implements QueryNodeVisitor {

    private final ImmutableSet.Builder<VariableImpl> collectedVariableBuilder;

    private VariableCollector() {
        collectedVariableBuilder = ImmutableSet.builder();
    }

    public static ImmutableSet<VariableImpl> collectVariables(IntermediateQuery query) {
        return collectVariables(query.getNodesInBottomUpOrder());
    }

    public static ImmutableSet<VariableImpl> collectVariables(List<QueryNode> nodes) {
        VariableCollector collector = new VariableCollector();

        for (QueryNode node : nodes) {
            node.acceptVisitor(collector);
        }
        return collector.collectedVariableBuilder.build();
    }

    @Override
    public void visit(OrdinaryDataNode ordinaryDataNode) {
        collectFromAtom(ordinaryDataNode.getAtom());
    }

    @Override
    public void visit(TableNode tableNode) {
        collectFromAtom(tableNode.getAtom());
    }

    @Override
    public void visit(InnerJoinNode innerJoinNode) {
        // Collects nothing
    }

    @Override
    public void visit(LeftJoinNode leftJoinNode) {
        // Collects nothing
    }

    @Override
    public void visit(FilterNode filterNode) {
        // Collects nothing
    }

    @Override
    public void visit(ConstructionNode constructionNode) {
        collectFromPureAtom(constructionNode.getProjectionAtom());
    }

    @Override
    public void visit(UnionNode unionNode) {
        // Collects nothing
    }

    private void collectFromAtom(DataAtom atom) {
        for (VariableOrGroundTerm term : atom.getVariablesOrGroundTerms()) {
            if (term instanceof VariableImpl)
                collectedVariableBuilder.add((VariableImpl)term);
        }
    }

    private void collectFromPureAtom(PureDataAtom atom) {
        collectedVariableBuilder.addAll(atom.getVariableTerms());
    }
}
