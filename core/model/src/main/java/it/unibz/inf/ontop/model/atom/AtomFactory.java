package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface AtomFactory {

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
}
