package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class TMappingExclusionConfigTest {

    @Test
    public void testParseFile() throws Exception {
        OntologyFactory factory = OntologyFactoryImpl.getInstance();
        TMappingExclusionConfig conf = TMappingExclusionConfig.parseFile("src/test/resources/tmappingExclusionConf/good.conf");
        // in the config
        assertTrue(conf.contains(factory.createClass("http://www.example.org/A")));
        // not in the config
        assertFalse(conf.contains(factory.createClass("http://wwww.example.org/B")));
        // wrong type
        assertFalse(conf.contains(factory.createObjectProperty("http://wwww.example.org/B")));
        // in the config
        assertTrue(conf.contains(factory.createObjectProperty("http://www.example.org/P")));
        // not in the config
        assertFalse(conf.contains(factory.createObjectProperty("http://wwww.example.org/Q")));
        // wrong type
        assertFalse(conf.contains(factory.createClass("http://wwww.example.org/P")));
    }

    @Test(expected = FileNotFoundException.class)
    public void testNotExistingFile() throws Exception {
        TMappingExclusionConfig.parseFile("not_existing.conf");
    }


    @Test//(expected = IllegalArgumentException.class)
    public void testBadFile() throws Exception {
        TMappingExclusionConfig.parseFile("src/test/resources/tmappingExclusionConf/bad.conf");
    }
}