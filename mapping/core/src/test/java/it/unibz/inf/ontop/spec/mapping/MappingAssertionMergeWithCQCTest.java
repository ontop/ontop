package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.MappingAssertionUnion;
import org.apache.commons.rdf.api.IRI;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static it.unibz.inf.ontop.utils.MappingTestingTools.TERM_FACTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MappingAssertionMergeWithCQCTest {

    private static final NamedRelationDefinition TABLE_AFFILIATED_WRITERS, TABLE_AUTHORS, TABLE_STUDENT, TABLE_ADDRESS, MEASUREMENT, MEASUREMENT_TYPE;

    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable S = TERM_FACTORY.getVariable("s");
    private static final Variable P = TERM_FACTORY.getVariable("p");
    private static final Variable O = TERM_FACTORY.getVariable("o");

    private static final Variable V0 = TERM_FACTORY.getVariable("v0");
    private static final Variable V1 = TERM_FACTORY.getVariable("v1");

    private static final Variable ID = TERM_FACTORY.getVariable("id");
    private static final Variable NAME = TERM_FACTORY.getVariable("name");


    private static final ImmutableList<Template.Component> URI_TEMPLATE_AUTHOR = Template.builder().string("http://meraka/moss/exampleBooks.owl#author/").placeholder().build();
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STUDENT = Template.builder().string("http://example.org/voc#uni1/student/").placeholder().build();
    private static final ImmutableList<Template.Component> URI_TEMPLATE_ADDRESS = Template.builder().string("http://www.owl-ontologies.com/Ontology1207768242.owl#address-").placeholder().build();

    private static final ImmutableList<Template.Component> URI_TEMPLATE_RESULT = Template.builder().string("http://example.org/weather/observation/result/").placeholder().build();

    private static final IRI AUTHOR = RDF_FACTORY.createIRI("http://meraka/moss/exampleBooks.owl#Author");
    private static final IRI PERSON = RDF_FACTORY.createIRI("http://xmlns.com/foaf/0.1/Person");
    private static final IRI ADDRESS = RDF_FACTORY.createIRI("http://www.owl-ontologies.com/Ontology1207768242.owl#Address");

    private static final IRI PROP_UNIT = RDF_FACTORY.createIRI("http://example.org/qudt/unit");
    private static final IRI DEGREES_CELCIUS = RDF_FACTORY.createIRI("http://example.org/qudt-unit/DegreeCelsius");
    private static final IRI METERS_PER_SECOND = RDF_FACTORY.createIRI("http://example.org/qudt-unit/MeterPerSecond");


    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();
        DBTermType stringDBType = builder.getDBTypeFactory().getDBStringType();
        DBTermType largeIntDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE_AUTHORS = builder.createDatabaseRelation("tb_authors",
                "bk_code", integerDBType, false,
                "wr_id", integerDBType, false);

        TABLE_AFFILIATED_WRITERS = builder.createDatabaseRelation("tb_affiliated_writers",
                "wr_code", integerDBType, false);
        ForeignKeyConstraint.builder("FK", TABLE_AFFILIATED_WRITERS, TABLE_AUTHORS)
                .add(1, 2)
                .build();

        TABLE_STUDENT = builder.createDatabaseRelation("student",
                "id", integerDBType, false,
                "first_name", stringDBType, false,
                "last_name", stringDBType, false,
                "ssn", stringDBType, true,
                "kind", integerDBType, false);

        TABLE_ADDRESS = builder.createDatabaseRelation("ADDRESS",
                "ID", integerDBType, false,
                "STREET", stringDBType, true);

        MEASUREMENT = builder.createDatabaseRelation("source3_weather_measurement",
                "id", largeIntDBType, false,
                "name", largeIntDBType, false);

        MEASUREMENT_TYPE = builder.createDatabaseRelation("source3_measurement_types",
                "name", largeIntDBType, false,
                "unit", largeIntDBType, false);

    }


    @Test
    public void testRedundancyDueToForeignKey() {

        IQ authorIQ = getAuthorsIQ(ImmutableMap.of(0, A, 1, C));
        IQ affiliatedWriterIQ = getAffiliatedWritersIQ(ImmutableMap.of(0, C));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(authorIQ, null));
        entry.add(new MappingAssertion(affiliatedWriterIQ, null));
        MappingAssertion result = entry.build().get();
        assertEquals(getAuthorsIQ(ImmutableMap.of(1, C)), result.getQuery());
    }

    @Test
    public void testRedundancyDueToForeignKeyInReverseOrder() {

        IQ authorIQ = getAuthorsIQ(ImmutableMap.of(0, A, 1, C));
        IQ affiliatedWriterIQ = getAffiliatedWritersIQ(ImmutableMap.of(0, C));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(affiliatedWriterIQ, null));
        entry.add(new MappingAssertion(authorIQ, null));
        MappingAssertion result = entry.build().get();
        assertEquals(getAuthorsIQ(ImmutableMap.of(1, C)), result.getQuery());
    }

    private IQ getAuthorsIQ(ImmutableMap<Integer, VariableOrGroundTerm> argumentMap) {
        return IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_AUTHOR, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(AUTHOR))),
                        IQ_FACTORY.createExtensionalDataNode(
                                TABLE_AUTHORS, argumentMap)));
    }

    private IQ getAffiliatedWritersIQ(ImmutableMap<Integer, VariableOrGroundTerm> argumentMap) {
        return IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_AUTHOR, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(AUTHOR))),
                        IQ_FACTORY.createExtensionalDataNode(
                                TABLE_AFFILIATED_WRITERS, argumentMap)));
    }

    @Test
    public void testRedundancyDueToDifferentArgumentMaps() {

        IQ studentIQ1 = getStudentIQ(ImmutableMap.of(0, C, 1, A));
        IQ studentIQ2 = getStudentIQ(ImmutableMap.of(0, C, 2, B));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(getStudentIQ(ImmutableMap.of(0, C)), result.getQuery());
    }

    @Test
    public void testRedundancyDueToDifferentArgumentMapsReverseOrder() {

        IQ studentIQ1 = getStudentIQ(ImmutableMap.of(0, C, 1, A));
        IQ studentIQ2 = getStudentIQ(ImmutableMap.of(0, C, 2, B));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ2, null));
        entry.add(new MappingAssertion(studentIQ1, null));
        MappingAssertion result = entry.build().get();
        assertEquals(getStudentIQ(ImmutableMap.of(0, C)), result.getQuery());
    }

    private IQ getStudentIQ(ImmutableMap<Integer, VariableOrGroundTerm> arguments) {
        return IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, arguments)));
    }

    @Test
    public void testRedundancyDueToAdditionalFilter() {

        IQ addressIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_ADDRESS, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(ADDRESS))),
                        IQ_FACTORY.createExtensionalDataNode(TABLE_ADDRESS, ImmutableMap.of(0, C))));

        IQ addressIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_ADDRESS, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(ADDRESS))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_ADDRESS, ImmutableMap.of(0, C, 1, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(addressIQ1, null));
        entry.add(new MappingAssertion(addressIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(addressIQ1, result.getQuery());
    }

    @Test
    public void testRedundancyDueToAdditionalFilterReverseOrder() {

        IQ addressIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_ADDRESS, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(ADDRESS))),
                        IQ_FACTORY.createExtensionalDataNode(TABLE_ADDRESS, ImmutableMap.of(0, C))));

        IQ addressIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_ADDRESS, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(ADDRESS))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_ADDRESS, ImmutableMap.of(0, C, 1, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(addressIQ2, null));
        entry.add(new MappingAssertion(addressIQ1, null));
        MappingAssertion result = entry.build().get();
        assertEquals(addressIQ1, result.getQuery());
    }

    @Test
    public void testRedundancyDueToRedundantJoin() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(B)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBIsNotNull(B)),
                                ImmutableList.of(IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 1, A)),
                                        IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, B))))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(B)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(B)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(3, B))))), result.getQuery());
    }


    @Disabled
    @Test
    public void testRedundancyDueToRedundantJoinWrongProjection() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBIsNotNull(B)),
                                ImmutableList.of(IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 1, A)),
                                        IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, B))))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(B)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(3, B))))), result.getQuery());
    }


    @Test
    public void testRedundancyDueToAdditionalFilters() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(3)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(3)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B))))), result.getQuery());
    }

    @Test
    public void testRedundancyDueToAdditionalFiltersReverseOrder() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(3)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ2, null));
        entry.add(new MappingAssertion(studentIQ1, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(3)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B))))), result.getQuery());
    }


    @Test
    public void testFilterMerge() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getConjunction(TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1))),
                                                TERM_FACTORY.getConjunction(TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(6))),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(4)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, A, 4, B)))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                                TERM_FACTORY.getConjunction(TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2))),
                                                TERM_FACTORY.getConjunction(TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(5))),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(3)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, A, 4, B)))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(3)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(4)),
                                                TERM_FACTORY.getConjunction(TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(5))),
                                                TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                                TERM_FACTORY.getConjunction(TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getStrictEquality(B, TERM_FACTORY.getDBIntegerConstant(6))))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, A, 4, B))))), result.getQuery());
    }


    @Test
    public void testRedundancyDueToValuesNodes() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2))))))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(3))))))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(studentIQ2, result.getQuery());
    }

    @Test
    public void testRedundancyDueToValuesNodesReverseOrder() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2))))))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(3))))))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ2, null));
        entry.add(new MappingAssertion(studentIQ1, null));
        MappingAssertion result = entry.build().get();
        assertEquals(studentIQ2, result.getQuery());
    }

    @Test
    public void testMergeValuesNodes() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(4))))))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(3))))))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ1, null));
        entry.add(new MappingAssertion(studentIQ2, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(3)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(4)))))))), result.getQuery());
    }

    @Test
    public void testMergeValuesNodesReverseOrder() {

        IQ studentIQ1 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(4))))))));

        IQ studentIQ2 = IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(3))))))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(studentIQ2, null));
        entry.add(new MappingAssertion(studentIQ1, null));
        MappingAssertion result = entry.build().get();
        assertEquals(IQ_FACTORY.createIQ(ATOM_FACTORY.getDistinctTripleAtom(S, P, O),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STUDENT, ImmutableList.of(C)),
                                        P, TERM_FACTORY.getConstantIRI(RDF.TYPE),
                                        O, TERM_FACTORY.getConstantIRI(PERSON))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 4, B)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(B), ImmutableList.of(
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(1)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(4)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(2)),
                                        ImmutableMap.of(B, TERM_FACTORY.getDBIntegerConstant(3)))))))), result.getQuery());
    }


    @Test
    public void testMergeWithConstantPropertyValues() {

        DistinctVariableOnlyDataAtom spoAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, O);

        //  data:weather/observation/result/{id} qudt:unit qudt-unit:DegreeCelsius .
        //  select m.id from "source3_weather_measurement" m, "source3_measurement_types" t
        //          WHERE m.name = t.name and t.unit = 'C'
        IQ mDegreeCelsius = IQ_FACTORY.createIQ(spoAtom,
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_RESULT, ImmutableList.of(ID)),
                                        P, TERM_FACTORY.getConstantIRI(PROP_UNIT),
                                        O, TERM_FACTORY.getConstantIRI(DEGREES_CELCIUS))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT, ImmutableMap.of(0, ID, 1, NAME)),
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT_TYPE, ImmutableMap.of(0, NAME, 1, TERM_FACTORY.getDBStringConstant("C")))))));

        //  data:weather/observation/result/{id} qudt:unit qudt-unit:MeterPerSecond .
        //  select m.id from "source3_weather_measurement" m, "source3_measurement_types" t
        //          WHERE m.name = t.name and t.unit = 'm/s'
        IQ mMeterPerSecond = IQ_FACTORY.createIQ(spoAtom,
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_RESULT, ImmutableList.of(ID)),
                                        P, TERM_FACTORY.getConstantIRI(PROP_UNIT),
                                        O, TERM_FACTORY.getConstantIRI(METERS_PER_SECOND))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT, ImmutableMap.of(0, ID, 1, NAME)),
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT_TYPE, ImmutableMap.of(0, NAME, 1, TERM_FACTORY.getDBStringConstant("m/s")))))));

        //  data:weather/observation/result/{id} qudt:unit qudt-unit:MeterPerSecond .
        //  select m.id from "source3_weather_measurement" m, "source3_measurement_types" t
        //          WHERE m.name = t.name and t.unit = '[m/s]'
        IQ mMeterPerSecond2 = IQ_FACTORY.createIQ(spoAtom,
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_RESULT, ImmutableList.of(ID)),
                                        P, TERM_FACTORY.getConstantIRI(PROP_UNIT),
                                        O, TERM_FACTORY.getConstantIRI(METERS_PER_SECOND))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT, ImmutableMap.of(0, ID, 1, NAME)),
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT_TYPE, ImmutableMap.of(0, NAME, 1, TERM_FACTORY.getDBStringConstant("[m/s]")))))));

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(HOMOMORPHISM_FACTORY, CORE_UTILS_FACTORY);
        MappingAssertionUnion entry = new MappingAssertionUnion(cqc, CORE_SINGLETONS, UNION_BASED_QUERY_MERGER);
        entry.add(new MappingAssertion(mDegreeCelsius, null));
        entry.add(new MappingAssertion(mMeterPerSecond, null));
        entry.add(new MappingAssertion(mMeterPerSecond2, null));
        MappingAssertion result = entry.build().get();

        assertEquals(IQ_FACTORY.createIQ(spoAtom,
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        S, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_RESULT, ImmutableList.of(ID)),
                                        P, TERM_FACTORY.getConstantIRI(PROP_UNIT),
                                        O, TERM_FACTORY.getIRIFunctionalTerm(V0))),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT, ImmutableMap.of(0, ID, 1, NAME)),
                                IQ_FACTORY.createExtensionalDataNode(
                                        MEASUREMENT_TYPE, ImmutableMap.of(0, NAME, 1, V1)),
                                IQ_FACTORY.createValuesNode(ImmutableSet.of(V0, V1), ImmutableList.of(
                                        ImmutableMap.of(V0, TERM_FACTORY.getDBStringConstant("http://example.org/qudt-unit/MeterPerSecond"), V1, TERM_FACTORY.getDBStringConstant("[m/s]")),
                                        ImmutableMap.of(V0, TERM_FACTORY.getDBStringConstant("http://example.org/qudt-unit/MeterPerSecond"), V1, TERM_FACTORY.getDBStringConstant("m/s")),
                                        ImmutableMap.of(V0, TERM_FACTORY.getDBStringConstant("http://example.org/qudt-unit/DegreeCelsius"), V1, TERM_FACTORY.getDBStringConstant("C")))))))),
                result.getQuery());

    }
}