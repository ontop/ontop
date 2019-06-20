package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 *    these dependencies are full (that is, contain no existentially quantified variables)
 * @param <P>
 */

public class FullLinearInclusionDependencies<P extends AtomPredicate> extends LinearInclusionDependencies<P> {

    private final AtomFactory atomFactory;

    private FullLinearInclusionDependencies(ImmutableUnificationTools immutableUnificationTools,
                                        CoreUtilsFactory coreUtilsFactory,
                                        SubstitutionFactory substitutionFactory,
                                        AtomFactory atomFactory,
                                        ImmutableList<LinearInclusionDependency<P>> dependencies) {
        super(immutableUnificationTools, coreUtilsFactory, substitutionFactory, dependencies);
        this.atomFactory = atomFactory;
    }

    public static Builder builder(ImmutableUnificationTools immutableUnificationTools,
                                  CoreUtilsFactory coreUtilsFactory,
                                  SubstitutionFactory substitutionFactory,
                                  AtomFactory atomFactory) {
        return new Builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory, atomFactory);
    }

    /**
     * @param atom
     * @return
     */
    @Override
    public ImmutableSet<DataAtom<P>> chaseAtom(DataAtom<P> atom) {
        return dependencies.stream()
                .filter(id -> id.getBody().getPredicate().equals(atom.getPredicate()))
                .map(id -> apply(id, atom))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    private Optional<DataAtom<P>> apply(LinearInclusionDependency<P> id, DataAtom<P> atom) {
        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder().extend(id.getBody().getArguments(), atom.getArguments());
        if (!builder.isValid())
            return Optional.empty();

        ImmutableHomomorphism h = builder.build();
        ImmutableList<VariableOrGroundTerm> newArguments = id.getHead().getArguments().stream()
                .map(t -> h.apply(t))
                .collect(ImmutableCollectors.toList());

        return Optional.of(atomFactory.getDataAtom(atom.getPredicate(), newArguments));
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

        private final AtomFactory atomFactory;

        protected Builder(ImmutableUnificationTools immutableUnificationTools,
                          CoreUtilsFactory coreUtilsFactory,
                          SubstitutionFactory substitutionFactory,
                          AtomFactory atomFactory) {
            super(immutableUnificationTools, coreUtilsFactory, substitutionFactory);
            this.atomFactory = atomFactory;
        }

        public Builder add(DataAtom<P> head, DataAtom<P> body) {
            if (!body.getVariables().containsAll(head.getVariables()))
                throw new IllegalArgumentException();
            super.add(head, body);
            return this;
        }

        public FullLinearInclusionDependencies build() {
            return new FullLinearInclusionDependencies(immutableUnificationTools, coreUtilsFactory, substitutionFactory, atomFactory, builder.build());
        }
    }

}
