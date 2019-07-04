package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.*;
import com.google.inject.Injector;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.TMappingProcessor;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;

import java.sql.Types;
import java.util.AbstractMap;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertEquals;


public class PunningTest {


    private final static RelationPredicate company;
    private final static AtomPredicate ANS1_VAR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static Variable cmpShare1 = TERM_FACTORY.getVariable("cmpShare1");
    private final static Variable fldNpdidField1 = TERM_FACTORY.getVariable("fldNpdidField1");
    private final static Variable cmpNpdidCompany2 = TERM_FACTORY.getVariable("cmpNpdidCompany2");
    private final static Variable cmpShortName2 = TERM_FACTORY.getVariable("cmpShortName2");

    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");

    private static Variable S = TERM_FACTORY.getVariable("s");
    private static Variable P = TERM_FACTORY.getVariable("p");
    private static Variable O = TERM_FACTORY.getVariable("o");

    private static final Constant URI_TEMPLATE_STR_1 = TERM_FACTORY.getConstantLiteral("http://example.org/company/{}");
    private static final IRI PROP_IRI = RDF_FACTORY.createIRI("http://example.org/voc#Company");
    private static final IRI CLASS_IRI = RDF_FACTORY.createIRI("http://example.org/voc#Company");


    static {

        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table24Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "company"));
        table24Def.addAttribute(idFactory.createAttributeID("cmpNpdidCompany"), Types.INTEGER, null, false);
        table24Def.addAttribute(idFactory.createAttributeID("cmpShortName"), Types.INTEGER, null, false);
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
                                P, getConstantIRI(RDF.TYPE),
                                O, getConstantIRI(CLASS_IRI))),
                        IQ_FACTORY.createExtensionalDataNode(extensionalAtom)));
        ImmutableMap<IRI, IQ> classMap = ImmutableMap.of(CLASS_IRI, classMappingAssertion);

        // Property
        IQ propertyMappingAssertion = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S, P, B),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, B),
                        SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A),
                                P, getConstantIRI(PROP_IRI))),
                        IQ_FACTORY.createExtensionalDataNode(extensionalAtom)));
        ImmutableMap<IRI, IQ> propertyMap = ImmutableMap.of(PROP_IRI, propertyMappingAssertion);


        Mapping mapping = SPECIFICATION_FACTORY.createMapping(SPECIFICATION_FACTORY.createMetadata(
                SPECIFICATION_FACTORY.createPrefixManager(ImmutableMap.of()),
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY)),
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
