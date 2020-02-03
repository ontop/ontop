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
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String URI_TEMPLATE_STR_1;

    private static final IRI PROP_1, PROP_2, CLASS_1;

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DBTermType integerDBType = TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p1"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), integerDBType.getName(), integerDBType, false);
        table1Def.addAttribute(idFactory.createAttributeID("col12"), integerDBType.getName(), integerDBType, false);
        P1_PREDICATE = table1Def.getAtomPredicate();

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p3"));
        table3Def.addAttribute(idFactory.createAttributeID("col31"), integerDBType.getName(), integerDBType, false);
        P3_PREDICATE = table3Def.getAtomPredicate();

        DatabaseRelationDefinition table4Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p4"));
        table4Def.addAttribute(idFactory.createAttributeID("col41"), integerDBType.getName(), integerDBType, false);
        P4_PREDICATE = table4Def.getAtomPredicate();

        DatabaseRelationDefinition table5Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p5"));
        table5Def.addAttribute(idFactory.createAttributeID("col51"), integerDBType.getName(), integerDBType, false);
        P5_PREDICATE = table5Def.getAtomPredicate();


        DatabaseRelationDefinition tableBrokerDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID("DB2INST1", "brokerworksfor"));
        tableBrokerDef.addAttribute(idFactory.createAttributeID("broker"), integerDBType.getName(), integerDBType, false);
        tableBrokerDef.addAttribute(idFactory.createAttributeID("company"), integerDBType.getName(), integerDBType, true);
        tableBrokerDef.addAttribute(idFactory.createAttributeID("client"), integerDBType.getName(), integerDBType, true);
        BROKER_PREDICATE = tableBrokerDef.getAtomPredicate();

        URI_TEMPLATE_STR_1 =  "http://example.org/person/{}";

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
        final RDFAtomPredicate tp = rdfAtomPredicate;
        MappingInTransformation nonNormalizedMapping = MAPPING_FACTORY.createMapping(Stream.concat(
                propertyMapBuilder.build().entrySet().stream()
                        .map(e -> Maps.immutableEntry(
                                new MappingAssertionIndex(tp, e.getKey(), false), e.getValue())),
                classMap.entrySet().stream()
                        .map(e -> Maps.immutableEntry(
                                new MappingAssertionIndex(tp, e.getKey(), true), e.getValue())))
                .collect(ImmutableCollectors.toMap()));
        MappingInTransformation normalizedMapping = MAPPING_NORMALIZER.normalize(nonNormalizedMapping);

        /*
         * Test whether two mapping assertions share a variable
         */
        LOGGER.info("After renaming:");
        Set<Variable> variableUnion = new HashSet<>();

        // Properties
        for (IRI propertyIri : propertyIris){

            IQ mappingAssertion = normalizedMapping.getAssertion(new MappingAssertionIndex(rdfAtomPredicate, propertyIri, false))
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
        IQ mappingAssertion = normalizedMapping.getAssertion(new MappingAssertionIndex(rdfAtomPredicate, CLASS_1, true))
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

        RDFAtomPredicate tp = (RDFAtomPredicate)projectionAtom.getPredicate();
        MAPPING_FACTORY.createMapping(ImmutableMap.of(new MappingAssertionIndex(tp, CLASS_1, true), mappingAssertion));
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private IRIConstant getConstantIRI(IRI iri) {
        return TERM_FACTORY.getConstantIRI(iri);
    }
}
