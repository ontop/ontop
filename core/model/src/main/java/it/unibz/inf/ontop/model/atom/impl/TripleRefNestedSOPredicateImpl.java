package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleRefNestedSOPredicate;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.RDF;

public class TripleRefNestedSOPredicateImpl extends TripleRefPredicateImpl implements TripleRefNestedSOPredicate {
    protected TripleRefNestedSOPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("TripleRefNestedSO", expectedBaseTypes, iriType, rdfFactory);
    }
}
