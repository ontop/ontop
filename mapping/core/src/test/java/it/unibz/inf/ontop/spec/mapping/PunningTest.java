package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.*;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.TMappingSaturatorImpl;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;


public class PunningTest {


    private final static RelationDefinition company;

    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");

    private static Variable S = TERM_FACTORY.getVariable("s");
    private static Variable P = TERM_FACTORY.getVariable("p");
    private static Variable O = TERM_FACTORY.getVariable("o");

    private static final ImmutableList<Template.Component> IRI_TEMPLATE_1 = Template.of("http://example.org/company/", 0);
    private static final IRI PROP_IRI = RDF_FACTORY.createIRI("http://example.org/voc#Company");
    private static final IRI CLASS_IRI = RDF_FACTORY.createIRI("http://example.org/voc#Company");

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        company = builder.createDatabaseRelation( "company",
            "cmpNpdidCompany", integerDBType, false,
            "cmpShortName", integerDBType, false);
    }

    @Test
    public void test() {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        TMappingSaturatorImpl tmap = injector.getInstance(TMappingSaturatorImpl.class);

        // Class
        IQ classMappingAssertion = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                        SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                                P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                O, TERM_FACTORY.getConstantIRI(CLASS_IRI))),
                        IQ_FACTORY.createExtensionalDataNode(company, ImmutableMap.of(0, A, 1, B))));

        // Property
        IQ propertyMappingAssertion = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S, P, B),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, B),
                        SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                                P, TERM_FACTORY.getConstantIRI(PROP_IRI))),
                        IQ_FACTORY.createExtensionalDataNode(company, ImmutableMap.of(0, A, 1, B))));

        RDFAtomPredicate tp = (RDFAtomPredicate) ATOM_FACTORY.getDistinctTripleAtom(S, P, B).getPredicate();

        ImmutableList<MappingAssertion> mapping = ImmutableList.of(
                new MappingAssertion(MappingAssertionIndex.ofProperty(tp, PROP_IRI), propertyMappingAssertion, null),
                new MappingAssertion(MappingAssertionIndex.ofClass(tp, CLASS_IRI), classMappingAssertion, null));

        OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY, TERM_FACTORY);
        builder.declareClass(CLASS_IRI);
        builder.declareDataProperty(PROP_IRI);
        Ontology ontology = builder.build();
        ClassifiedTBox tbox = ontology.tbox();

        ImmutableList<MappingAssertion> result = tmap.saturate(mapping, tbox);
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(IRI_TEMPLATE_1, ImmutableList.of(argument));
    }
}
