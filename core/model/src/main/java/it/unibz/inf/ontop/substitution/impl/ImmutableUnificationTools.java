package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.GroundTermTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/**
 * Tools for new-gen immutable unifying substitutions.
 */
@Singleton
public class ImmutableUnificationTools {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    public ImmutableUnificationTools(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Computes the Most General Unifier (MGU) for two n-ary atoms.
     *
     * @param args1
     * @param args2
     * @return the substitution corresponding to this unification.
     */

    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> computeMGU(ImmutableList<T> args1,
                                                                                   ImmutableList<T> args2) {
        return (Optional)unify(substitutionFactory.getSubstitution(), args1, args2);
    }

    private Optional<ArgumentMapUnification> computeArgumentMapMGU(
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap1,
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap2) {
        ImmutableSet<Integer> firstIndexes = argumentMap1.keySet();
        ImmutableSet<Integer> secondIndexes = argumentMap2.keySet();

        Sets.SetView<Integer> commonIndexes = Sets.intersection(firstIndexes, secondIndexes);

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> unifier = computeMGU(
                commonIndexes.stream()
                        .map(argumentMap1::get)
                        .collect(ImmutableCollectors.toList()),
                commonIndexes.stream()
                        .map(argumentMap2::get)
                        .collect(ImmutableCollectors.toList()));

        return unifier
                .map(u -> new ArgumentMapUnification(
                        // Merges the argument maps and applies the unifier
                        u.applyToArgumentMap(
                                Sets.union(firstIndexes, secondIndexes).stream()
                                        .collect(ImmutableCollectors.toMap(
                                                i -> i,
                                                i -> Optional.ofNullable((VariableOrGroundTerm) argumentMap1.get(i))
                                                .orElseGet(() -> argumentMap2.get(i))))),
                        u));

    }

    /**
     * TODO: make it replace computeMGUS()
     */
    public Optional<ImmutableSubstitution<NonFunctionalTerm>> computeMGUS2(ImmutableSubstitution<NonFunctionalTerm> s1,
                                                                           ImmutableSubstitution<NonFunctionalTerm> s2) {
        return computeMGUS(s1,s2)
                .map(u -> u.castTo(NonFunctionalTerm.class));
    }

    /**
     * Computes one Most General Unifier (MGU) of (two) substitutions.
     */
    public Optional<ImmutableSubstitution<ImmutableTerm>> computeMGUS(ImmutableSubstitution<? extends ImmutableTerm> substitution1,
                                                                      ImmutableSubstitution<? extends ImmutableTerm> substitution2) {
        if (substitution1.isEmpty())
            return Optional.of((ImmutableSubstitution<ImmutableTerm>)substitution2);
        else if (substitution2.isEmpty())
            return Optional.of((ImmutableSubstitution<ImmutableTerm>)substitution1);

        ImmutableList.Builder<ImmutableTerm> firstArgListBuilder = ImmutableList.builder();
        ImmutableList.Builder<ImmutableTerm> secondArgListBuilder = ImmutableList.builder();

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution1.entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution2.entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        ImmutableList<ImmutableTerm> firstArgList = firstArgListBuilder.build();
        ImmutableList<ImmutableTerm> secondArgList = secondArgListBuilder.build();

        return computeMGU(firstArgList, secondArgList);
    }

    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> computeAtomMGUS(
            ImmutableSubstitution<VariableOrGroundTerm> substitution1,
            ImmutableSubstitution<VariableOrGroundTerm> substitution2) {
        Optional<ImmutableSubstitution<ImmutableTerm>> optionalMGUS = computeMGUS(substitution1, substitution2);
        return optionalMGUS
                .map(s -> s.castTo(VariableOrGroundTerm.class));
    }



    public final class ArgumentMapUnification {
        public final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private ArgumentMapUnification(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                      ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.argumentMap = argumentMap;
            this.substitution = substitution;
        }

        private Optional<ImmutableUnificationTools.ArgumentMapUnification> unify(
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap) {

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> updatedArgumentMap =
                    substitution.applyToArgumentMap(newArgumentMap);

            return computeArgumentMapMGU(argumentMap, updatedArgumentMap)
                    .flatMap(u -> substitution.isEmpty()
                            ? Optional.of(u)
                            : computeAtomMGUS(substitution, u.substitution)
                            .map(s -> new ArgumentMapUnification(u.argumentMap, s)));
        }
    }

    public Optional<ImmutableSubstitution<VariableOrGroundTerm>> getSubstitutionUnifier(
            Stream<ImmutableSubstitution<VariableOrGroundTerm>> substitutions) {
        return substitutions
                .reduce(Optional.of(substitutionFactory.getSubstitution()),
                        (o, s) -> o.flatMap(s1 -> computeAtomMGUS(s1, s)),
                        (s1, s2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be run in parallel");
                        });
    }

    public Optional<ImmutableUnificationTools.ArgumentMapUnification> getArgumentMapUnifier(
            Stream<ImmutableMap<Integer, ? extends VariableOrGroundTerm>> arguments) {
        return arguments
                .reduce(Optional.of(new ArgumentMapUnification(ImmutableMap.of(), substitutionFactory.getSubstitution())),
                        (o, n) -> o.flatMap(u -> u.unify(n)),
                        (m1, m2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be run in parallel");
                        });
    }



    /**
     * Creates a unifier for args1 and args2
     *
     * The operation is as follows
     *
     * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
     *
     * @return true the substitution (of null if it does not)
     */

    private <T> Optional<ImmutableSubstitution<ImmutableTerm>> unify(ImmutableSubstitution<ImmutableTerm> sub, ImmutableList<? extends ImmutableTerm> args1, ImmutableList<? extends ImmutableTerm> args2) {
        if (args1.size() != args2.size())
            return Optional.empty();

        ImmutableSubstitution<ImmutableTerm> result = sub;
        int arity = args1.size();
        for (int i = 0; i < arity; i++) {
            // applying the computed substitution first
            ImmutableTerm term1 = result.apply(args1.get(i));
            ImmutableTerm term2 = result.apply(args2.get(i));

            if (term1.equals(term2))
                continue;

            // Special case: unification of two functional terms (possibly recursive)
            if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                if (!f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                    return Optional.empty();

                Optional<ImmutableSubstitution<ImmutableTerm>> resultForSubTerms = unify(result, f1.getTerms(), f2.getTerms());
                if (resultForSubTerms.isEmpty())
                    return Optional.empty();

                result = resultForSubTerms.get();
            }
            else {
                ImmutableSubstitution<ImmutableTerm> s;
                // avoid unifying x with f(g(x))
                if (term1 instanceof Variable && term2.getVariableStream().noneMatch(term1::equals))
                    s = substitutionFactory.getSubstitution((Variable) term1, term2);
                else if (term2 instanceof Variable && term1.getVariableStream().noneMatch(term2::equals))
                    s = substitutionFactory.getSubstitution((Variable) term2, term1);
                else
                    return Optional.empty(); // neither is a variable, impossible to unify distinct terms

                result = substitutionFactory.compose(s, result);
            }
        }
        return Optional.of(result);
    }

}
