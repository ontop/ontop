package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleRefSimplePredicate;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.RDF;

public class TripleRefSimplePredicateImpl extends TripleRefPredicateImpl implements TripleRefSimplePredicate {
    protected TripleRefSimplePredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("TripleRefSimple", expectedBaseTypes, iriType, rdfFactory);
    }
}
