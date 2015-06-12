package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.model.NonFunctionalTerm;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Collects all the variables found in the nodes.
 */
public class VariableCollector implements QueryNodeVisitor {

    private final Set<Variable> collectedVariables;

    private VariableCollector() {
        collectedVariables = new HashSet<>();
    }

    public static Set<Variable> collectVariables(IntermediateQuery query) {
        VariableCollector collector = new VariableCollector();

        for (QueryNode node : query.getNodesInBottomUpOrder()) {
            node.acceptVisitor(collector);
        }
        return collector.collectedVariables;
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
    public void visit(SimpleFilterNode simpleFilterNode) {
        // Collects nothing
    }

    @Override
    public void visit(ProjectionNode projectionNode) {
        collectFromPureAtom(projectionNode.getHeadAtom());
    }

    @Override
    public void visit(UnionNode unionNode) {
        // Collects nothing
    }

    private void collectFromAtom(FunctionFreeDataAtom atom) {
        for (NonFunctionalTerm term : atom.getNonFunctionalTerms()) {
            if (term instanceof Variable)
                collectedVariables.add((Variable)term);
        }
    }

    private void collectFromPureAtom(PureDataAtom atom) {
        collectedVariables.addAll(atom.getVariableTerms());
    }
}
