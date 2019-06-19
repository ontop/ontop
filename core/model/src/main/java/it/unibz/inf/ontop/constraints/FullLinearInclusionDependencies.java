package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
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
                                        ImmutableList<LinearInclusionDependency<P>> dependencies) {
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
    public ImmutableSet<DataAtom<P>> chaseAtom(DataAtom<P> atom) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> (DataAtom<P>)theta.applyToDataAtom(dependency.getHead())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<DataAtom<P>> chaseAllAtoms(ImmutableCollection<DataAtom<P>> atoms) {
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

        public Builder add(DataAtom<P> head, DataAtom<P> body) {
            if (!body.getVariables().containsAll(head.getVariables()))
                throw new IllegalArgumentException();
            super.add(head, body);
            return this;
        }

        public FullLinearInclusionDependencies build() {
            return new FullLinearInclusionDependencies(immutableUnificationTools, coreUtilsFactory, substitutionFactory, builder.build());
        }
    }

}
