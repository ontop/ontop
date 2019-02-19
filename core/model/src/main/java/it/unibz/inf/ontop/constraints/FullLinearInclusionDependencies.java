package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 *    assumes that the dependencies are full (that is, contain no existentially quantified variables)
 * @param <P>
 */

public class FullLinearInclusionDependencies<P extends AtomPredicate> extends LinearInclusionDependencies<P> {

    private FullLinearInclusionDependencies(ImmutableUnificationTools immutableUnificationTools,
                                        CoreUtilsFactory coreUtilsFactory,
                                        SubstitutionFactory substitutionFactory,
                                        ImmutableList<LinearInclusionDependency> dependencies) {
        super(immutableUnificationTools, coreUtilsFactory, substitutionFactory, dependencies);
    }

    public static Builder builder(ImmutableUnificationTools immutableUnificationTools,
                                  CoreUtilsFactory coreUtilsFactory,
                                  SubstitutionFactory substitutionFactory) {
        return new Builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory);
    }

    /**
     * @param atom
     * @return
     */
    @Override
    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> theta.applyToDataAtom(dependency.getHead())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<DataAtom> chaseAllAtoms(ImmutableList<DataAtom> atoms) {
        return Stream.concat(
                atoms.stream(),
                atoms.stream()
                        .flatMap(a -> chaseAtom(a).stream()))
                .collect(ImmutableCollectors.toSet());
    }

    public static class Builder<P extends AtomPredicate> extends LinearInclusionDependencies.Builder<P> {

        protected Builder(ImmutableUnificationTools immutableUnificationTools,
                          CoreUtilsFactory coreUtilsFactory,
                          SubstitutionFactory substitutionFactory) {
            super(immutableUnificationTools, coreUtilsFactory, substitutionFactory);
        }

        public LinearInclusionDependencies.Builder add(DataAtom<P> head, DataAtom<P> body) {
            if (!head.getVariables().containsAll(body.getVariables()))
                throw new IllegalArgumentException();
            return super.add(head, body);
        }

        public LinearInclusionDependencies build() {
            return new FullLinearInclusionDependencies(immutableUnificationTools, coreUtilsFactory, substitutionFactory, builder.build());
        }
    }

}
