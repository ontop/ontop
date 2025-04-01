package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.NodeInGraphPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.ObjectRDFType;

import java.util.Optional;

public class NodeInGraphPredicateImpl extends AtomPredicateImpl
        implements NodeInGraphPredicate {

    private final boolean isInDefaultGraph;

    protected NodeInGraphPredicateImpl(ObjectRDFType rdfObjectType, boolean isInDefaultGraph) {
        super(isInDefaultGraph
                        ? "nodeInDefaultGraph"
                        : "nodeInGraph",
                isInDefaultGraph
                        ? ImmutableList.of(rdfObjectType)
                        : ImmutableList.of(rdfObjectType, rdfObjectType));
        this.isInDefaultGraph = isInDefaultGraph;
    }

    @Override
    public <T extends ImmutableTerm> T getNode(ImmutableList<T> atomArguments) {
        return atomArguments.get(0);
    }

    @Override
    public <T extends ImmutableTerm> Optional<T> getGraph(ImmutableList<T> atomArguments) {
        return isInDefaultGraph
                ? Optional.empty()
                : Optional.of(atomArguments.get(1));
    }

    @Override
    public boolean isInDefaultGraph() {
        return isInDefaultGraph;
    }
}
