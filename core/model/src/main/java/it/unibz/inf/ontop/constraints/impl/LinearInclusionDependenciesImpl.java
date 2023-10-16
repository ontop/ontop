package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

public class LinearInclusionDependenciesImpl<P extends AtomPredicate> extends AbstractLinearInclusionDependencies<P> {

    protected final AtomFactory atomFactory;
    protected final HomomorphismFactory homomorphismFactory;

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

    protected LinearInclusionDependenciesImpl(CoreUtilsFactory coreUtilsFactory,
                                              AtomFactory atomFactory,
                                              HomomorphismFactory homomorphismFactory,
                                              ImmutableList<LinearInclusionDependency<P>> dependencies) {
        this.atomFactory = atomFactory;
        this.homomorphismFactory = homomorphismFactory;
        this.dependencies = dependencies;
        this.variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
    }

    @Override
    protected Stream<DataAtom<P>> chase(DataAtom<P> atom) {
        return dependencies.stream().flatMap(id -> chase(id, atom));
    }

    private Stream<DataAtom<P>> chase(LinearInclusionDependency<P> id, DataAtom<P> atom) {
        if (!id.getBody().getPredicate().equals(atom.getPredicate()))
            return Stream.empty();

        Homomorphism.Builder builder = homomorphismFactory.getHomomorphismBuilder();
        if (!builder.extend(id.getBody().getArguments(), atom.getArguments()).isValid())
            return Stream.empty();

        Homomorphism h = extendWithLabelledNulls(id, builder.build());
        ImmutableList<VariableOrGroundTerm> newArguments = id.getHead().getArguments().stream()
                .map(h::apply)
                .collect(ImmutableCollectors.toList());

        return Stream.of(atomFactory.getDataAtom(id.getHead().getPredicate(), newArguments));
    }

    @Override
    protected void registerVariables(DataAtom<P> atom) {
        variableGenerator.registerAdditionalVariables(atom.getVariables());
    }

    protected Homomorphism extendWithLabelledNulls(LinearInclusionDependency<P> id, Homomorphism h) {
        Homomorphism.Builder builder = h.builder();
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



    static class Builder<P extends AtomPredicate> implements LinearInclusionDependencies.Builder<P> {
        protected final ImmutableList.Builder<LinearInclusionDependency<P>> builder = ImmutableList.builder();

        protected final CoreUtilsFactory coreUtilsFactory;
        protected final AtomFactory atomFactory;
        protected final HomomorphismFactory homomorphismFactory;

        protected Builder(CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory, HomomorphismFactory homomorphismFactory) {
            this.coreUtilsFactory = coreUtilsFactory;
            this.atomFactory = atomFactory;
            this.homomorphismFactory = homomorphismFactory;
        }

        @Override
        public Builder<P> add(DataAtom<P> head, DataAtom<P> body) {
            builder.add(new LinearInclusionDependency<>(head, body));
            return this;
        }

        @Override
        public LinearInclusionDependencies<P> build() {
            return new LinearInclusionDependenciesImpl<>(coreUtilsFactory, atomFactory, homomorphismFactory, builder.build());
        }
    }
}
