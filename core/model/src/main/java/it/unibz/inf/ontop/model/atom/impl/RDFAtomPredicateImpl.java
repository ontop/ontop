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

    private final int propertyIndex;
    private final int classIndex;

    protected RDFAtomPredicateImpl(String name, int arity, ImmutableList<TermType> expectedBaseTypes,
                                   int propertyIndex, int classIndex) {
        super(name, arity, expectedBaseTypes);
        this.propertyIndex = propertyIndex;
        this.classIndex = classIndex;

        if (propertyIndex >= arity)
            throw new IllegalArgumentException("propertyIndex must be inferior to arity");
        if (classIndex >= arity)
            throw new IllegalArgumentException("classIndex must be inferior to arity");

    }

    @Override
    public Optional<IRI> getClassIRI(ImmutableList<? extends ImmutableTerm> atomArguments) {
        if (atomArguments.size() != getArity())
            throw new IllegalArgumentException("The given arguments do not match with the expected arity");
        return getPropertyIRI(atomArguments)
                .filter(i -> i.equals(RDF.TYPE))
                .flatMap(i -> extractIRI(atomArguments.get(classIndex)));
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
                        ? extractIRI(atomArguments.get(classIndex))
                        : Optional.of(i));
    }

    @Override
    public <T extends ImmutableTerm> T getSubject(ImmutableList<T> atomArguments) {
        return atomArguments.get(0);
    }

    @Override
    public <T extends ImmutableTerm> T getProperty(ImmutableList<T> atomArguments) {
        return atomArguments.get(1);
    }

    @Override
    public <T extends ImmutableTerm> T getObject(ImmutableList<T> atomArguments) {
        return atomArguments.get(2);
    }

    /**
     * TODO: make it more robust
     */
    private Optional<IRI> extractIRI(ImmutableTerm term) {
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
