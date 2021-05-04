package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleNestedSOPredicate;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.RDF;

public class TripleNestedSOPredicateImpl extends TripleNestedPredicateImpl implements TripleNestedSOPredicate {

    protected TripleNestedSOPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("tripleNestedSO", expectedBaseTypes, iriType, rdfFactory);
    }
}
