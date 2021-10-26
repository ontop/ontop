package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.stream.IntStream;


public class AtomFactoryImpl implements AtomFactory {

    private final TriplePredicate triplePredicate;
    private final QuadPredicate quadPredicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    // These are used for RDF-star support
    private final TripleNestedSubjectPredicate tripleNestedSubjectPredicate;
    private final TripleNestedObjectPredicate tripleNestedObjectPredicate;
    private final TripleNestedSOPredicate tripleNestedSOPredicate;
    private final TripleRefSimplePredicate tripleRefSimplePredicate;
    private final TripleRefNestedSubjectPredicate tripleRefNestedSubjectPredicate;
    private final TripleRefNestedObjectPredicate tripleRefNestedObjectPredicate;
    private final TripleRefNestedSOPredicate tripleRefNestedSOPredicate;

    @Inject
    private AtomFactoryImpl(TermFactory termFactory, TypeFactory typeFactory, org.apache.commons.rdf.api.RDF rdfFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;

        RDFTermTypeConstant iriType = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());

        triplePredicate = new TriplePredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType()),
                iriType, rdfFactory);
        quadPredicate = new QuadPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType(),
                typeFactory.getIRITermType()),
                iriType, rdfFactory);
        // These are used for RDF-star support
        tripleRefSimplePredicate = new TripleRefSimplePredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType(),
                typeFactory.getIRITermType()),
                iriType, rdfFactory);
        tripleRefNestedSubjectPredicate = new TripleRefNestedSubjectPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractRDFStarTermType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType(),
                typeFactory.getIRITermType()),
                iriType, rdfFactory);
        tripleRefNestedObjectPredicate = new TripleRefNestedObjectPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFStarTermType(),
                typeFactory.getIRITermType()),
                iriType, rdfFactory);
        tripleRefNestedSOPredicate = new TripleRefNestedSOPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractRDFStarTermType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFStarTermType(),
                typeFactory.getIRITermType()),
                iriType, rdfFactory);
        tripleNestedSubjectPredicate = new TripleNestedSubjectPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractRDFStarTermType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFTermType()),
                iriType, rdfFactory);
        tripleNestedObjectPredicate = new TripleNestedObjectPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractObjectRDFType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFStarTermType()),
                iriType, rdfFactory);
        tripleNestedSOPredicate = new TripleNestedSOPredicateImpl(ImmutableList.of(
                typeFactory.getAbstractRDFStarTermType(),
                typeFactory.getIRITermType(),
                typeFactory.getAbstractRDFStarTermType()),
                iriType, rdfFactory);
    }

    @Override
    public AtomPredicate getRDFAnswerPredicate(int arity) {
        ImmutableList<TermType> defaultBaseTypes = IntStream.range(0, arity).boxed()
                .map(i -> typeFactory.getAbstractRDFTermType())
                .collect(ImmutableCollectors.toList());
        return new AtomPredicateImpl(PredicateConstants.ONTOP_QUERY, defaultBaseTypes);
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
    public DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> arguments) {
        return new DistinctVariableOnlyDataAtomImpl(predicate, arguments);
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate, Variable... arguments) {
        return getDistinctVariableOnlyDataAtom(predicate, ImmutableList.copyOf(arguments));
    }

    private IRIConstant convertIRIIntoConstant(IRI iri) {
        return termFactory.getConstantIRI(iri);
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctTripleAtom(Variable subject, Variable property, Variable object) {
        return getDistinctVariableOnlyDataAtom(triplePredicate, subject, property, object);
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctRDFStarTripleAtom(Variable subject, Variable property, Variable object, boolean nestedSubject, boolean nestedObject) {
        RDFAtomPredicate predicate;
        if (nestedSubject) {
            if (nestedObject) {
                predicate = tripleNestedSOPredicate;
            } else {
                predicate = tripleNestedSubjectPredicate;
            }
        } else {
            if (nestedObject) {
                predicate = tripleNestedObjectPredicate;
            } else {
                predicate = triplePredicate;    // This case should never occur?
            }
        }
        return getDistinctVariableOnlyDataAtom(predicate, subject, property, object);
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctRDFStarTripleRefAtom(Variable subject, Variable property, Variable object, Variable tripleRef, boolean nestedSubject, boolean nestedObject) {
        RDFAtomPredicate predicate;
        if (nestedSubject) {
            if (nestedObject) {
                predicate = tripleRefNestedSOPredicate;
            } else {
                predicate = tripleRefNestedSubjectPredicate;
            }
        } else {
            if (nestedObject) {
                predicate = tripleRefNestedObjectPredicate;
            } else {
                predicate = tripleRefSimplePredicate;
            }
        }
        return getDistinctVariableOnlyDataAtom(predicate, subject, property, object, tripleRef);
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                            VariableOrGroundTerm object) {
        return getDataAtom(triplePredicate, subject, property, object);
    }

    // Davide> TODO Add "quad" version for other methods as well
    @Override
    public DataAtom<AtomPredicate> getIntensionalQuadAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                          VariableOrGroundTerm object, VariableOrGroundTerm graph) {
        return getDataAtom(quadPredicate, subject, property, object, graph);
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI propertyIRI,
                                                            VariableOrGroundTerm object) {
        // TODO: in the future, constants will be for IRIs in intensional data atoms
        return getIntensionalTripleAtom(subject, convertIRIIntoConstant(propertyIRI), object);
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI classIRI) {
        // TODO: in the future, constants will be for IRIs in intensional data atoms
        return getIntensionalTripleAtom(subject, RDF.TYPE, convertIRIIntoConstant(classIRI));
    }

    @Override
    public DataAtom<AtomPredicate> getIntensionalQuadAtom(VariableOrGroundTerm subject, IRI classIRI, VariableOrGroundTerm graph) {
        // TODO: in the future, constants will be for IRIs in intensional data atoms
        return getIntensionalQuadAtom(subject, convertIRIIntoConstant(RDF.TYPE), convertIRIIntoConstant(classIRI), graph);
    }

    @Override
    public DistinctVariableOnlyDataAtom getDistinctQuadAtom(Variable subject, Variable property, Variable object,
                                                            Variable namedGraph) {
        return getDistinctVariableOnlyDataAtom(quadPredicate, subject, property, object, namedGraph);
    }

    // These are used for RDF-star support
    public DataAtom<AtomPredicate> getIntensionalTripleRefSimpleAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                               VariableOrGroundTerm object, VariableOrGroundTerm ref) {
        return getDataAtom(tripleRefSimplePredicate, subject, property, object, ref);
    }

    public DataAtom<AtomPredicate> getIntensionalTripleRefNestedSubjectAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                                     VariableOrGroundTerm object, VariableOrGroundTerm ref) {
        return getDataAtom(tripleRefNestedSubjectPredicate, subject, property, object, ref);
    }

    public DataAtom<AtomPredicate> getIntensionalTripleRefNestedObjectAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                                     VariableOrGroundTerm object, VariableOrGroundTerm ref) {
        return getDataAtom(tripleRefNestedObjectPredicate, subject, property, object, ref);
    }

    public DataAtom<AtomPredicate> getIntensionalTripleRefNestedSOAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                                     VariableOrGroundTerm object, VariableOrGroundTerm ref) {
        return getDataAtom(tripleRefNestedSOPredicate, subject, property, object, ref);
    }

    public DataAtom<AtomPredicate> getIntensionalTripleNestedSubjectAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                                            VariableOrGroundTerm object) {
        return getDataAtom(tripleNestedSubjectPredicate, subject, property, object);
    }

    public DataAtom<AtomPredicate> getIntensionalTripleNestedObjectAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                                           VariableOrGroundTerm object) {
        return getDataAtom(tripleNestedObjectPredicate, subject, property, object);
    }

    public DataAtom<AtomPredicate> getIntensionalTripleNestedSOAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                                       VariableOrGroundTerm object) {
        return getDataAtom(tripleNestedSOPredicate, subject, property, object);
    }
}
