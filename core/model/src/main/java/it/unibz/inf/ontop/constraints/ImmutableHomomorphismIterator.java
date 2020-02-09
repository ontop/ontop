package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

import java.util.*;

public class ImmutableHomomorphismIterator<P extends AtomPredicate> implements Iterator<ImmutableHomomorphism> {
    private final ListIterator<DataAtom<P>> iterator;
    private final Deque<State> stack; // the current state is at the top

    private boolean movedToNext;
    private ImmutableHomomorphism next; // null means reached the end

    private final ImmutableCollection<DataAtom<P>> to;

    public ImmutableHomomorphismIterator(ImmutableHomomorphism baseHomomorphism, ImmutableList<DataAtom<P>> from, ImmutableCollection<DataAtom<P>> to) {
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
        final ImmutableHomomorphism homomorphism;
        final Queue<DataAtom<P>> remainingChoices;
        final DataAtom<P> atom;

        State(DataAtom<P> atom, ImmutableHomomorphism homomorphism) {
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
    public ImmutableHomomorphism next() {
        if (!hasNext())
            throw new NoSuchElementException();
        movedToNext = false;
        return next;
    }

    private ImmutableHomomorphism shift() {
        while (!stack.isEmpty()) {
            State state = stack.peek();
            DataAtom<P> candidateAtom = state.remainingChoices.poll();
            if (candidateAtom != null) {
                if (state.atom.getPredicate().equals(candidateAtom.getPredicate())) {
                    ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder(state.homomorphism);
                    if (builder.extend(state.atom.getArguments(), candidateAtom.getArguments()).isValid()) {
                        //stack.push(state); // save the state for the next iteration

                        ImmutableHomomorphism homomorphism = builder.build();
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
