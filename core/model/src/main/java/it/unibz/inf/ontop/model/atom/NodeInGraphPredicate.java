package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import java.util.Optional;

public interface NodeInGraphPredicate extends AtomPredicate {

    <T extends ImmutableTerm> T getNode(ImmutableList<T> atomArguments);
    <T extends ImmutableTerm> Optional<T> getGraph(ImmutableList<T> atomArguments);

    boolean isInDefaultGraph();
}
