package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

public class TriplePredicateImpl extends RDFAtomPredicateImpl implements TriplePredicate {

    protected TriplePredicateImpl(ImmutableList<TermType> expectedBaseTypes,
                                  RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("triple", expectedBaseTypes, 0, 1, 2, iriType, rdfFactory);
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
        return ImmutableList.of(newSubject, newProperty, newObject);
    }
}
