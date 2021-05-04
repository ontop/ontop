package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TripleNestedSubjectPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

public class TripleNestedSubjectPredicateImpl extends TripleNestedPredicateImpl implements TripleNestedSubjectPredicate {

    protected TripleNestedSubjectPredicateImpl(ImmutableList<TermType> expectedBaseTypes, RDFTermTypeConstant iriType, RDF rdfFactory) {
        super("tripleNestedSubject", expectedBaseTypes, iriType, rdfFactory);
    }
}
