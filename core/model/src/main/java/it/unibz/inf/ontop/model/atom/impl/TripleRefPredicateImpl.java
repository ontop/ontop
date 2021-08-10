package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleRefPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

public abstract class TripleRefPredicateImpl extends RDFAtomPredicateImpl implements TripleRefPredicate {

    private static final int TRIPLE_REFERENCE_INDEX = 3;

    protected TripleRefPredicateImpl(String name, ImmutableList<TermType> expectedBaseTypes,
                                     RDFTermTypeConstant iriType, RDF rdfFactory) {
        super(name, expectedBaseTypes, 0, 1, 2, iriType, rdfFactory);
    }

    @Override
    public <T extends ImmutableTerm> T getTripleReference(ImmutableList<T> atomArguments) {
        return atomArguments.get(TRIPLE_REFERENCE_INDEX);
    }

    @Override
    public Optional<IRI> getGraphIRI(ImmutableList<? extends ImmutableTerm> atomArguments) {
        return Optional.empty();
    }

    @Override
    public <T extends ImmutableTerm> Optional<T> getGraph(ImmutableList<T> atomArguments) {
        return Optional.empty();
    }

    @Override
    public <T extends ImmutableTerm> ImmutableList<T>  updateSPO(ImmutableList<T> originalArguments, T newSubject,
                                                                 T newProperty, T newObject) {
        return ImmutableList.of(newSubject, newProperty, newObject, originalArguments.get(TRIPLE_REFERENCE_INDEX));
    }
}
