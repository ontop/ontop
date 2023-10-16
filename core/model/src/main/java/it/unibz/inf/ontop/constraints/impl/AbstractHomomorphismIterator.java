package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.Homomorphism;

import java.util.*;

public abstract class AbstractHomomorphismIterator<S, T> implements Iterator<Homomorphism> {

    private final ListIterator<S> iterator;
    private final Deque<State> stack; // the current state is at the top

    private boolean movedToNext;
    private Homomorphism next; // null means reached the end

    private final ImmutableCollection<T> to;

    public AbstractHomomorphismIterator(Homomorphism baseHomomorphism, ImmutableList<S> from, ImmutableCollection<T> to) {
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
        final Queue<T> remainingChoices;
        final S atom;

        State(S atom, Homomorphism homomorphism) {
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
            T candidateAtom = state.remainingChoices.poll();
            if (candidateAtom != null) {
                if (equalPredicates(state.atom, candidateAtom)) {
                    Homomorphism.Builder builder = state.homomorphism.builder();
                    extendHomomorphism(builder, state.atom, candidateAtom);
                    if (builder.isValid()) {
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

    abstract boolean equalPredicates(S s, T t);

    abstract void extendHomomorphism(Homomorphism.Builder builder, S s, T t);
}
