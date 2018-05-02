package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.Context;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

public class QuadPredicateImpl extends RDFAtomPredicateImpl implements QuadPredicate {

    private static int NAMED_GRAPH_INDEX = 3;

    protected QuadPredicateImpl(ImmutableList<TermType> expectedBaseTypes) {
        super("triple", 4, expectedBaseTypes,0, 1, 2);
    }

    @Override
    public Optional<Context> getContext(ImmutableList<? extends ImmutableTerm> atomArguments) {
        return extractIRI(atomArguments.get(NAMED_GRAPH_INDEX))
                .map(SimpleNamedGraph::new);
    }

    @Override
    public <T extends ImmutableTerm> ImmutableList<T> updateSPO(ImmutableList<T> originalArguments, T newSubject, T newProperty, T newObject) {
        return ImmutableList.of(newSubject, newProperty, newObject, originalArguments.get(NAMED_GRAPH_INDEX));
    }
}
