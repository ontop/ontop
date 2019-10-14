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
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.stream.Stream;

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

    private static final Constant URI_TEMPLATE_PERSON, URI_TEMPLATE_COURSE1, URI_TEMPLATE_COURSE2;

    private static final IRI PROP_GIVES_LECTURE, PROP_TEACHES, PROP_GIVES_LAB, PROP_IS_TAUGHT_BY;

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p1"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col12"), Types.INTEGER, null, false);
        P1_PREDICATE = table1Def.getAtomPredicate();

        URI_TEMPLATE_PERSON =  TERM_FACTORY.getConstantLiteral("http://example.org/person/{}");
        URI_TEMPLATE_COURSE1 =  TERM_FACTORY.getConstantLiteral("http://example.org/uni1/course/{}");
        URI_TEMPLATE_COURSE2 =  TERM_FACTORY.getConstantLiteral("http://example.org/uni2/course/{}");

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
                            S, TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_PERSON, A),
                            P, getConstantIRI(PROP_TEACHES),
                            O, TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_COURSE1, B)));

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
                            S, TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_PERSON, A),
                            P, getConstantIRI(PROP_GIVES_LAB),
                            O, TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_COURSE2, B)));

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
                            S, TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_PERSON, A),
                            P, getConstantIRI(PROP_GIVES_LECTURE),
                            O, TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_COURSE2, B)));

            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                    ATOM_FACTORY.getDataAtom(P1_PREDICATE, ImmutableList.of(A, B)));

            maGivesLecture = IQ_FACTORY.createIQ(spoAtom, IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode));
        }

        /*
         * Renaming
         */
        MappingMetadata mappingMetadata = MAPPING_FACTORY.createMetadata(MAPPING_FACTORY.createPrefixManager(ImmutableMap.of()),
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY));

        Mapping mapping = MAPPING_FACTORY.createMapping(mappingMetadata,
                transformIntoTable(ImmutableMap.of(PROP_TEACHES, maTeaches, PROP_GIVES_LAB, maGivesLab, PROP_GIVES_LECTURE, maGivesLecture)),
                transformIntoTable(ImmutableMap.of()));


        Mapping saturatedMapping = MAPPING_SATURATOR.saturate(mapping, DB_METADATA, classifiedTBox);
        RDFAtomPredicate triplesPredicate = saturatedMapping.getRDFAtomPredicates().iterator().next();

        assertEquals(maGivesLab, saturatedMapping.getRDFPropertyDefinition(triplesPredicate, PROP_GIVES_LAB).get());
        assertEquals(maGivesLecture, saturatedMapping.getRDFPropertyDefinition(triplesPredicate, PROP_GIVES_LECTURE).get());

        assertTrue(saturatedMapping.getRDFPropertyDefinition(triplesPredicate, PROP_IS_TAUGHT_BY).get().getTree().getChildren().get(0).getRootNode() instanceof UnionNode);
        System.out.println(PROP_IS_TAUGHT_BY + ":\n" + saturatedMapping.getRDFPropertyDefinition(triplesPredicate, PROP_IS_TAUGHT_BY));

        assertTrue(saturatedMapping.getRDFPropertyDefinition(triplesPredicate, PROP_TEACHES).get().getTree().getChildren().get(0).getRootNode() instanceof UnionNode);
        System.out.println(PROP_TEACHES + ":\n" + saturatedMapping.getRDFPropertyDefinition(triplesPredicate, PROP_TEACHES));
    }


    private ImmutableTerm getConstantIRI(IRI iri) {
        return TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantLiteral(iri.getIRIString()));
    }

    private static ImmutableTable<RDFAtomPredicate, IRI, IQ> transformIntoTable(ImmutableMap<IRI, IQ> map) {
        return map.entrySet().stream()
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate)e.getValue().getProjectionAtom().getPredicate(),
                        e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toTable());
    }

}
