package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChaseTools {

    private final ImmutableUnificationTools immutableUnificationTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;


    @Inject
    private ChaseTools(ImmutableUnificationTools immutableUnificationTools, CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory) {
        this.immutableUnificationTools = immutableUnificationTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
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

    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom, ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies, VariableGenerator variableGenerator) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> theta.applyToDataAtom(
                                freshLabelledNulls(dependency, variableGenerator)
                                        .applyToDataAtom(dependency.getHead()))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * assumes that the dependencies are full
     * @param atom
     * @param dependencies
     * @return
     */
    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom, ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> theta.applyToDataAtom(dependency.getHead())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    public ImmutableSet<DataAtom> chaseAllAtoms(ImmutableList<DataAtom> atoms, ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {
        Stream<Variable> s = atoms.stream().flatMap(a -> a.getVariables().stream());
        ImmutableSet<Variable> v = s.collect(ImmutableCollectors.toSet());
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(v);

        return Stream.concat(
                    atoms.stream(),
                    atoms.stream()
                            .flatMap(a -> chaseAtom(a, dependencies, variableGenerator).stream()))
                    .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSubstitution<VariableOrGroundTerm> freshLabelledNulls(ImmutableLinearInclusionDependency<AtomPredicate> dependency, VariableGenerator variableGenerator) {
        ImmutableSet<Variable> bodyVariables = dependency.getBody().getVariables();

        return substitutionFactory.getSubstitution(dependency.getHead().getVariables().stream()
                .filter(v -> !bodyVariables.contains(v))
                .collect(ImmutableCollectors.toMap(Function.identity(), variableGenerator::generateNewVariableFromVar)));
    }
}
