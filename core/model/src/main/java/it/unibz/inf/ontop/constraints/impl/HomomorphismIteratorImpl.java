package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

import java.util.*;

public class HomomorphismIteratorImpl<P extends AtomPredicate> extends AbstractHomomorphismIterator<DataAtom<P>, DataAtom<P>> {

    public HomomorphismIteratorImpl(Homomorphism baseHomomorphism, ImmutableList<DataAtom<P>> from, ImmutableCollection<DataAtom<P>> to) {
        super(baseHomomorphism, from, to);
    }
    @Override
    boolean equalPredicates(DataAtom<P> from, DataAtom<P> to) {
        return from.getPredicate().equals(to.getPredicate());
    }

    @Override
    void extendHomomorphism(Homomorphism.Builder builder, DataAtom<P> from, DataAtom<P> to) {
        builder.extend(from.getArguments(), to.getArguments());
    }
}
