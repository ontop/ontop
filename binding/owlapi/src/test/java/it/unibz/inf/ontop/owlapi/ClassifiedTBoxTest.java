package it.unibz.inf.ontop.owlapi;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.ClassImpl;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.Test;

import static it.unibz.inf.ontop.owlapi.OWL2QLTranslatorTest.getIRI;
import static it.unibz.inf.ontop.owlapi.OWL2QLTranslatorTest.loadOntologyFromFileAndClassify;
import static org.junit.Assert.assertEquals;


/**
 * Check  ClassifiedTBox does not fail when equalities with top is present
 */

public class ClassifiedTBoxTest {

    private static final TermFactory TERM_FACTORY = OntopModelConfiguration.defaultBuilder().build().getTermFactory();

    /*
    top object property with domain and range, two top classes should be present in the ontology and owlthing
     */
    @Test
    public void test_top_property() throws Exception {

        ClassifiedTBox ontology = loadOntologyFromFileAndClassify("src/test/resources/ontology/ontology_with_top.owl");
        Equivalences<ClassExpression> top = ontology.classesDAG().getVertex(ClassImpl.owlThing);
        assertEquals(3, top.getMembers().size());
        OntologyBuilder builder = OntologyBuilderImpl.builder(new SimpleRDF(), TERM_FACTORY);
        OClass percorso = builder.declareClass(getIRI("http://my.org/navi#", "Percorso"));
        OClass linee = builder.declareClass(getIRI("http://my.org/navi#", "LineeDiPercorso"));
        assertEquals(ImmutableSet.of(ClassImpl.owlThing, percorso, linee), top.getMembers());
    }

}
