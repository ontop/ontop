package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static it.unibz.inf.ontop.utils.MappingTestingTools.TERM_FACTORY;
import static junit.framework.TestCase.*;

public class MappingSaturationTest {
    private static final RelationPredicate P1_PREDICATE;

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingTest.class);

    private static final DBMetadata DB_METADATA;

    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");

    private static Variable S = TERM_FACTORY.getVariable("s");
    private static Variable P = TERM_FACTORY.getVariable("p");
    private static Variable O = TERM_FACTORY.getVariable("o");

    private static final String URI_TEMPLATE_PERSON, URI_TEMPLATE_COURSE1, URI_TEMPLATE_COURSE2;

    private static final IRI PROP_GIVES_LECTURE, PROP_TEACHES, PROP_GIVES_LAB, PROP_IS_TAUGHT_BY;

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DBTermType dbIntType = TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p1"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), dbIntType.getName(), dbIntType, false);
        table1Def.addAttribute(idFactory.createAttributeID("col12"), dbIntType.getName(), dbIntType, false);
        P1_PREDICATE = table1Def.getAtomPredicate();

        URI_TEMPLATE_PERSON =  "http://example.org/person/{}";
        URI_TEMPLATE_COURSE1 =  "http://example.org/uni1/course/{}";
        URI_TEMPLATE_COURSE2 =  "http://example.org/uni2/course/{}";

        DB_METADATA = dbMetadata;

        PROP_GIVES_LECTURE = RDF_FACTORY.createIRI("http://example.org/voc#givesLecture");
        PROP_TEACHES = RDF_FACTORY.createIRI("http://example.org/voc#teaches");
        PROP_GIVES_LAB = RDF_FACTORY.createIRI("http://example.org/voc#givesLab");
        PROP_IS_TAUGHT_BY = RDF_FACTORY.createIRI("http://example.org/voc#isTaughtBy");
    }

    @Test
    public void testMappingSaturationInverses() throws InconsistentOntologyException {

        OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY);
        ObjectPropertyExpression givesLecture = builder.declareObjectProperty(PROP_GIVES_LECTURE);
        ObjectPropertyExpression teaches = builder.declareObjectProperty(PROP_TEACHES);
        ObjectPropertyExpression givesLab =builder.declareObjectProperty(PROP_GIVES_LAB);
        ObjectPropertyExpression isTaughtBy = builder.declareObjectProperty(PROP_IS_TAUGHT_BY);
        builder.addSubPropertyOfAxiom(givesLab, teaches);
        builder.addSubPropertyOfAxiom(givesLecture, teaches);
        builder.addSubPropertyOfAxiom(isTaughtBy, teaches.getInverse());
        builder.addSubPropertyOfAxiom(teaches.getInverse(), isTaughtBy);
        Ontology onto = builder.build();
        ClassifiedTBox classifiedTBox = onto.tbox();

        DistinctVariableOnlyDataAtom spoAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);

        // ex:person/{ssn} :teaches ex:uni1/course/{c_id}
        IQ maTeaches;
        {
            ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(
                    ImmutableSet.of(S, P, O),
                    SUBSTITUTION_FACTORY.getSubstitution(
                            S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_PERSON, ImmutableList.of(A)),
                            P, getConstantIRI(PROP_TEACHES),
                            O, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_COURSE1, ImmutableList.of(B))));

            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                    ATOM_FACTORY.getDataAtom(P1_PREDICATE, ImmutableList.of(A, B)));

            maTeaches = IQ_FACTORY.createIQ(spoAtom, IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode));
        }
        // ex:person/{ssn} :givesLab ex:uni2/course/{cid}
        IQ maGivesLab;
        {
            ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(
                    ImmutableSet.of(S, P, O),
                    SUBSTITUTION_FACTORY.getSubstitution(
                            S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_PERSON, ImmutableList.of(A)),
                            P, getConstantIRI(PROP_GIVES_LAB),
                            O, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_COURSE2, ImmutableList.of(B))));

            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                    ATOM_FACTORY.getDataAtom(P1_PREDICATE, ImmutableList.of(A, B)));

            maGivesLab = IQ_FACTORY.createIQ(spoAtom, IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode));
        }
        // ex:person/{ssn} :givesLecture ex:uni2/course/{cid}
        IQ maGivesLecture;
        {
            ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(
                    ImmutableSet.of(S, P, O),
                    SUBSTITUTION_FACTORY.getSubstitution(
                            S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_PERSON, ImmutableList.of(A)),
                            P, getConstantIRI(PROP_GIVES_LECTURE),
                            O, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_COURSE2, ImmutableList.of(B))));

            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                    ATOM_FACTORY.getDataAtom(P1_PREDICATE, ImmutableList.of(A, B)));

            maGivesLecture = IQ_FACTORY.createIQ(spoAtom, IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode));
        }

        /*
         * Renaming
         */
        RDFAtomPredicate tp = (RDFAtomPredicate)spoAtom.getPredicate();
        ImmutableList<MappingAssertion> mapping = ImmutableList.of(
                new MappingAssertion(MappingAssertionIndex.ofProperty(tp, PROP_TEACHES), maTeaches, null),
                new MappingAssertion(MappingAssertionIndex.ofProperty(tp, PROP_GIVES_LAB), maGivesLab, null),
                new MappingAssertion(MappingAssertionIndex.ofProperty(tp, PROP_GIVES_LECTURE), maGivesLecture, null));

        ImmutableMap<MappingAssertionIndex, IQ> saturatedMapping = MAPPING_SATURATOR.saturate(mapping, DB_METADATA, classifiedTBox);

        assertEquals(maGivesLab, saturatedMapping.get(MappingAssertionIndex.ofProperty(tp, PROP_GIVES_LAB)));
        assertEquals(maGivesLecture, saturatedMapping.get(MappingAssertionIndex.ofProperty(tp, PROP_GIVES_LECTURE)));

        assertTrue(saturatedMapping.get(MappingAssertionIndex.ofProperty(tp, PROP_IS_TAUGHT_BY)).getTree().getChildren().get(0).getRootNode() instanceof UnionNode);
        System.out.println(PROP_IS_TAUGHT_BY + ":\n" + saturatedMapping.get(MappingAssertionIndex.ofProperty(tp, PROP_IS_TAUGHT_BY)));

        assertTrue(saturatedMapping.get(MappingAssertionIndex.ofProperty(tp, PROP_TEACHES)).getTree().getChildren().get(0).getRootNode() instanceof UnionNode);
        System.out.println(PROP_TEACHES + ":\n" + saturatedMapping.get(MappingAssertionIndex.ofProperty(tp, PROP_TEACHES)) + "\nvs\n" + maTeaches);
    }


    private ImmutableTerm getConstantIRI(IRI iri) {
        return TERM_FACTORY.getConstantIRI(iri);
    }
}
