package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphism;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

public class LinearInclusionDependenciesImpl<P extends AtomPredicate> extends BasicLinearInclusionDependenciesImpl<P> {

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

    protected LinearInclusionDependenciesImpl(CoreUtilsFactory coreUtilsFactory,
                                              AtomFactory atomFactory,
                                              ImmutableList<LinearInclusionDependency<P>> dependencies) {
        this.atomFactory = atomFactory;
        this.dependencies = dependencies;
        this.variableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
    }

    public static <P extends AtomPredicate> Builder<P> builder(CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory) {
        return new Builder<>(coreUtilsFactory, atomFactory);
    }

    @Override
    protected Stream<DataAtom<P>> chase(DataAtom<P> atom) {
        return dependencies.stream().flatMap(id -> chase(id, atom));
    }

    private Stream<DataAtom<P>> chase(LinearInclusionDependency<P> id, DataAtom<P> atom) {
        if (!id.getBody().getPredicate().equals(atom.getPredicate()))
            return Stream.empty();

        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder();
        if (!builder.extend(id.getBody().getArguments(), atom.getArguments()).isValid())
            return Stream.empty();

        ImmutableHomomorphism h = extendWithLabelledNulls(id, builder.build());
        ImmutableList<VariableOrGroundTerm> newArguments = id.getHead().getArguments().stream()
                .map(h::apply)
                .collect(ImmutableCollectors.toList());

        return Stream.of(atomFactory.getDataAtom(id.getHead().getPredicate(), newArguments));
    }

    @Override
    protected void registerVariables(DataAtom<P> atom) {
        variableGenerator.registerAdditionalVariables(atom.getVariables());
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

        public BasicLinearInclusionDependenciesImpl<P> build() {
            return new LinearInclusionDependenciesImpl<>(coreUtilsFactory, atomFactory, builder.build());
        }
    }

}
