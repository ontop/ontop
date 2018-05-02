package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

import java.util.Optional;

public abstract class RDFAtomPredicateImpl extends AtomPredicateImpl implements RDFAtomPredicate {

    private final int subjectIndex;
    private final int propertyIndex;
    private final int objectIndex;

    protected RDFAtomPredicateImpl(String name, int arity, ImmutableList<TermType> expectedBaseTypes,
                                   int subjectIndex, int propertyIndex, int objectIndex) {
        super(name, arity, expectedBaseTypes);
        this.subjectIndex = subjectIndex;
        this.propertyIndex = propertyIndex;
        this.objectIndex = objectIndex;

        if (propertyIndex >= arity)
            throw new IllegalArgumentException("propertyIndex must be inferior to arity");
        if (objectIndex >= arity)
            throw new IllegalArgumentException("objectIndex must be inferior to arity");

    }

    @Override
    public Optional<IRI> getClassIRI(ImmutableList<? extends ImmutableTerm> atomArguments) {
        if (atomArguments.size() != getArity())
            throw new IllegalArgumentException("The given arguments do not match with the expected arity");
        return getPropertyIRI(atomArguments)
                .filter(i -> i.equals(RDF.TYPE))
                .flatMap(i -> extractIRI(atomArguments.get(objectIndex)));
    }

    @Override
    public Optional<IRI> getPropertyIRI(ImmutableList<? extends ImmutableTerm> atomArguments) {
        if (atomArguments.size() != getArity())
            throw new IllegalArgumentException("The given arguments do not match with the expected arity");
        return extractIRI(atomArguments.get(propertyIndex));
    }

    @Override
    public Optional<IRI> getPredicateIRI(ImmutableList<? extends ImmutableTerm> atomArguments) {
        return getPropertyIRI(atomArguments)
                .flatMap(i -> i.equals(RDF.TYPE)
                        ? extractIRI(atomArguments.get(objectIndex))
                        : Optional.of(i));
    }

    @Override
    public <T extends ImmutableTerm> T getSubject(ImmutableList<T> atomArguments) {
        return atomArguments.get(subjectIndex);
    }

    @Override
    public <T extends ImmutableTerm> T getProperty(ImmutableList<T> atomArguments) {
        return atomArguments.get(propertyIndex);
    }

    @Override
    public <T extends ImmutableTerm> T getObject(ImmutableList<T> atomArguments) {
        return atomArguments.get(objectIndex);
    }

    /**
     * TODO: make it more robust
     */
    protected Optional<IRI> extractIRI(ImmutableTerm term) {
        if (term instanceof IRIConstant) {
            return Optional.of(((IRIConstant) term).getIRI());
        }
        // TODO: look for the RDF building function (and check the type is an IRI)
        else if (term instanceof ImmutableFunctionalTerm) {
            return ((ImmutableFunctionalTerm) term).getArity() == 1
                    ? extractIRI(((ImmutableFunctionalTerm) term).getArguments().get(0))
                    : Optional.empty();
        }
        else if (term instanceof ValueConstant) {
            return Optional.of(new SimpleRDF().createIRI( ((ValueConstant) term).getValue()));
        }
        return Optional.empty();
    }
}
