package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
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

    private static final NamedRelationDefinition TABLE_AFFILIATED_WRITERS, TABLE_AUTHORS, TABLE_STUDENT, TABLE_ADDRESS;

    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable S = TERM_FACTORY.getVariable("s");
    private static final Variable P = TERM_FACTORY.getVariable("p");
    private static final Variable O = TERM_FACTORY.getVariable("o");

    private static final ImmutableList<Template.Component> URI_TEMPLATE_AUTHOR = Template.of("http://meraka/moss/exampleBooks.owl#author/", 0);
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STUDENT = Template.of("http://example.org/voc#uni1/student/", 0);
    private static final ImmutableList<Template.Component> URI_TEMPLATE_ADDRESS = Template.of("http://www.owl-ontologies.com/Ontology1207768242.owl#address-", 0);

    private static final IRI AUTHOR = RDF_FACTORY.createIRI("http://meraka/moss/exampleBooks.owl#Author");
    private static final IRI PERSON = RDF_FACTORY.createIRI("http://xmlns.com/foaf/0.1/Person");
    private static final IRI ADDRESS = RDF_FACTORY.createIRI("http://www.owl-ontologies.com/Ontology1207768242.owl#Address");


    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();
        DBTermType stringDBType = builder.getDBTypeFactory().getDBStringType();

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
                                IQ_FACTORY.createExtensionalDataNode(TABLE_STUDENT, ImmutableMap.of(0, C, 3, A,4, B))))), result.getQuery());
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

}
