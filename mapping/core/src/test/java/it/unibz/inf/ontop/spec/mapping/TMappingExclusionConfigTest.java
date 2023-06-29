package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static it.unibz.inf.ontop.utils.MappingTestingTools.RDF_FACTORY;
import static it.unibz.inf.ontop.utils.MappingTestingTools.TERM_FACTORY;

import static org.junit.jupiter.api.Assertions.*;

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

        assertAll(
                () -> assertTrue(conf.contains(A)),         // in the config
                () -> assertFalse(conf.contains(B)), // not in the config
                () -> assertFalse(conf.contains(Pc)), // wrong type
                () -> assertTrue(conf.contains(P)), // in the config
                () -> assertFalse(conf.contains(Q)), // not in the config
                () -> assertFalse(conf.contains(Ac))); // wrong type
    }

    @Test
    public void testNotExistingFile() {
        assertThrows(FileNotFoundException.class, () -> TMappingExclusionConfig.parseFile("not_existing.conf"));
    }

    @Test
    public void testBadFile() {
        assertThrows(IllegalArgumentException.class, () -> TMappingExclusionConfig.parseFile("src/test/resources/tmappingExclusionConf/bad.conf"));
    }
}