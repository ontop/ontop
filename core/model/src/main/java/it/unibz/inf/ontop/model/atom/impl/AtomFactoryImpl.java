package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.GroundTermTools;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.atom.impl.DataAtomTools.areVariablesDistinct;
import static it.unibz.inf.ontop.model.atom.impl.DataAtomTools.isVariableOnly;


public class AtomFactoryImpl implements AtomFactory {

    private final TriplePredicate triplePredicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final IRIConstant rdfTypeConstant;

    @Inject
    private AtomFactoryImpl(TermFactory termFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        triplePredicate = new TriplePredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType()
        ));
        rdfTypeConstant = termFactory.getConstantIRI(RDF.TYPE);
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
        if (datalogPredicate instanceof AtomPredicate)
            return (AtomPredicate) datalogPredicate;
        return new AtomPredicateImpl(datalogPredicate);
    }

    @Override
    public <P extends AtomPredicate> DataAtom<P> getDataAtom(P predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
        return new DataAtomImpl<>(predicate, arguments);
    }

    @Override
    public <P extends AtomPredicate> DataAtom<P> getDataAtom(P predicate, VariableOrGroundTerm... terms) {
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
    public Function getMutableTripleAtom(Term subject, Term property, Term object) {
        return termFactory.getFunction(triplePredicate, subject, property, object);
    }

    @Override
    public Function getMutableTripleAtom(Term subject, IRI propertyIRI, Term object) {
        return getMutableTripleAtom(subject, termFactory.getConstantIRI(propertyIRI), object);
    }

    @Override
    public Function getMutableTripleAtom(Term subject, IRI classIRI) {
        return getMutableTripleAtom(subject, rdfTypeConstant, termFactory.getConstantIRI(classIRI));
    }

    @Override
    public TriplePredicate getTripleAtomPredicate() {
        return triplePredicate;
    }


    @Override
    public DataAtom<TriplePredicate> getTripleAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                   VariableOrGroundTerm object) {
        return getDataAtom(triplePredicate, subject, property, object);
    }

    @Override
    public ImmutableFunctionalTerm getTripleAtom(VariableOrGroundTerm subject, IRI propertyIRI,
                                                 VariableOrGroundTerm object) {
        return getTripleAtom(subject, termFactory.getConstantIRI(propertyIRI), object);
    }

    @Override
    public ImmutableFunctionalTerm getTripleAtom(VariableOrGroundTerm subject, IRI classIRI) {
        return getTripleAtom(subject, rdfTypeConstant, termFactory.getConstantIRI(classIRI));
    }
}
