package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class LinearInclusionDependencies<P extends AtomPredicate> {

    protected final ImmutableUnificationTools immutableUnificationTools;
    protected final CoreUtilsFactory coreUtilsFactory;
    protected final SubstitutionFactory substitutionFactory;
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

    protected LinearInclusionDependencies(ImmutableUnificationTools immutableUnificationTools,
                                          CoreUtilsFactory coreUtilsFactory,
                                          SubstitutionFactory substitutionFactory,
                                          AtomFactory atomFactory, ImmutableList<LinearInclusionDependency<P>> dependencies) {
        this.immutableUnificationTools = immutableUnificationTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.dependencies = dependencies;
        this.variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
    }

    public static Builder builder(ImmutableUnificationTools immutableUnificationTools,
                                  CoreUtilsFactory coreUtilsFactory,
                                  SubstitutionFactory substitutionFactory,
                                  AtomFactory atomFactory) {
        return new Builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory, atomFactory);
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

    public ImmutableSet<DataAtom<P>> chaseAtom(DataAtom<P> atom) {
        variableGenerator.registerAdditionalVariables(atom.getVariables());

        return dependencies.stream()
                .map(id -> chase(id, atom))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    protected Optional<DataAtom<P>> chase(LinearInclusionDependency<P> id, DataAtom<P> atom) {
        if (!id.getBody().getPredicate().equals(atom.getPredicate()))
            return Optional.empty();

        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder()
                .extend(id.getBody().getArguments(), atom.getArguments());
        if (!builder.isValid())
            return Optional.empty();

        ImmutableHomomorphism h = extendWithLabelledNulls(id, builder.build());
        ImmutableList<VariableOrGroundTerm> newArguments = id.getHead().getArguments().stream()
                .map(t -> h.apply(t))
                .collect(ImmutableCollectors.toList());

        return Optional.of(atomFactory.getDataAtom(id.getHead().getPredicate(), newArguments));
    }


    protected ImmutableHomomorphism extendWithLabelledNulls(LinearInclusionDependency<P> id, ImmutableHomomorphism h) {
        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder(h);
        ImmutableSet<Variable> bodyVariables = id.getBody().getVariables();
        id.getHead().getVariables().stream()
                .filter(v -> !bodyVariables.contains(v))
                .forEach(v -> builder.extend(v, variableGenerator.generateNewVariableFromVar(v)));
        return builder.build();
    }

    public ImmutableSet<DataAtom<P>> chaseAllAtoms(ImmutableCollection<DataAtom<P>> atoms) {
        ImmutableSet<Variable> v = atoms.stream()
                .flatMap(a -> a.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
        variableGenerator.registerAdditionalVariables(v);

        return Stream.concat(
                atoms.stream(),
                atoms.stream()
                        .flatMap(a -> chaseAtom(a).stream()))
                .collect(ImmutableCollectors.toSet());
    }



    @Override
    public String toString() {
        return dependencies.toString();
    }



    public static class Builder<P extends AtomPredicate> {
        protected final ImmutableList.Builder<LinearInclusionDependency<P>> builder = ImmutableList.builder();

        protected final ImmutableUnificationTools immutableUnificationTools;
        protected final CoreUtilsFactory coreUtilsFactory;
        protected final SubstitutionFactory substitutionFactory;
        protected final AtomFactory atomFactory;

        protected Builder(ImmutableUnificationTools immutableUnificationTools,
                          CoreUtilsFactory coreUtilsFactory,
                          SubstitutionFactory substitutionFactory, AtomFactory atomFactory) {
            this.immutableUnificationTools = immutableUnificationTools;
            this.coreUtilsFactory = coreUtilsFactory;
            this.substitutionFactory = substitutionFactory;
            this.atomFactory = atomFactory;
        }

        public Builder add(DataAtom<P> head, DataAtom<P> body) {
            builder.add(new LinearInclusionDependency(head, body));
            return this;
        }

        public LinearInclusionDependencies<P> build() {
            return new LinearInclusionDependencies(immutableUnificationTools, coreUtilsFactory, substitutionFactory, atomFactory, builder.build());
        }
    }
}
