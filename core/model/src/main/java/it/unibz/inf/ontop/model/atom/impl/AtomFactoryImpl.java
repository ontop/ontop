package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.impl.GroundTermTools;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.atom.impl.DataAtomTools.areVariablesDistinct;
import static it.unibz.inf.ontop.model.atom.impl.DataAtomTools.isVariableOnly;


public class AtomFactoryImpl implements AtomFactory {

    private static final AtomFactory INSTANCE = new AtomFactoryImpl();

    private final AtomPredicate triplePredicate;

    private AtomFactoryImpl() {
        triplePredicate = getAtomPredicate("triple", 3);
    }

    public static AtomFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public AtomPredicate getAtomPredicate(String name, int arity) {
        return new AtomPredicateImpl(name, arity);
    }

    @Override
    public AtomPredicate getAtomPredicate(Predicate datalogPredicate) {
        return new AtomPredicateImpl(datalogPredicate);
    }

    @Override
    public DataAtom getDataAtom(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
        /**
         * NB: A GroundDataAtom is a DistinctVariableDataAtom
         */
        if(areVariablesDistinct(arguments)) {
            return getDistinctVariableDataAtom(predicate, arguments);
        }
        else if (isVariableOnly(arguments)) {
            return new VariableOnlyDataAtomImpl(predicate, (ImmutableList<Variable>)(ImmutableList<?>)arguments);
        }
        else {
            return new DataAtomImpl(predicate, arguments);
        }
    }

    @Override
    public DataAtom getDataAtom(AtomPredicate predicate, VariableOrGroundTerm... terms) {
        return getDataAtom(predicate, ImmutableList.copyOf(terms));
    }

    @Override
    public DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate,
                                                                ImmutableList<? extends VariableOrGroundTerm> arguments) {
        if (isVariableOnly(arguments)) {
            return new DistinctVariableOnlyDataAtomImpl(predicate, (ImmutableList<Variable>)(ImmutableList<?>)arguments);
        }
        else if (GroundTermTools.areGroundTerms(arguments)) {
            return new GroundDataAtomImpl(predicate, (ImmutableList<GroundTerm>)(ImmutableList<?>)arguments);
        }
        else {
            return new NonGroundDistinctVariableDataAtomImpl(predicate, arguments);
        }
    }

    @Override
    public DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate, VariableOrGroundTerm... arguments) {
        return getDistinctVariableDataAtom(predicate, ImmutableList.copyOf(arguments));
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> arguments) {
        return new DistinctVariableOnlyDataAtomImpl(predicate, arguments);
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate, Variable... arguments) {
        return getDistinctVariableOnlyDataAtom(predicate, ImmutableList.copyOf(arguments));
    }

    @Override
    public VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, Variable... arguments) {
        return getVariableOnlyDataAtom(predicate, ImmutableList.copyOf(arguments));
    }

    @Override
    public VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> arguments) {
        if (areVariablesDistinct(arguments)) {
            return new DistinctVariableOnlyDataAtomImpl(predicate, arguments);
        }
        else {
            return new VariableOnlyDataAtomImpl(predicate, arguments);
        }
    }

    @Override
    public Function getTripleAtom(Term subject, Term predicate, Term object) {
        return TERM_FACTORY.getFunction(triplePredicate, subject, predicate, object);
    }

    @Override
    public AtomPredicate getTripleAtomPredicate() {
        return triplePredicate;
    }


    @Override
    public ImmutableFunctionalTerm getImmutableTripleAtom(ImmutableTerm subject, ImmutableTerm predicate,
                                                          ImmutableTerm object) {
        return TERM_FACTORY.getImmutableFunctionalTerm(triplePredicate, subject, predicate, object);
    }
}
