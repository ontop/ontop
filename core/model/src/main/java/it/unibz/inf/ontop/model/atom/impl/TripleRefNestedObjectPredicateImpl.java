package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleRefNestedObjectPredicate;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.RDF;

public class TripleRefNestedObjectPredicateImpl extends TripleRefPredicateImpl implements TripleRefNestedObjectPredicate {
    protected TripleRefNestedObjectPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("TripleRefNestedObject", expectedBaseTypes, iriType, rdfFactory);
    }
}
