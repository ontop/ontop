package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static junit.framework.TestCase.fail;

public class MappingTest {

    private static final RelationPredicate P1_PREDICATE;
    private static final RelationPredicate P3_PREDICATE;
    private static final RelationPredicate P4_PREDICATE;
    private static final RelationPredicate P5_PREDICATE;
    private static final RelationPredicate BROKER_PREDICATE;

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingTest.class);

    private static final DBMetadata DB_METADATA;

    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");
    private static Variable C = TERM_FACTORY.getVariable("c");
    private static Variable S = TERM_FACTORY.getVariable("s");
    private static Variable P = TERM_FACTORY.getVariable("p");
    private static Variable O = TERM_FACTORY.getVariable("o");

    private final static Variable Y = TERM_FACTORY.getVariable("company");

    private static final Constant URI_TEMPLATE_STR_1;

    private static final IRI PROP_1, PROP_2, CLASS_1;

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p1"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col12"), Types.INTEGER, null, false);
        P1_PREDICATE = table1Def.getAtomPredicate();

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p3"));
        table3Def.addAttribute(idFactory.createAttributeID("col31"), Types.INTEGER, null, false);
        P3_PREDICATE = table3Def.getAtomPredicate();

        DatabaseRelationDefinition table4Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p4"));
        table4Def.addAttribute(idFactory.createAttributeID("col41"), Types.INTEGER, null, false);
        P4_PREDICATE = table4Def.getAtomPredicate();

        DatabaseRelationDefinition table5Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p5"));
        table5Def.addAttribute(idFactory.createAttributeID("col51"), Types.INTEGER, null, false);
        P5_PREDICATE = table5Def.getAtomPredicate();


        DatabaseRelationDefinition tableBrokerDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID("DB2INST1", "brokerworksfor"));
        tableBrokerDef.addAttribute(idFactory.createAttributeID("broker"), Types.INTEGER, null, false);
        tableBrokerDef.addAttribute(idFactory.createAttributeID("company"), Types.INTEGER, null, true);
        tableBrokerDef.addAttribute(idFactory.createAttributeID("client"), Types.INTEGER, null, true);
        BROKER_PREDICATE = tableBrokerDef.getAtomPredicate();

        URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/person/{}");

        DB_METADATA = dbMetadata;

        PROP_1 = RDF_FACTORY.createIRI("http://example.org/voc#Prop1");
        PROP_2 = RDF_FACTORY.createIRI("http://example.org/voc#Prop2");
        CLASS_1 = RDF_FACTORY.createIRI("http://example.org/voc#Class1");
    }

    @Test
    public void testOfflineMappingAssertionsRenaming() {

        ImmutableList<IRI> propertyIris = ImmutableList.of(PROP_1, PROP_2);

        DataAtom<RelationPredicate> binaryExtensionalAtom = ATOM_FACTORY.getDataAtom(P1_PREDICATE, ImmutableList.of(A, B));
        DataAtom<RelationPredicate> unaryExtensionalAtom = ATOM_FACTORY.getDataAtom(P3_PREDICATE, ImmutableList.of(A));

        ImmutableMap.Builder<IRI, IQ> propertyMapBuilder = ImmutableMap.builder();
        RDFAtomPredicate rdfAtomPredicate = null;

        // Properties
        for (IRI propertyIri : propertyIris){
            IntermediateQueryBuilder mappingBuilder = createQueryBuilder(DB_METADATA);
            ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                    SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                            P, getConstantIRI(propertyIri),
                            O, generateURI1(B)));

            DistinctVariableOnlyDataAtom mappingProjectionAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);
            rdfAtomPredicate = (RDFAtomPredicate) mappingProjectionAtom.getPredicate();

            mappingBuilder.init(mappingProjectionAtom, mappingRootNode);
            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(binaryExtensionalAtom);
            mappingBuilder.addChild(mappingRootNode, extensionalDataNode);
            IQ mappingAssertion = IQ_CONVERTER.convert(mappingBuilder.build());
            propertyMapBuilder.put(propertyIri, mappingAssertion);
            LOGGER.info("Mapping assertion:\n" +mappingAssertion);
        }

        // Class
        IntermediateQueryBuilder mappingBuilder = createQueryBuilder(DB_METADATA);
        ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                        P, getConstantIRI(RDF.TYPE),
                        O, getConstantIRI(CLASS_1)));

        mappingBuilder.init(ATOM_FACTORY.getDistinctTripleAtom(S, P, O), mappingRootNode);
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(unaryExtensionalAtom);
        mappingBuilder.addChild(mappingRootNode, extensionalDataNode);
        IQ classMappingAssertion = IQ_CONVERTER.convert(mappingBuilder.build());
        ImmutableMap<IRI, IQ> classMap = ImmutableMap.of(CLASS_1, classMappingAssertion);
        LOGGER.info("Mapping assertion:\n" + classMappingAssertion);


        /*
         * Renaming
         */
        MappingMetadata mappingMetadata = MAPPING_FACTORY.createMetadata(MAPPING_FACTORY.createPrefixManager(ImmutableMap.of()),
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY));

        Mapping nonNormalizedMapping = MAPPING_FACTORY.createMapping(mappingMetadata,  transformIntoTable(
                propertyMapBuilder.build()), transformIntoTable(classMap));
        Mapping normalizedMapping = MAPPING_NORMALIZER.normalize(nonNormalizedMapping);

        /*
         * Test whether two mapping assertions share a variable
         */
        LOGGER.info("After renaming:");
        Set<Variable> variableUnion = new HashSet<>();

        // Properties
        for (IRI propertyIri : propertyIris){

            IQ mappingAssertion = normalizedMapping.getRDFPropertyDefinition(rdfAtomPredicate, propertyIri)
                    .orElseThrow(() -> new IllegalStateException("Test fail: missing mapping assertion "));

            LOGGER.info(mappingAssertion.toString());
            ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getProjectionAtom().getVariables();
            if(Stream.of(mappingAssertionVariables)
                    .anyMatch(variableUnion::contains)){
                fail();
                break;
            }
            variableUnion.addAll(mappingAssertionVariables);
            LOGGER.info("All variables thus far: "+variableUnion+"\n");
        }

        // Class
        IQ mappingAssertion = normalizedMapping.getRDFClassDefinition(rdfAtomPredicate, CLASS_1)
                .orElseThrow(() -> new IllegalStateException("Test fail: missing mapping assertion "));

        System.out.println(mappingAssertion);
        ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getProjectionAtom().getVariables();
        if(Stream.of(mappingAssertionVariables)
                .anyMatch(variableUnion::contains)){
            fail();
        }
        variableUnion.addAll(mappingAssertionVariables);
        LOGGER.info("All variables thus far: "+variableUnion+"\n");
    }

    @Test
    public void testTwoEqualVariablesInExtensionalTable() {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(C),
                        P, getConstantIRI(RDF.TYPE),
                        O, getConstantIRI(CLASS_1)));

        DataAtom<RelationPredicate> dataAtom = ATOM_FACTORY.getDataAtom(BROKER_PREDICATE, ImmutableList.of(C,Y,C));
        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(dataAtom);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, table1DataNode);

        IQ mappingAssertion = IQ_CONVERTER.convert(queryBuilder.build());
        LOGGER.info(mappingAssertion.toString());

        MappingMetadata mappingMetadata = MAPPING_FACTORY.createMetadata(MAPPING_FACTORY.createPrefixManager(ImmutableMap.of()),
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY));
        MAPPING_FACTORY.createMapping(mappingMetadata,  ImmutableTable.of(),
                transformIntoTable(ImmutableMap.of(CLASS_1, mappingAssertion))
        );
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_STR_1, argument);
    }

    /**
     *
     * Currently, we are wrapping IRI constants into an IRI function
     * TODO: stop this practise
     */
    private ImmutableFunctionalTerm getConstantIRI(IRI iri) {
        return TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantIRI(iri));
    }

    private static ImmutableTable<RDFAtomPredicate, IRI, IQ> transformIntoTable(ImmutableMap<IRI, IQ> map) {
        return map.entrySet().stream()
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate)e.getValue().getProjectionAtom().getPredicate(),
                        e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toTable());
    }
}
