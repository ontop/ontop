package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.constraints.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

import java.util.*;

public class ImmutableCQContainmentCheckUnderLIDs<P extends AtomPredicate> implements ImmutableCQContainmentCheck<P> {

    private final Map<ImmutableCollection<DataAtom<P>>, ImmutableCollection<DataAtom<P>>> chaseCache = new HashMap<>();

    private final HomomorphismFactory homomorphismFactory;
    private final LinearInclusionDependencies<P> dependencies;

    ImmutableCQContainmentCheckUnderLIDs(HomomorphismFactory homomorphismFactory, LinearInclusionDependencies<P> dependencies) {
        this.homomorphismFactory = homomorphismFactory;
        this.dependencies = dependencies;
    }

    @Override
    public boolean isContainedIn(ImmutableCQ<P> cq1, ImmutableCQ<P> cq2) {
        Homomorphism.Builder builder = homomorphismFactory.getHomomorphismBuilder();
        // get the substitution for the answer variables first
        // this will ensure that all answer variables are mapped either to constants or
        //       to answer variables in the base (but not to the labelled nulls generated by the chase)
        if (!cq1.getAnswerVariables().equals(cq2.getAnswerVariables()))
            return false;

        cq1.getAnswerVariables().forEach(v -> builder.extend(cq2.getAnswerTerm(v), cq1.getAnswerTerm(v)));

        if (builder.isValid()) {
            Iterator<Homomorphism> iterator = homomorphismFactory.getHomomorphismIterator(
                    builder.build(),
                    cq2.getAtoms(),
                    chaseAllAtoms(cq1.getAtoms()));
            return iterator.hasNext();
        }
        return false;
    }

    @Override
    public ImmutableCollection<DataAtom<P>> chaseAllAtoms(ImmutableCollection<DataAtom<P>> dataAtoms) {
        return chaseCache.computeIfAbsent(dataAtoms, dependencies::chaseAllAtoms);
    }
}
