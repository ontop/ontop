package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

public interface LinearInclusionDependencies<P extends AtomPredicate> {
    /**
     * Chases a given atom with the linear inclusions dependencies
     *
     * IMPORTANT: each dependency is applied only ONCE to the atom
     *
     * @param atom to be chased
     * @return set of atoms
     */

    ImmutableSet<DataAtom<P>> chaseAtom(DataAtom<P> atom);

    /**
     * Chases given atoms with the linear inclusions dependencies
     *
     * IMPORTANT: each dependency is applied only ONCE to each atom
     *
     * @param atoms to be chased
     * @return set of atoms
     */

    ImmutableSet<DataAtom<P>> chaseAllAtoms(ImmutableCollection<DataAtom<P>> atoms);
}
