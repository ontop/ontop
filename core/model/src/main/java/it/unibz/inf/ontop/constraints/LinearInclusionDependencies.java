package it.unibz.inf.ontop.constraints;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
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

    private final ImmutableUnificationTools immutableUnificationTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;

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

    private final ImmutableList<LinearInclusionDependency> dependencies;

    private LinearInclusionDependencies(ImmutableUnificationTools immutableUnificationTools,
                                        CoreUtilsFactory coreUtilsFactory,
                                        SubstitutionFactory substitutionFactory,
                                        ImmutableList<LinearInclusionDependency> dependencies) {
        this.immutableUnificationTools = immutableUnificationTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
        this.dependencies = dependencies;
    }

    public static Builder builder(ImmutableUnificationTools immutableUnificationTools,
                                  CoreUtilsFactory coreUtilsFactory,
                                  SubstitutionFactory substitutionFactory) {
        return new Builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory);
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

    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom, VariableGenerator variableGenerator) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> theta.applyToDataAtom(
                                freshLabelledNulls(dependency, variableGenerator)
                                        .applyToDataAtom(dependency.getHead()))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSubstitution<VariableOrGroundTerm> freshLabelledNulls(LinearInclusionDependency<P> dependency, VariableGenerator variableGenerator) {
        ImmutableSet<Variable> bodyVariables = dependency.getBody().getVariables();

        return substitutionFactory.getSubstitution(dependency.getHead().getVariables().stream()
                .filter(v -> !bodyVariables.contains(v))
                .collect(ImmutableCollectors.toMap(Function.identity(), variableGenerator::generateNewVariableFromVar)));
    }

    /**
     * assumes that the dependencies are full
     * @param atom
     * @return
     */
    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> theta.applyToDataAtom(dependency.getHead())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    public ImmutableSet<DataAtom> chaseAllAtoms(com.google.common.collect.ImmutableList<DataAtom> atoms) {
        Stream<Variable> s = atoms.stream().flatMap(a -> a.getVariables().stream());
        ImmutableSet<Variable> v = s.collect(ImmutableCollectors.toSet());
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(v);

        return Stream.concat(
                atoms.stream(),
                atoms.stream()
                        .flatMap(a -> chaseAtom(a, variableGenerator).stream()))
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSubstitution<VariableOrGroundTerm> freshLabelledNulls(ImmutableLinearInclusionDependency<AtomPredicate> dependency, VariableGenerator variableGenerator) {
        ImmutableSet<Variable> bodyVariables = dependency.getBody().getVariables();

        return substitutionFactory.getSubstitution(dependency.getHead().getVariables().stream()
                .filter(v -> !bodyVariables.contains(v))
                .collect(ImmutableCollectors.toMap(Function.identity(), variableGenerator::generateNewVariableFromVar)));
    }




    public static class Builder<P extends AtomPredicate> {
        private final ImmutableList.Builder<LinearInclusionDependency<P>> builder = ImmutableList.builder();

        private final ImmutableUnificationTools immutableUnificationTools;
        private final CoreUtilsFactory coreUtilsFactory;
        private final SubstitutionFactory substitutionFactory;

        private Builder(ImmutableUnificationTools immutableUnificationTools,
                        CoreUtilsFactory coreUtilsFactory,
                        SubstitutionFactory substitutionFactory) {
            this.immutableUnificationTools = immutableUnificationTools;
            this.coreUtilsFactory = coreUtilsFactory;
            this.substitutionFactory = substitutionFactory;
        }

        public Builder add(DataAtom<P> head, DataAtom<P> body) {
            builder.add(new LinearInclusionDependency(head, body));
            return this;
        }

        public LinearInclusionDependencies build() {
            return new LinearInclusionDependencies(immutableUnificationTools, coreUtilsFactory, substitutionFactory, builder.build());
        }
    }
}
