package it.unibz.inf.ontop.spec.ontology.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.ontology.*;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.junit.Test;

import static it.unibz.inf.ontop.spec.ontology.impl.DatatypeImpl.rdfsLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TopBotInClassifiedTBoxTest {

    private final RDF rdfFactory = new SimpleRDF();
    private static final TermFactory TERM_FACTORY = OntopModelConfiguration.defaultBuilder().build().getTermFactory();

    @Test
    public void topClass() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(ClassImpl.owlThing, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> top = tbox.classesDAG().getVertex(ClassImpl.owlThing);
        assertEquals(2, top.getMembers().size());
        assertEquals(ImmutableSet.of(ClassImpl.owlThing, a), top.getMembers());
    }

    @Test
    public void bottomClass() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(a, ClassImpl.owlNothing);
        Ontology ontology = builder.build();

        // A <= bot is replaced by disjointness
        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> bot = tbox.classesDAG().getVertex(ClassImpl.owlNothing);
        assertNull(bot);
    }

    @Test
    public void noTopBottomClass() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        OClass b = builder.declareClass(rdfFactory.createIRI("http://b"));
        builder.addSubClassOfAxiom(b, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> ae = tbox.classesDAG().getVertex(a);
        assertEquals(1, ae.getMembers().size());
        assertEquals(ImmutableSet.of(a), ae.getMembers());
        assertEquals(1, tbox.classesDAG().getDirectSub(ae).size());
    }


    @Test
    public void topObjectProperty() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        ObjectPropertyExpression a = builder.declareObjectProperty(rdfFactory.createIRI("http://a"));
        builder.addSubPropertyOfAxiom(ObjectPropertyExpressionImpl.owlTopObjectProperty, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ObjectPropertyExpression> top = tbox.objectPropertiesDAG().getVertex(ObjectPropertyExpressionImpl.owlTopObjectProperty);
        assertEquals(3, top.getMembers().size());
        assertEquals(ImmutableSet.of(ObjectPropertyExpressionImpl.owlTopObjectProperty,
                a, a.getInverse()), top.getMembers());
    }

    @Test
    public void topObjectPropertyDomain() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        OClass b = builder.declareClass(rdfFactory.createIRI("http://b"));
        builder.addSubClassOfAxiom(ObjectPropertyExpressionImpl.owlTopObjectProperty.getDomain(), a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> top = tbox.classesDAG().getVertex(ClassImpl.owlThing);
        assertEquals(2, top.getMembers().size());
        assertEquals(ImmutableSet.of(ClassImpl.owlThing, a), top.getMembers());
        Equivalences<ClassExpression> be = tbox.classesDAG().getVertex(b);
        assertEquals(1, be.getMembers().size());
        assertEquals(ImmutableSet.of(b), be.getMembers());
    }

    @Test
    public void topObjectPropertyRange() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(ObjectPropertyExpressionImpl.owlTopObjectProperty.getRange(), a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> top = tbox.classesDAG().getVertex(ClassImpl.owlThing);
        assertEquals(2, top.getMembers().size());
        assertEquals(ImmutableSet.of(ClassImpl.owlThing, a), top.getMembers());
    }

    @Test
    public void botObjectProperty() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        ObjectPropertyExpression a = builder.declareObjectProperty(rdfFactory.createIRI("http://a"));
        builder.addSubPropertyOfAxiom(ObjectPropertyExpressionImpl.owlBottomObjectProperty, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ObjectPropertyExpression> bot = tbox.objectPropertiesDAG().getVertex(ObjectPropertyExpressionImpl.owlBottomObjectProperty);
        assertNull(bot);
    }

    @Test
    public void botObjectPropertyDomain() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(a, ObjectPropertyExpressionImpl.owlBottomObjectProperty.getDomain());
        Ontology ontology = builder.build();

        // A <= bot is replaced by disjointness
        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> bot = tbox.classesDAG().getVertex(ClassImpl.owlNothing);
        assertNull(bot);
    }

    @Test
    public void botObjectPropertyRange() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(a, ObjectPropertyExpressionImpl.owlBottomObjectProperty.getRange());
        Ontology ontology = builder.build();

        // A <= bot is replaced by disjointness
        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> bot = tbox.classesDAG().getVertex(ClassImpl.owlNothing);
        assertNull(bot);
    }


    @Test
    public void noTopBottomObjectProperty() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        ObjectPropertyExpression a = builder.declareObjectProperty(rdfFactory.createIRI("http://a"));
        ObjectPropertyExpression b = builder.declareObjectProperty(rdfFactory.createIRI("http://b"));
        builder.addSubPropertyOfAxiom(b, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ObjectPropertyExpression> ae = tbox.objectPropertiesDAG().getVertex(a);
        assertEquals(1, ae.getMembers().size());
        assertEquals(ImmutableSet.of(a), ae.getMembers());
        assertEquals(1, tbox.objectPropertiesDAG().getDirectSub(ae).size());

        Equivalences<ObjectPropertyExpression> aei = tbox.objectPropertiesDAG().getVertex(a.getInverse());
        assertEquals(1, aei.getMembers().size());
        assertEquals(ImmutableSet.of(a.getInverse()), aei.getMembers());
        assertEquals(1, tbox.objectPropertiesDAG().getDirectSub(aei).size());
    }

    @Test
    public void topDataProperty() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        DataPropertyExpression a = builder.declareDataProperty(rdfFactory.createIRI("http://a"));
        builder.addSubPropertyOfAxiom(DataPropertyExpressionImpl.owlTopDataProperty, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<DataPropertyExpression> top = tbox.dataPropertiesDAG().getVertex(DataPropertyExpressionImpl.owlTopDataProperty);
        assertEquals(2, top.getMembers().size());
        assertEquals(ImmutableSet.of(DataPropertyExpressionImpl.owlTopDataProperty, a), top.getMembers());
    }

    @Test
    public void topDataPropertyDomain() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(DataPropertyExpressionImpl.owlTopDataProperty.getDomainRestriction(rdfsLiteral), a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> top = tbox.classesDAG().getVertex(ClassImpl.owlThing);
        assertEquals(2, top.getMembers().size());
        assertEquals(ImmutableSet.of(ClassImpl.owlThing, a), top.getMembers());
    }

    @Test
    public void botDataProperty() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        DataPropertyExpression a = builder.declareDataProperty(rdfFactory.createIRI("http://a"));
        builder.addSubPropertyOfAxiom(DataPropertyExpressionImpl.owlBottomDataProperty, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<DataPropertyExpression> bot = tbox.dataPropertiesDAG().getVertex(DataPropertyExpressionImpl.owlBottomDataProperty);
        assertNull(bot);
    }

    @Test
    public void botDataPropertyDomain() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        OClass a = builder.declareClass(rdfFactory.createIRI("http://a"));
        builder.addSubClassOfAxiom(a,
                DataPropertyExpressionImpl.owlBottomDataProperty.getDomainRestriction(rdfsLiteral));
        Ontology ontology = builder.build();

        // A <= bot is replaced by disjointness
        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<ClassExpression> bot = tbox.classesDAG().getVertex(ClassImpl.owlNothing);
        assertNull(bot);
    }

    @Test
    public void noTopBottomDataProperty() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(rdfFactory, TERM_FACTORY);
        DataPropertyExpression a = builder.declareDataProperty(rdfFactory.createIRI("http://a"));
        DataPropertyExpression b = builder.declareDataProperty(rdfFactory.createIRI("http://b"));
        builder.addSubPropertyOfAxiom(b, a);
        Ontology ontology = builder.build();

        ClassifiedTBox tbox = ontology.tbox();
        Equivalences<DataPropertyExpression> ae = tbox.dataPropertiesDAG().getVertex(a);
        assertEquals(1, ae.getMembers().size());
        assertEquals(ImmutableSet.of(a), ae.getMembers());
        assertEquals(1, tbox.dataPropertiesDAG().getDirectSub(ae).size());
    }
}
