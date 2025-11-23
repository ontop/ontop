package it.unibz.inf.ontop.rdf4j.utils;

import it.unibz.inf.ontop.OntopModelTestingTools;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFStarTripleConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.eclipse.rdf4j.model.Triple;
import org.eclipse.rdf4j.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RDF4JHelperTest {

    @Test
    public void rdfStarTripleIsConvertedToRDF4JTriple() {
        TermFactory termFactory = OntopModelTestingTools.TERM_FACTORY;
        ImmutableTerm simplified = termFactory.getRDFStarTripleFunctionalTerm(
                termFactory.getConstantIRI("http://example.com/s"),
                termFactory.getConstantIRI("http://example.com/p"),
                termFactory.getRDFLiteralConstant("42", XSD.INTEGER)).simplify();

        assertTrue(simplified instanceof RDFStarTripleConstant);
        RDFStarTripleConstant tripleConstant = (RDFStarTripleConstant) simplified;

        Value rdf4jValue = RDF4JHelper.getValue(tripleConstant);
        assertTrue(rdf4jValue instanceof Triple);
        Triple tripleValue = (Triple) rdf4jValue;
        assertEquals("http://example.com/s", tripleValue.getSubject().stringValue());
        assertEquals("http://example.com/p", tripleValue.getPredicate().stringValue());
        assertEquals("42", tripleValue.getObject().stringValue());
    }
}
