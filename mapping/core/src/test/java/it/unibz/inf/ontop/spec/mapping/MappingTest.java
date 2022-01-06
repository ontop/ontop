package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
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

    private static final RelationDefinition P1;
    private static final RelationDefinition P3;
    private static final RelationDefinition P4;
    private static final RelationDefinition P5;
    private static final RelationDefinition BROKER;

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingTest.class);

    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable S = TERM_FACTORY.getVariable("s");
    private static final Variable P = TERM_FACTORY.getVariable("p");
    private static final Variable O = TERM_FACTORY.getVariable("o");

    private final static Variable Y = TERM_FACTORY.getVariable("company");

    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/person/", 0);

    private static final IRI PROP_1, PROP_2, CLASS_1;

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        P1 = builder.createDatabaseRelation("p1",
            "col1", integerDBType, false,
            "col12", integerDBType, false);

        P3 = builder.createDatabaseRelation("p3",
            "col31", integerDBType, false);

        P4 = builder.createDatabaseRelation("p4",
            "col41", integerDBType, false);

        P5 = builder.createDatabaseRelation("p5",
            "col51", integerDBType, false);

        BROKER = builder.createDatabaseRelation("brokerworksfor",
            "broker", integerDBType, false,
            "company", integerDBType, true,
            "client", integerDBType, true);

        PROP_1 = RDF_FACTORY.createIRI("http://example.org/voc#Prop1");
        PROP_2 = RDF_FACTORY.createIRI("http://example.org/voc#Prop2");
        CLASS_1 = RDF_FACTORY.createIRI("http://example.org/voc#Class1");
    }

    @Test
    public void testOfflineMappingAssertionsRenaming() {

        ImmutableList<IRI> propertyIris = ImmutableList.of(PROP_1, PROP_2);

        ImmutableMap.Builder<IRI, IQ> propertyMapBuilder = ImmutableMap.builder();
        RDFAtomPredicate rdfAtomPredicate = null;

        // Properties
        for (IRI propertyIri : propertyIris) {
            ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                    SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                            P, getConstantIRI(propertyIri),
                            O, generateURI1(B)));

            DistinctVariableOnlyDataAtom mappingProjectionAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);
            rdfAtomPredicate = (RDFAtomPredicate) mappingProjectionAtom.getPredicate();

            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(P1, ImmutableMap.of(0, A, 1, B));
            IQ mappingAssertion = IQ_FACTORY.createIQ(mappingProjectionAtom, IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode));
            propertyMapBuilder.put(propertyIri, mappingAssertion);
            LOGGER.info("Mapping assertion:\n" + mappingAssertion);
        }

        // Class
        ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                        P, getConstantIRI(RDF.TYPE),
                        O, getConstantIRI(CLASS_1)));

        DistinctVariableOnlyDataAtom mappingProjectionAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(P3, ImmutableMap.of(0, A));
        IQ classMappingAssertion = IQ_FACTORY.createIQ(mappingProjectionAtom, IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode));
        ImmutableMap<IRI, IQ> classMap = ImmutableMap.of(CLASS_1, classMappingAssertion);
        LOGGER.info("Mapping assertion:\n" + classMappingAssertion);


        /*
         * Renaming
         */
        final RDFAtomPredicate tp = rdfAtomPredicate;
        ImmutableList<MappingAssertion> nonNormalizedMapping = Stream.concat(
                propertyMapBuilder.build().entrySet().stream()
                        .map(e -> Maps.immutableEntry(
                                MappingAssertionIndex.ofProperty(tp, e.getKey()), e.getValue())),
                classMap.entrySet().stream()
                        .map(e -> Maps.immutableEntry(
                                MappingAssertionIndex.ofClass(tp, e.getKey()), e.getValue())))
                .collect(ImmutableCollectors.toMap()).entrySet().stream()
                .map(e -> new MappingAssertion(e.getKey(), e.getValue(), null))
                .collect(ImmutableCollectors.toList());
        ImmutableMap<MappingAssertionIndex, IQ> normalizedMapping = MAPPING_NORMALIZER.normalize(nonNormalizedMapping).stream()
                .collect(ImmutableCollectors.toMap(MappingAssertion::getIndex, MappingAssertion::getQuery));

        /*
         * Test whether two mapping assertions share a variable
         */
        LOGGER.info("After renaming:");
        Set<Variable> variableUnion = new HashSet<>();

        // Properties
        for (IRI propertyIri : propertyIris) {

            IQ mappingAssertion = normalizedMapping.get(MappingAssertionIndex.ofProperty(rdfAtomPredicate, propertyIri));

            LOGGER.info(mappingAssertion.toString());
            ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getProjectionAtom().getVariables();
            if (Stream.of(mappingAssertionVariables)
                    .anyMatch(variableUnion::contains)){
                fail();
                break;
            }
            variableUnion.addAll(mappingAssertionVariables);
            LOGGER.info("All variables thus far: "+variableUnion+"\n");
        }

        // Class
        IQ mappingAssertion = normalizedMapping.get(MappingAssertionIndex.ofClass(rdfAtomPredicate, CLASS_1));

        System.out.println(mappingAssertion);
        ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getProjectionAtom().getVariables();
        if(Stream.of(mappingAssertionVariables)
                .anyMatch(variableUnion::contains)) {
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

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(
                BROKER, ImmutableMap.of(0, C,1, Y,2, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);

        IQ mappingAssertion = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode, table1DataNode));
        LOGGER.info(mappingAssertion.toString());

//        RDFAtomPredicate tp = (RDFAtomPredicate)projectionAtom.getPredicate();
//        ImmutableMap.of(MappingAssertionIndex.ofClass(tp, CLASS_1), mappingAssertion);
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private IRIConstant getConstantIRI(IRI iri) {
        return TERM_FACTORY.getConstantIRI(iri);
    }
}
