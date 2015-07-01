package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
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

    public static ImmutableSet<VariableImpl> collectVariables(QueryNode node) {
        VariableCollector collector = new VariableCollector();
        node.acceptVisitor(collector);
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
    public void visit(GroupNode groupNode) {
        for (NonGroundTerm term : groupNode.getGroupingTerms()) {
            if (term instanceof VariableImpl) {
                collectedVariableBuilder.add((VariableImpl)term);
            }
            else {
                collectedVariableBuilder.addAll((List<VariableImpl>)(List<?>)term.getReferencedVariables());
            }
        }
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

        collectFromAtom(constructionNode.getProjectionAtom());
        collectFromSubstitution(constructionNode.getSubstitution());
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

    private void collectFromSubstitution(ImmutableSubstitution<ImmutableTerm> substitution) {
        ImmutableMap<VariableImpl, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
            // TODO: remove this cast!!!
            collectedVariableBuilder.addAll((ImmutableSet<VariableImpl>)(ImmutableSet<?>)term.getReferencedVariables());
        }
    }
}
