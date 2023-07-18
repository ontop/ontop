package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

import java.util.*;

public class HomomorphismIteratorImpl<P extends AtomPredicate> implements Iterator<Homomorphism> {
    private final ListIterator<DataAtom<P>> iterator;
    private final Deque<State> stack; // the current state is at the top

    private boolean movedToNext;
    private Homomorphism next; // null means reached the end

    private final ImmutableCollection<DataAtom<P>> to;

    public HomomorphismIteratorImpl(Homomorphism baseHomomorphism, ImmutableList<DataAtom<P>> from, ImmutableCollection<DataAtom<P>> to) {
        this.iterator = from.listIterator();
        this.stack = new ArrayDeque<>(from.size());
        this.to = to;
        if (iterator.hasNext()) {
            movedToNext = false;
            stack.push(new State(iterator.next(), baseHomomorphism));
        }
        else {
            movedToNext = true;
            next = baseHomomorphism;
        }
    }


    private final class State {
        final Homomorphism homomorphism;
        final Queue<DataAtom<P>> remainingChoices;
        final DataAtom<P> atom;

        State(DataAtom<P> atom, Homomorphism homomorphism) {
            this.atom = atom;
            this.homomorphism = homomorphism;
            this.remainingChoices = new ArrayDeque<>(to);
        }
    }

    @Override
    public boolean hasNext() {
        if (!movedToNext) {
            next = shift();
            movedToNext = true;
        }
        return next != null;
    }

    @Override
    public Homomorphism next() {
        if (!hasNext())
            throw new NoSuchElementException();
        movedToNext = false;
        return next;
    }

    private Homomorphism shift() {
        while (!stack.isEmpty()) {
            State state = stack.peek();
            DataAtom<P> candidateAtom = state.remainingChoices.poll();
            if (candidateAtom != null) {
                if (state.atom.getPredicate().equals(candidateAtom.getPredicate())) {
                    Homomorphism.Builder builder = state.homomorphism.builder();
                    if (builder.extend(state.atom.getArguments(), candidateAtom.getArguments()).isValid()) {
                        //stack.push(state); // save the state for the next iteration

                        Homomorphism homomorphism = builder.build();
                        if (iterator.hasNext()) {
                            stack.push(new State(iterator.next(), homomorphism));
                        }
                        else { // reached the last atom
                            return homomorphism;
                        }
                    }
                }
            }
            else {  // backtracking: move back
                stack.pop();
                iterator.previous();
            }
        }
        return null; // checked all possible homomorphism candidates but found no match
    }
}
