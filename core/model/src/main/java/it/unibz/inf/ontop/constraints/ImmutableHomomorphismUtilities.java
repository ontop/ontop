package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;

import java.util.*;

public class ImmutableHomomorphismUtilities {


    public static boolean extendHomomorphism(Map<Variable, VariableOrGroundTerm> map, ImmutableList<? extends VariableOrGroundTerm> from, ImmutableList<? extends VariableOrGroundTerm> to) {
        int arity = from.size();
        if (arity != to.size())
            return false;

        for (int i = 0; i < arity; i++) {
            // if we cannot find a match, terminate the process and return false
            if (!extendHomomorphism(map, from.get(i), to.get(i)))
                return false;
        }
        return true;
    }

    private static boolean extendHomomorphism(Map<Variable, VariableOrGroundTerm> map, VariableOrGroundTerm from, VariableOrGroundTerm to) {
        if (from instanceof Variable) {
            VariableOrGroundTerm t = map.put((Variable) from, to);
            if (t != null && !t.equals(to)) {
                // if there was a different value there
                return false;
            }
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
            if (!fromF.getFunctionSymbol().equals(toF.getFunctionSymbol()))
                return false;

            return extendHomomorphism(map, fromF.getTerms(), toF.getTerms());
        }
        return true;
    }


    private static final class State {
        final Map<Variable, VariableOrGroundTerm> homomorphism;
        final Queue<DataAtom> remainingAtomChoices;
        final DataAtom currentAtom;

        State(DataAtom currentAtom, Map<Variable, VariableOrGroundTerm> homomorphism, Collection<DataAtom> choices) {
            this.currentAtom = currentAtom;
            this.homomorphism = homomorphism;
            this.remainingAtomChoices = new ArrayDeque<>(choices);
        }
    }

    /**
     * Extends a given substitution that maps each atom in {@code from} to match at least one atom in {@code to}
     *
     * @param map
     * @param from
     * @param to
     * @return
     */

    public static boolean hasSomeHomomorphism(Map<Variable, VariableOrGroundTerm> map, ImmutableList<DataAtom> from, ImmutableSet<DataAtom> to) {

        ListIterator<DataAtom> iterator = from.listIterator();
        if (!iterator.hasNext())
            return true;

        State state = new State(iterator.next(), map, to);
        Deque<State> stack = new ArrayDeque<>();
        while (true) {
            DataAtom candidate = state.remainingAtomChoices.poll();
            if (candidate != null) {
                if (state.currentAtom.getPredicate().equals(candidate.getPredicate())) {
                    // create a copy because extendHomomorphism changes its first argument
                    Map<Variable, VariableOrGroundTerm> ext = new HashMap<>(state.homomorphism);
                    if (extendHomomorphism(ext, state.currentAtom.getArguments(), candidate.getArguments())) {
                        if (!iterator.hasNext())  // reached the last atom
                            return true;

                        // save the partial homomorphism for the next iteration
                        stack.push(state);
                        state = new State(iterator.next(), ext, to);
                    }

                }
            }
            else {
                if (stack.isEmpty())  // checked all possible substitutions and found no match
                    return false;

                // backtracking: restore the state and move back
                state = stack.pop();
                iterator.previous();
            }
        }
    }
}
