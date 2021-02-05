package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public abstract class RDFAtomPredicateImpl extends AtomPredicateImpl implements RDFAtomPredicate {

    private final int subjectIndex;
    private final int propertyIndex;
    private final int objectIndex;
    private final RDFTermTypeConstant iriType;
    private final org.apache.commons.rdf.api.RDF rdfFactory;

    protected RDFAtomPredicateImpl(String name, ImmutableList<TermType> expectedBaseTypes,
                                   int subjectIndex, int propertyIndex, int objectIndex,
                                   RDFTermTypeConstant iriType, org.apache.commons.rdf.api.RDF rdfFactory) {
        super(name, expectedBaseTypes);
        this.subjectIndex = subjectIndex;
        this.propertyIndex = propertyIndex;
        this.objectIndex = objectIndex;
        this.rdfFactory = rdfFactory;
        this.iriType = iriType;

        if (subjectIndex >= expectedBaseTypes.size())
            throw new IllegalArgumentException("subjectIndex exceeds the arity");
        if (propertyIndex >= expectedBaseTypes.size())
            throw new IllegalArgumentException("propertyIndex exceeds the arity");
        if (objectIndex >= expectedBaseTypes.size())
            throw new IllegalArgumentException("objectIndex exceeds the arity");
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

    protected Optional<IRI> extractIRI(ImmutableTerm term) {
        if (term instanceof IRIConstant) {
            return Optional.of(((IRIConstant) term).getIRI());
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

            return Optional.of(functionalTerm)
                    .filter(f -> f.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
                    .filter(f -> f.getTerm(1).equals(iriType))
                    .map(f -> f.getTerm(0))
                    .filter(t -> t instanceof DBConstant)
                    .map(t -> rdfFactory.createIRI(((DBConstant) t).getValue()));
        }
        return Optional.empty();
    }
}
