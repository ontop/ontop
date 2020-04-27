package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

public abstract class BasicLinearInclusionDependenciesImpl<P extends AtomPredicate> implements LinearInclusionDependencies<P> {

    /**
     * Chases a given atom with the linear inclusions dependencies
     *
     * IMPORTANT: each dependency is applied only ONCE to the atom
     *
     * @param atom to be chased
     * @return set of atoms
     */

    @Override
    public ImmutableSet<DataAtom<P>> chaseAtom(DataAtom<P> atom) {
        registerVariables(atom);
        return Stream.concat(Stream.of(atom), chase(atom))
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Chases given atoms with the linear inclusions dependencies
     *
     * IMPORTANT: each dependency is applied only ONCE to each atom
     *
     * @param atoms to be chased
     * @return set of atoms
     */

    @Override
    public ImmutableSet<DataAtom<P>> chaseAllAtoms(ImmutableCollection<DataAtom<P>> atoms) {
        registerVariables(atoms);
        return Stream.concat(
                atoms.stream(),
                atoms.stream().flatMap(this::chase))
                .collect(ImmutableCollectors.toSet());
    }

    protected abstract Stream<DataAtom<P>> chase(DataAtom<P> atom);

    protected abstract void registerVariables(DataAtom<P> atom);

    protected void registerVariables(ImmutableCollection<DataAtom<P>> atoms) {
        atoms.forEach(this::registerVariables);
    }
}
