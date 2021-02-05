package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

public class QuadPredicateImpl extends RDFAtomPredicateImpl implements QuadPredicate {

    private static final int NAMED_GRAPH_INDEX = 3;

    protected QuadPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType,
                                RDF rdfFactory) {
        super("quad", expectedBaseTypes,0, 1, 2, iriType, rdfFactory);
    }

    @Override
    public Optional<IRI> getGraphIRI(ImmutableList<? extends ImmutableTerm> atomArguments) {
        return extractIRI(atomArguments.get(NAMED_GRAPH_INDEX));
    }

    @Override
    public <T extends ImmutableTerm> Optional<T> getGraph(ImmutableList<T> atomArguments) {
        return Optional.of(atomArguments.get(NAMED_GRAPH_INDEX));
    }

    @Override
    public <T extends ImmutableTerm> ImmutableList<T> updateSPO(ImmutableList<T> originalArguments, T newSubject,
                                                                T newProperty, T newObject) {
        return ImmutableList.of(newSubject, newProperty, newObject, originalArguments.get(NAMED_GRAPH_INDEX));
    }
}
