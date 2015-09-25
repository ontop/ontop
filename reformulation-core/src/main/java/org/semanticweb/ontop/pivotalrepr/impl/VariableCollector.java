package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.List;

/**
 * Collects all the variables found in the nodes.
 */
public class VariableCollector extends QueryNodeVisitorImpl {

    private final ImmutableSet.Builder<Variable> collectedVariableBuilder;

    private VariableCollector() {
        collectedVariableBuilder = ImmutableSet.builder();
    }

    public static ImmutableSet<Variable> collectVariables(IntermediateQuery query) {
        return collectVariables(query.getNodesInBottomUpOrder());
    }

    public static ImmutableSet<Variable> collectVariables(List<QueryNode> nodes) {
        VariableCollector collector = new VariableCollector();

        for (QueryNode node : nodes) {
            node.acceptVisitor(collector);
        }
        return collector.collectedVariableBuilder.build();
    }

    public static ImmutableSet<Variable> collectVariables(QueryNode node) {
        VariableCollector collector = new VariableCollector();
        node.acceptVisitor(collector);
        return collector.collectedVariableBuilder.build();
    }

    @Override
    public void visit(IntensionalDataNode intensionalDataNode) {
        collectFromAtom(intensionalDataNode.getAtom());
    }

    @Override
    public void visit(ExtensionalDataNode extensionalDataNode) {
        collectFromAtom(extensionalDataNode.getAtom());
    }

    @Override
    public void visit(GroupNode groupNode) {
        for (NonGroundTerm term : groupNode.getGroupingTerms()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm) term).getVariables());
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
            if (term instanceof Variable)
                collectedVariableBuilder.add((Variable)term);
        }
    }

    private void collectFromSubstitution(ImmutableSubstitution<ImmutableTerm> substitution) {
        ImmutableMap<Variable, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm)term).getVariables());
            }
        }
    }
}
