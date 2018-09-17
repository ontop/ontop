package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.*;

public class ImmutableHomomorphismUtilities {

    private final SubstitutionFactory substitutionFactory;

    @Inject
    public ImmutableHomomorphismUtilities(SubstitutionFactory substitutionFactory) {
        this.substitutionFactory = substitutionFactory;
    }

    private boolean extendHomomorphism(Map<Variable, VariableOrGroundTerm> map, DataAtom from, DataAtom to) {

        int arity = from.getArity();
        if ((arity != to.getArity()) || !from.getPredicate().equals(to.getPredicate()))
            return false;

        for (int i = 0; i < arity; i++) {
            boolean result = extendHomomorphism(map, from.getTerm(i), to.getTerm(i));
            // if we cannot find a match, terminate the process and return false
            if (!result)
                return false;
        }
        return true;
    }

    public boolean extendHomomorphism(Map<Variable, VariableOrGroundTerm> map, ImmutableList<? extends VariableOrGroundTerm> from, ImmutableList<? extends VariableOrGroundTerm> to) {
        int arity = from.size();
        for (int i = 0; i < arity; i++) {
            boolean result = extendHomomorphism(map, from.get(i), to.get(i));
            // if we cannot find a match, terminate the process and return false
            if (!result)
                return false;
        }
        return true;
    }

    public boolean extendHomomorphism(Map<Variable, VariableOrGroundTerm> map, VariableOrGroundTerm from, VariableOrGroundTerm to) {
        if (from instanceof Variable) {
            // ignore if the substitution already has the same value
            if (from.equals(to))
                return true;

            VariableOrGroundTerm t = map.get(from);
            // add if there is no value yet
            if (t == null) {
                map.put((Variable) from, to);
                return true;
            }
            // otherwise
            return false;
        }
        else if (from instanceof Constant) {
            // constants must match
            if (!from.equals(to))
                return false;
        }
        else /*if (from instanceof GroundFunctionalTerm)*/ {
            // the to term must also be a function
            if (!(to instanceof GroundFunctionalTerm))
                return false;

            GroundFunctionalTerm fromF = (GroundFunctionalTerm)from;
            GroundFunctionalTerm toF = (GroundFunctionalTerm)to;
            if ((fromF.getArity() != toF.getArity()) ||
                    !(fromF.getFunctionSymbol().equals(toF.getFunctionSymbol())))
                return false;

            return extendHomomorphism(map, fromF.getTerms(), toF.getTerms());
        }
        return true;
    }

    /**
     * Extends a given substitution that maps each atom in {@code from} to match at least one atom in {@code to}
     *
     * @param map
     * @param from
     * @param to
     * @return
     */

    public Optional<ImmutableSubstitution> computeSomeHomomorphism(Map<Variable, VariableOrGroundTerm> map, ImmutableList<DataAtom> from, ImmutableMultimap<AtomPredicate, DataAtom> to) {

        int fromSize = from.size();
        if (fromSize == 0)
            return Optional.of(substitutionFactory.getSubstitution(ImmutableMap.copyOf(map)));

        Map<Variable, VariableOrGroundTerm> currentSubstitution = map;

        // stack of partial homomorphisms
        Stack<Map<Variable, VariableOrGroundTerm>> sbStack = new Stack<>();
        sbStack.push(currentSubstitution);
        // set the capacity to reduce memory re-allocations
        List<Stack<DataAtom>> choicesMap = new ArrayList<>(fromSize);

        int currentAtomIdx = 0;
        while (currentAtomIdx >= 0) {
            DataAtom currentAtom = from.get(currentAtomIdx);

            Stack<DataAtom> choices;
            if (currentAtomIdx >= choicesMap.size()) {
                // we have never reached this atom (this is lazy initialization)
                // initializing the stack
                choices = new Stack<>();
                // add all choices for the current predicate symbol
                choices.addAll(to.get(currentAtom.getPredicate()));
                choicesMap.add(currentAtomIdx, choices);
            }
            else
                choices = choicesMap.get(currentAtomIdx);

            boolean choiceMade = false;
            while (!choices.isEmpty()) {
                Map<Variable, VariableOrGroundTerm> s1 = new HashMap<>(currentSubstitution); // clone!
                choiceMade = extendHomomorphism(s1, currentAtom, choices.pop());
                if (choiceMade) {
                    // we reached the last atom
                    if (currentAtomIdx == fromSize - 1)
                        return Optional.of(substitutionFactory.getSubstitution(ImmutableMap.copyOf(s1)));

                    // otherwise, save the partial homomorphism
                    sbStack.push(currentSubstitution);
                    currentSubstitution = s1;
                    currentAtomIdx++;  // move to the next atom
                    break;
                }
            }
            if (!choiceMade) {
                // backtracking
                // restore all choices for the current predicate symbol
                choices.addAll(to.get(currentAtom.getPredicate()));
                currentSubstitution = sbStack.pop();   // restore the partial homomorphism
                currentAtomIdx--;   // move to the previous atom
            }
        }

        // checked all possible substitutions and have not found anything
        return Optional.empty();
    }

}
