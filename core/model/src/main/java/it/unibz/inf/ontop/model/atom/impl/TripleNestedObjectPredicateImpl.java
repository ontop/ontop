package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleNestedObjectPredicate;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.RDF;

public class TripleNestedObjectPredicateImpl extends TripleNestedPredicateImpl implements TripleNestedObjectPredicate {

    protected TripleNestedObjectPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("tripleNestedObject", expectedBaseTypes, iriType, rdfFactory);
    }
}
