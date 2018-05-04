package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
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
    private final QuadPredicate quadPredicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    @Inject
    private AtomFactoryImpl(TermFactory termFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        triplePredicate = new TriplePredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType()
        ));
        quadPredicate = new QuadPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType(),
                typeFactory.getIRITermType()
        ));
    }

    @Override
    public AtomPredicate getRDFAnswerPredicate(int arity) {
        ImmutableList<TermType> defaultBaseTypes = IntStream.range(0, arity).boxed()
                .map(i -> typeFactory.getAbstractRDFTermType())
                .collect(ImmutableCollectors.toList());
        return new AtomPredicateImpl(PredicateConstants.ONTOP_QUERY, arity, defaultBaseTypes);
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
    public Function getMutableTripleBodyAtom(Term subject, IRI propertyIRI, Term object) {
        // At the moment, no distinction between body and head atoms (this will change)
        return getMutableTripleHeadAtom(subject, propertyIRI, object);
    }

    @Override
    public Function getMutableTripleBodyAtom(Term subject, IRI classIRI) {
        // At the moment, no distinction between body and head atoms (this will change)
        return getMutableTripleHeadAtom(subject, classIRI);
    }

    @Override
    public Function getMutableTripleHeadAtom(Term subject, IRI propertyIRI, Term object) {
        return getMutableTripleAtom(
                subject,
                convertIRIIntoFunctionalGroundTerm(propertyIRI),
                object);
    }

    @Override
    public Function getMutableTripleHeadAtom(Term subject, IRI classIRI) {
        return getMutableTripleAtom(
                subject,
                convertIRIIntoFunctionalGroundTerm(RDF.TYPE),
                convertIRIIntoFunctionalGroundTerm(classIRI));
    }

    private Function convertIRIIntoFunctionalGroundTerm(IRI iri) {
        return termFactory.getUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
    }

    private GroundFunctionalTerm convertIRIIntoGroundFunctionalTerm(IRI iri) {
        return (GroundFunctionalTerm) termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctTripleAtom(Variable subject, Variable property, Variable object) {
        return getDistinctVariableOnlyDataAtom(triplePredicate, subject, property, object);
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                            VariableOrGroundTerm object) {
        return getDataAtom(triplePredicate, subject, property, object);
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI propertyIRI,
                                                            VariableOrGroundTerm object) {
        // TODO: in the future, constants will be for IRIs in intensional data atoms
        return getIntensionalTripleAtom(subject, convertIRIIntoGroundFunctionalTerm(propertyIRI), object);
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI classIRI) {
        // TODO: in the future, constants will be for IRIs in intensional data atoms
        return getIntensionalTripleAtom(subject, RDF.TYPE, convertIRIIntoGroundFunctionalTerm(classIRI));
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctQuadAtom(Variable subject, Variable property, Variable object,
                                                            Variable namedGraph) {
        return getDistinctVariableOnlyDataAtom(quadPredicate, subject, property, object, namedGraph);
    }
}
