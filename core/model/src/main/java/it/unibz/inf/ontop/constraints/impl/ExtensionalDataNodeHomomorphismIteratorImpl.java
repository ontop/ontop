package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import java.util.*;

public class ExtensionalDataNodeHomomorphismIteratorImpl implements Iterator<Homomorphism> {
    private final ListIterator<ExtensionalDataNode> iterator;
    private final Deque<State> stack; // the current state is at the top

    private boolean movedToNext;
    private Homomorphism next; // null means reached the end

    private final ImmutableCollection<ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode> to;

    public ExtensionalDataNodeHomomorphismIteratorImpl(Homomorphism baseHomomorphism, ImmutableList<ExtensionalDataNode> from, ImmutableCollection<ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode> to) {
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
        final Queue<ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode> remainingChoices;
        final ExtensionalDataNode atom;

        State(ExtensionalDataNode atom, Homomorphism homomorphism) {
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
            System.out.println("  STATE: " + state.atom);
            ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode candidateAtom = state.remainingChoices.poll();
            if (candidateAtom != null) {
                System.out.println("  CANDIDATE: " + candidateAtom);
                if (state.atom.getRelationDefinition().equals(candidateAtom.getRelationDefinition())) {
                    Homomorphism.Builder builder = state.homomorphism.builder();
                    for (Map.Entry<Integer, ? extends VariableOrGroundTerm> e : state.atom.getArgumentMap().entrySet()) {
                        VariableOrGroundTerm to = candidateAtom.getArgument(e.getKey());
                        builder.extend(e.getValue(), to);
                        System.out.println("     EXTENDS: " + e.getValue() + " -> " + to + " " + builder.isValid());
                        if (!builder.isValid())
                            break;
                    }
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
}


