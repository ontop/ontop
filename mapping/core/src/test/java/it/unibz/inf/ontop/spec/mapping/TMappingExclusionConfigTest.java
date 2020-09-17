package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.junit.Test;

import java.io.FileNotFoundException;

import static it.unibz.inf.ontop.utils.MappingTestingTools.RDF_FACTORY;
import static it.unibz.inf.ontop.utils.MappingTestingTools.TERM_FACTORY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TMappingExclusionConfigTest {

    @Test
    public void testParseFile() throws Exception {
        TMappingExclusionConfig conf = TMappingExclusionConfig.parseFile("src/test/resources/tmappingExclusionConf/good.conf");
        OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY, TERM_FACTORY);
        OClass A = builder.declareClass(RDF_FACTORY.createIRI("http://www.example.org/A"));
        OClass B = builder.declareClass(RDF_FACTORY.createIRI("http://wwww.example.org/B"));
        OClass Pc = builder.declareClass(RDF_FACTORY.createIRI("http://wwww.example.org/P"));
        ObjectPropertyExpression P = builder.declareObjectProperty(RDF_FACTORY.createIRI("http://www.example.org/P"));
        ObjectPropertyExpression Q = builder.declareObjectProperty(RDF_FACTORY.createIRI("http://www.example.org/Q"));
        ObjectPropertyExpression Ac = builder.declareObjectProperty(RDF_FACTORY.createIRI("http://www.example.org/A"));
        // in the config
        assertTrue(conf.contains(A));
        // not in the config
        assertFalse(conf.contains(B));
        // wrong type
        assertFalse(conf.contains(Pc));
        // in the config
        assertTrue(conf.contains(P));
        // not in the config
        assertFalse(conf.contains(Q));
        // wrong type
        assertFalse(conf.contains(Ac));
    }

    // File not found
    @Test(expected = FileNotFoundException.class)
    public void testNotExistingFile() throws Exception {
        TMappingExclusionConfig.parseFile("not_existing.conf");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testBadFile() throws Exception {
        TMappingExclusionConfig.parseFile("src/test/resources/tmappingExclusionConf/bad.conf");
    }
}