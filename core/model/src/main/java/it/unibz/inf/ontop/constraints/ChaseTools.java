package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class ChaseTools {

    private final ImmutableUnificationTools immutableUnificationTools;


    @Inject
    private ChaseTools(ImmutableUnificationTools immutableUnificationTools) {
        this.immutableUnificationTools = immutableUnificationTools;
    }

    /**
     * This method is used to chase foreign key constraint rule in which the rule
     * has only one atom in the body.
     *
     * IMPORTANT: each rule is applied only ONCE to the atom
     *
     * @param atom
     * @return set of atoms
     */

    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom, ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        // TODO: generate fresh labelled nulls
                        .map(theta -> theta.applyToDataAtom(dependency.getHead())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }


    public ImmutableSet<DataAtom> chaseAllAtoms(ImmutableList<DataAtom> atoms, Optional<?extends ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>>> dependencies) {
            return (!dependencies.isPresent()
                    ? atoms.stream()
                    : Stream.concat(atoms.stream(),
                    atoms.stream()
                            .flatMap(a -> chaseAtom(a, dependencies.get()).stream())))
                    .collect(ImmutableCollectors.toSet());
    }
}
