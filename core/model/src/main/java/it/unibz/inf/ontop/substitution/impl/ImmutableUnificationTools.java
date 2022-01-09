package it.unibz.inf.ontop.substitution.impl;

import java.util.Optional;

import com.google.common.collect.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    public ImmutableUnificationTools(TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
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
        ImmutableMap<Variable, ImmutableTerm> sub = unify(ImmutableMap.of(), args1, args2);
        if (sub == null)
            return Optional.empty();

        return Optional.of(substitutionFactory.getSubstitution((ImmutableMap<Variable, T>)sub));
    }

    public Optional<ArgumentMapUnification> computeArgumentMapMGU(
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
                .map(u -> (ImmutableSubstitution<NonFunctionalTerm>)(ImmutableSubstitution<?>)u);
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

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution1.getImmutableMap().entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution2.getImmutableMap().entrySet()) {
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
                .map(substitution -> substitution.transform(GroundTermTools::convertIntoVariableOrGroundTerm));
    }



    public static class ArgumentMapUnification {
        public final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        public ArgumentMapUnification(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                      ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.argumentMap = argumentMap;
            this.substitution = substitution;
        }
    }




    private static boolean variableOccursInTerm(Variable v, ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm)
            return ((ImmutableFunctionalTerm)term).getTerms().stream()
                    .anyMatch(t -> variableOccursInTerm(v, t));
        return v.equals(term);
    }

    /**
     * Recursive
     */
    private ImmutableTerm apply(ImmutableTerm t, ImmutableMap<Variable, ImmutableTerm> sub) {
        if (sub.isEmpty())
            return t;

        if (t instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm)t;
            ImmutableList<ImmutableTerm> terms = f.getTerms().stream()
                    .map(tt -> apply(tt, sub))
                    .collect(ImmutableCollectors.toList());
            return termFactory.getImmutableFunctionalTerm(f.getFunctionSymbol(), terms);
        }
        return sub.getOrDefault(t, t);
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

    private ImmutableMap<Variable, ImmutableTerm> unify(ImmutableMap<Variable, ImmutableTerm> sub, ImmutableList<? extends ImmutableTerm> args1, ImmutableList<? extends ImmutableTerm> args2) {
        if (args1.size() != args2.size())
            return null;

        int arity = args1.size();
        for (int i = 0; i < arity; i++) {
            // applying the computed substitution first
            ImmutableTerm term1 = apply(args1.get(i), sub);
            ImmutableTerm term2 = apply(args2.get(i), sub);

            if (term1.equals(term2))
                continue;

            // Special case: unification of two functional terms (possibly recursive)
            if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                if (!f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                    return null;

                sub = unify(sub, f1.getTerms(), f2.getTerms());
                if (sub == null)
                    return null;
            }
            else {
                ImmutableMap<Variable, ImmutableTerm> s;
                // avoid unifying x with f(g(x))
                if (term1 instanceof Variable && !variableOccursInTerm((Variable) term1, term2))
                    s = ImmutableMap.of((Variable) term1, term2);
                else if (term2 instanceof Variable && !variableOccursInTerm((Variable) term2, term1))
                    s = ImmutableMap.of((Variable) term2, term1);
                else
                    return null; // neither is a variable, impossible to unify distinct terms

                sub = Stream.concat(s.entrySet().stream(), sub.entrySet().stream()
                                .map(e -> Maps.immutableEntry(e.getKey(), apply(e.getValue(), s)))
                                // The substitution for the current variable has become
                                // trivial, e.g., x/x with the current composition. We
                                // remove it to keep only a non-trivial unifier
                                .filter(e -> !e.getValue().equals(e.getKey())))
                        .collect(ImmutableCollectors.toMap());
            }
        }
        return sub;
    }

}
