package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.*;
import com.google.inject.Injector;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.TMappingProcessor;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;


public class PunningTest {


    private final static RelationPredicate company;

    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");

    private static Variable S = TERM_FACTORY.getVariable("s");
    private static Variable P = TERM_FACTORY.getVariable("p");
    private static Variable O = TERM_FACTORY.getVariable("o");

    private static final String IRI_TEMPLATE_1 = "http://example.org/company/{}";
    private static final IRI PROP_IRI = RDF_FACTORY.createIRI("http://example.org/voc#Company");
    private static final IRI CLASS_IRI = RDF_FACTORY.createIRI("http://example.org/voc#Company");


    static {

        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();
        DBTermType integerType = TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType();

        DatabaseRelationDefinition table24Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "company"));
        table24Def.addAttribute(idFactory.createAttributeID("cmpNpdidCompany"), integerType.getName(), integerType, false);
        table24Def.addAttribute(idFactory.createAttributeID("cmpShortName"), integerType.getName(), integerType, false);
        company = table24Def.getAtomPredicate();

        dbMetadata.freeze();
    }

    @Test
    public void test() {
                OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        TMappingProcessor tmap = injector.getInstance(TMappingProcessor.class);


        DataAtom<RelationPredicate> extensionalAtom = ATOM_FACTORY.getDataAtom(company, ImmutableList.of(A, B));

        // Class
        IQ classMappingAssertion = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                        SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                                P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                O, TERM_FACTORY.getConstantIRI(CLASS_IRI))),
                        IQ_FACTORY.createExtensionalDataNode(extensionalAtom)));
        ImmutableMap<IRI, IQ> classMap = ImmutableMap.of(CLASS_IRI, classMappingAssertion);

        // Property
        IQ propertyMappingAssertion = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S, P, B),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, B),
                        SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                                P, TERM_FACTORY.getConstantIRI(PROP_IRI))),
                        IQ_FACTORY.createExtensionalDataNode(extensionalAtom)));
        ImmutableMap<IRI, IQ> propertyMap = ImmutableMap.of(PROP_IRI, propertyMappingAssertion);


        Mapping mapping = SPECIFICATION_FACTORY.createMapping(SPECIFICATION_FACTORY.createMetadata(
                SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of())),
                transformIntoTable(propertyMap),
                transformIntoTable(classMap));

        OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY);
        builder.declareClass(CLASS_IRI);
        builder.declareDataProperty(PROP_IRI);
        Ontology ontology = builder.build();
        ClassifiedTBox tbox = ontology.tbox();

        LinearInclusionDependencies<AtomPredicate> lids = LinearInclusionDependencies.builder(CORE_UTILS_FACTORY, ATOM_FACTORY).build();

        Mapping result = tmap.getTMappings(mapping,
                tbox,
                new TMappingExclusionConfig(ImmutableSet.of(), ImmutableSet.of()),
                new ImmutableCQContainmentCheckUnderLIDs(lids));
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(IRI_TEMPLATE_1, ImmutableList.of(argument));
    }

    private static ImmutableTable<RDFAtomPredicate, IRI, IQ> transformIntoTable(ImmutableMap<IRI, IQ> map) {
        return map.entrySet().stream()
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate)e.getValue().getProjectionAtom().getPredicate(),
                        e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toTable());
    }

}
