package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

public interface AtomFactory {

    AtomPredicate getAtomPredicate(String name, int arity);
    AtomPredicate getAtomPredicate(Predicate datalogPredicate);

    /**
     * Beware: a DataAtom is immutable
     */
    DataAtom getDataAtom(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> terms);

    /**
     * Beware: a DataAtom is immutable
     */
    DataAtom getDataAtom(AtomPredicate predicate, VariableOrGroundTerm... terms);

    DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate,
                                                                ImmutableList<? extends VariableOrGroundTerm> arguments);
    DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate, VariableOrGroundTerm ... arguments);

    DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate,
                                                                        ImmutableList<Variable> arguments);

    DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate,
                                                                        Variable ... arguments);

    VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, Variable... terms);

    VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> terms);

    Function getTripleAtom(Term subject, Term predicate, Term object);

    /**
     * TODO: create an abstraction of DataAtom (Atom) that accepts arbitrary ImmutableTerms as arguments
     * (not only Variable or ground terms)
     */
    ImmutableFunctionalTerm getImmutableTripleAtom(ImmutableTerm subject, ImmutableTerm predicate, ImmutableTerm object);

    ImmutableFunctionalTerm getImmutableQuadrupleAtom(ImmutableTerm subject, ImmutableTerm predicate, ImmutableTerm object);
}
