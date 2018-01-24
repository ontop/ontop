package it.unibz.inf.ontop.owlapi;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.ClassImpl;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Check  ClassifiedTBox does not fail when equalities with top is present
 */

public class ClassifiedTBoxTest {


    /*
    top object property with domain and range, two top classes should be present in the ontology and owlthing
     */
    @Test
    public void test_top_property() throws Exception {

        ClassifiedTBox ontology = OWL2QLTranslatorTest.loadOntologyFromFileAndClassify("src/test/resources/ontology/ontology_with_top.owl");
        Equivalences<ClassExpression> top = ontology.classesDAG().getVertex(ClassImpl.owlThing);
        assertEquals(3, top.getMembers().size());
        OntologyBuilder builder = OntologyBuilderImpl.builder();
        OClass percorso = builder.declareClass("http://my.org/navi#Percorso");
        OClass linee = builder.declareClass("http://my.org/navi#LineeDiPercorso");
        assertEquals(ImmutableSet.of(ClassImpl.owlThing, percorso, linee), top.getMembers());
    }

}
