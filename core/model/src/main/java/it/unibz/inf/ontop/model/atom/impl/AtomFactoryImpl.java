package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.impl.GroundTermTools;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.atom.impl.DataAtomTools.areVariablesDistinct;
import static it.unibz.inf.ontop.model.atom.impl.DataAtomTools.isVariableOnly;


public class AtomFactoryImpl implements AtomFactory {

    private final AtomPredicate triplePredicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final ObjectRDFType objectRDFType;

    @Inject
    private AtomFactoryImpl(TermFactory termFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        triplePredicate = getAtomPredicate("triple", 3);
        objectRDFType = typeFactory.getAbstractObjectRDFType();
    }

    @Override
    public AtomPredicate getAtomPredicate(String name, int arity) {
        ImmutableList<TermType> defaultBaseTypes = IntStream.range(0, arity).boxed()
                .map(i -> typeFactory.getAbstractRDFTermType())
                .collect(ImmutableCollectors.toList());
        return getAtomPredicate(name, defaultBaseTypes);
    }

    @Override
    public AtomPredicate getAtomPredicate(String name, ImmutableList<TermType> expectedBaseTypes) {
        return new AtomPredicateImpl(name, expectedBaseTypes.size(), expectedBaseTypes);
    }

    @Override
    public AtomPredicate getAtomPredicate(Predicate datalogPredicate) {
        return new AtomPredicateImpl(datalogPredicate);
    }

    @Override
    public AtomPredicate getObjectPropertyPredicate(String name) {
        return new AtomPredicateImpl(name, 2, ImmutableList.of(objectRDFType, objectRDFType));
    }

    @Override
    public AtomPredicate getObjectPropertyPredicate(IRI iri) {
        return getObjectPropertyPredicate(iri.getIRIString());
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
        return termFactory.getFunction(triplePredicate, subject, predicate, object);
    }

    @Override
    public AtomPredicate getTripleAtomPredicate() {
        return triplePredicate;
    }


    @Override
    public ImmutableFunctionalTerm getImmutableTripleAtom(ImmutableTerm subject, ImmutableTerm predicate,
                                                          ImmutableTerm object) {
        return termFactory.getImmutableFunctionalTerm(triplePredicate, subject, predicate, object);
    }
}
