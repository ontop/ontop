package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class LinearInclusionDependencies<P extends AtomPredicate> {

    protected final AtomFactory atomFactory;

    public static final class LinearInclusionDependency<P extends AtomPredicate> {
        private final DataAtom<P> head, body;

        private LinearInclusionDependency(DataAtom<P> head, DataAtom<P> body) {
            this.head = head;
            this.body = body;
        }

        public DataAtom<P> getHead() { return head; }

        public DataAtom<P> getBody() { return body; }

        @Override
        public String toString() { return head + " :- " + body; }
    }

    protected final ImmutableList<LinearInclusionDependency<P>> dependencies;
    private final VariableGenerator variableGenerator;

    protected LinearInclusionDependencies(CoreUtilsFactory coreUtilsFactory,
                                          AtomFactory atomFactory,
                                          ImmutableList<LinearInclusionDependency<P>> dependencies) {
        this.atomFactory = atomFactory;
        this.dependencies = dependencies;
        this.variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
    }

    public static <P extends AtomPredicate> Builder<P> builder(CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory) {
        return new Builder<>(coreUtilsFactory, atomFactory);
    }

    /**
     * Chases a given atom with the linear inclusions dependencies
     *
     * IMPORTANT: each dependency is applied only ONCE to the atom
     *
     * @param atom to be chased
     * @return set of atoms
     */

    public ImmutableSet<DataAtom<P>> chaseAtom(DataAtom<P> atom) {
        registerVariables(atom);
        return Stream.concat(
                    Stream.of(atom),
                    dependencies.stream()
                        .map(id -> chase(id, atom))
                        .filter(Optional::isPresent)
                        .map(Optional::get))
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

    public ImmutableSet<DataAtom<P>> chaseAllAtoms(ImmutableCollection<DataAtom<P>> atoms) {
        registerVariables(atoms);
        return Stream.concat(
                atoms.stream(),
                atoms.stream()
                        .flatMap(a -> dependencies.stream()
                                        .map(id -> chase(id, a))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)))
                .collect(ImmutableCollectors.toSet());
    }

    private Optional<DataAtom<P>> chase(LinearInclusionDependency<P> id, DataAtom<P> atom) {
        if (!id.getBody().getPredicate().equals(atom.getPredicate()))
            return Optional.empty();

        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder();
        if (!builder.extend(id.getBody().getArguments(), atom.getArguments()).isValid())
            return Optional.empty();

        ImmutableHomomorphism h = extendWithLabelledNulls(id, builder.build());
        ImmutableList<VariableOrGroundTerm> newArguments = id.getHead().getArguments().stream()
                .map(h::apply)
                .collect(ImmutableCollectors.toList());

        return Optional.of(atomFactory.getDataAtom(id.getHead().getPredicate(), newArguments));
    }

    protected void registerVariables(DataAtom<P> atom) {
        variableGenerator.registerAdditionalVariables(atom.getVariables());
    }

    protected void registerVariables(ImmutableCollection<DataAtom<P>> atoms) {
        atoms.forEach(this::registerVariables);
    }

    protected ImmutableHomomorphism extendWithLabelledNulls(LinearInclusionDependency<P> id, ImmutableHomomorphism h) {
        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder(h);
        ImmutableSet<Variable> bodyVariables = id.getBody().getVariables();
        id.getHead().getVariables().stream()
                .filter(v -> !bodyVariables.contains(v))
                .forEach(v -> builder.extend(v, variableGenerator.generateNewVariableFromVar(v)));
        return builder.build();
    }

    @Override
    public String toString() {
        return dependencies.toString();
    }



    public static class Builder<P extends AtomPredicate> {
        protected final ImmutableList.Builder<LinearInclusionDependency<P>> builder = ImmutableList.builder();

        protected final CoreUtilsFactory coreUtilsFactory;
        protected final AtomFactory atomFactory;

        protected Builder(CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory) {
            this.coreUtilsFactory = coreUtilsFactory;
            this.atomFactory = atomFactory;
        }

        public Builder<P> add(DataAtom<P> head, DataAtom<P> body) {
            builder.add(new LinearInclusionDependency<>(head, body));
            return this;
        }

        public LinearInclusionDependencies<P> build() {
            return new LinearInclusionDependencies<>(coreUtilsFactory, atomFactory, builder.build());
        }
    }
}
