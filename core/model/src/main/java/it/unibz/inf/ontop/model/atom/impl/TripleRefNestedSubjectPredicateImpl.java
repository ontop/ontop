package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleRefNestedSubjectPredicate;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.RDF;

public class TripleRefNestedSubjectPredicateImpl extends TripleRefPredicateImpl implements TripleRefNestedSubjectPredicate {
    protected TripleRefNestedSubjectPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("TripleRefNestedSubject", expectedBaseTypes, iriType, rdfFactory);
    }
}
