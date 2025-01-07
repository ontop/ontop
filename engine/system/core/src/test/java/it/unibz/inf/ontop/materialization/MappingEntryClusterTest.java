package it.unibz.inf.ontop.materialization;

import static it.unibz.inf.ontop.utils.MaterializationTestingTools.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.materialization.impl.*;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBOrFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

public class MappingEntryClusterTest {
    private final static DBTypeFactory dbTypeFactory;
    private final static NamedRelationDefinition T1;
    private final static NamedRelationDefinition T2;
    private final static NamedRelationDefinition T3;

    static {
        final OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        dbTypeFactory = builder.getDBTypeFactory();

        T1 = builder.createDatabaseRelation("person",
                "id", dbTypeFactory.getDBStringType(), false,
                "name", dbTypeFactory.getDBStringType(), false,
                "age", dbTypeFactory.getDBLargeIntegerType(), true,
                "works_at", dbTypeFactory.getDBStringType(), true);

        T2 = builder.createDatabaseRelation("shop",
                "id", dbTypeFactory.getDBStringType(), false,
                "name", dbTypeFactory.getDBStringType(), false,
                "type", dbTypeFactory.getDBStringType(), false);

        T3 = builder.createDatabaseRelation("owner",
                "id", dbTypeFactory.getDBStringType(), false,
                "person_id", dbTypeFactory.getDBStringType(), false,
                "owns", dbTypeFactory.getDBStringType(), true);

        UniqueConstraint.primaryKeyOf(T1.getAttribute(1));
        UniqueConstraint.primaryKeyOf(T2.getAttribute(1));
        UniqueConstraint.primaryKeyOf(T3.getAttribute(1));
        ForeignKeyConstraint.of("works_at_fk", T1.getAttribute(4), T2.getAttribute(1));
        ForeignKeyConstraint.of("owns_fk", T3.getAttribute(3), T2.getAttribute(1));
        ForeignKeyConstraint.of("person_fk", T3.getAttribute(2), T1.getAttribute(1));
    }
    private final Variable ID1 = TERM_FACTORY.getVariable("id1");
    private final Variable NAME1 = TERM_FACTORY.getVariable("name1");
    private final Variable AGE1 = TERM_FACTORY.getVariable("age1");
    private final Variable ID2 = TERM_FACTORY.getVariable("id2");
    private final Variable NAME2 = TERM_FACTORY.getVariable("name2");
    private final Variable AGE2 = TERM_FACTORY.getVariable("age2");
    private final Variable ID3 = TERM_FACTORY.getVariable("id3");
    private final Variable S1 = TERM_FACTORY.getVariable("s1");
    private final Variable P1 = TERM_FACTORY.getVariable("p1");
    private final Variable O1 = TERM_FACTORY.getVariable("o1");
    private final Variable G1 = TERM_FACTORY.getVariable("g1");
    private final Variable S2 = TERM_FACTORY.getVariable("s2");
    private final Variable P2 = TERM_FACTORY.getVariable("p2");
    private final Variable O2 = TERM_FACTORY.getVariable("o2");

    private final IRI RDF_TYPE_PROP = RDF_FACTORY.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private final IRI NAME_PROP = RDF_FACTORY.createIRI("http://example.org/name");
    private final IRI AGE_PROP = RDF_FACTORY.createIRI("http://example.org/age");
    private final IRI BAR_PROP = RDF_FACTORY.createIRI("http://example.org/bar");
    private final IRI RESTAURANT_PROP = RDF_FACTORY.createIRI("http://example.org/restaurant");
    private final IRI WORKS_PROP = RDF_FACTORY.createIRI("http://example.org/works_at");

    private static final ImmutableList<Template.Component> PERSON_URI_TEMPLATE = Template.builder()
            .string("http://example.org/person/")
            .placeholder()
            .build();
    private final ImmutableList<Template.Component> SHOP_URI_TEMPLATE = Template.builder()
            .string("http://example.org/shop/")
            .placeholder()
            .build();

    @Test
    public void simpleAssertionMergeTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq1 = IQ_FACTORY.createIQ(
                    ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                    IQ_FACTORY.createUnaryIQTree(constructionNode1, extensionalNode1));
        MappingEntryCluster assertion1 = new SimpleMappingEntryCluster(
                iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID2, 2, AGE2));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generatePersonURI(PERSON_URI_TEMPLATE, ID2),
                        P2, getConstantIRI(AGE_PROP),
                        O2, getRDFLiteral(AGE2)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, extensionalNode2));
        MappingEntryCluster assertion2 = new SimpleMappingEntryCluster(
                iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent();

    }

    @Test
    public void conflictingVariablesInRelationTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, extensionalNode1));
        MappingEntryCluster assertion1 = new SimpleMappingEntryCluster(
                iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, AGE1));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(AGE_PROP),
                        O1, getRDFLiteral(AGE1)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, extensionalNode2));
        MappingEntryCluster assertion2 = new SimpleMappingEntryCluster(
                iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        MappingEntryCluster mergedAssertion = assertion1.merge(assertion2).get();
        assert ((ConstructionNode) mergedAssertion.getIQTree().getRootNode()).getSubstitution().getRangeSet()
                .equals(ImmutableSet.of(generatePersonURI(PERSON_URI_TEMPLATE, ID1), getConstantIRI(NAME_PROP), getConstantIRI(AGE_PROP), getRDFLiteral(NAME1)));
    }

    @Test
    public void simplifyNotNullFilterTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode nameFilter = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(nameFilter, extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingEntryCluster info = new FilterMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);
        assert !hasFilterNode(info.getIQTree());

    }

    @Test
    public void filterOnNotProjectedVariableTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode ageFilter = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(AGE1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(ageFilter, extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingEntryCluster info = new FilterMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        assert hasFilterNode(info.getIQTree());

    }

    @Test
    public void simplifyNotNullConjunctionFilterTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        var conjunctionCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBIsNotNull(ID1),
                TERM_FACTORY.getDBIsNotNull(NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(conjunctionCondition), extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingEntryCluster info = new FilterMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        assert !hasFilterNode(info.getIQTree());
    }

    @Test
    @Ignore
    public void simplifyNotNullConcatFilterTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        var conjunctionCondition = TERM_FACTORY.getDBIsNotNull(
                TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(AGE1, NAME1)));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(conjunctionCondition), extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingEntryCluster info = new FilterMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        assert !hasFilterNode(info.getIQTree());
    }

    @Test
    public void mergeOnNonCompatibleFiltersTest() {
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode ageFilter1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE1,
                TERM_FACTORY.getDBConstant("18", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode nameTypeConstructionNode = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, getRDFLiteral(NAME1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(NAME_PROP)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(ageFilter1, extensionalDataNode);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(nameTypeConstructionNode, childTree));

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID2, 1, NAME2, 2, AGE2));
        FilterNode ageFilter2 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE2,
                TERM_FACTORY.getDBConstant("20", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generatePersonURI(PERSON_URI_TEMPLATE, ID2),
                        P2, getConstantIRI(NAME_PROP),
                        O2, getRDFLiteral(NAME2)));
        IQTree childTree2 = IQ_FACTORY.createUnaryIQTree(ageFilter2, extensionalNode2);
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, childTree2));

        MappingEntryCluster assertion1 = new FilterMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        MappingEntryCluster assertion2 = new FilterMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    @Test
    public void mergeOnCompatibleFiltersTest() {
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode ageFilter1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE1,
                TERM_FACTORY.getDBConstant("18", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode nameTypeConstructionNode = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, getRDFLiteral(NAME1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(NAME_PROP)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(ageFilter1, extensionalDataNode);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(nameTypeConstructionNode, childTree));

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID2, 1, NAME2, 2, AGE2));
        FilterNode ageFilter2 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE2,
                TERM_FACTORY.getDBConstant("18", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generatePersonURI(PERSON_URI_TEMPLATE, ID2),
                        P2, getConstantIRI(NAME_PROP),
                        O2, getRDFLiteral(NAME2)));
        IQTree childTree2 = IQ_FACTORY.createUnaryIQTree(ageFilter2, extensionalNode2);
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, childTree2));

        MappingEntryCluster assertion1 = new FilterMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        MappingEntryCluster assertion2 = new FilterMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent();
    }

    @Test
    public void simplifyImplicitEqualityTest() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID1, 1, NAME1,
                2, TERM_FACTORY.getDBConstant("Bar", dbTypeFactory.getDBStringType())));
        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(SHOP_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(BAR_PROP)));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, ext1));

        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2,
                2, TERM_FACTORY.getDBConstant("Restaurant", dbTypeFactory.getDBStringType())));
        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generatePersonURI(SHOP_URI_TEMPLATE, ID2),
                        P2, getConstantIRI(RDF_TYPE_PROP),
                        O2, getConstantIRI(RESTAURANT_PROP)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constr2, ext2));

        MappingEntryCluster assertion1 = new DictionaryPatternMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY,
                SUBSTITUTION_FACTORY,
                TERM_FACTORY);
        MappingEntryCluster assertion2 = new DictionaryPatternMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY,
                SUBSTITUTION_FACTORY,
                TERM_FACTORY);
        assert assertion1.merge(assertion2).isPresent();
    }

    @Test
    public void simplifyImplicitEqualityConjunctionTest() {
        ExtensionalDataNode ext = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2,
                1, TERM_FACTORY.getDBConstant("Name", dbTypeFactory.getDBStringType()),
                2, TERM_FACTORY.getDBConstant("Restaurant", dbTypeFactory.getDBStringType())));
        ConstructionNode constr = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generatePersonURI(SHOP_URI_TEMPLATE, ID2),
                        P2, getConstantIRI(RDF_TYPE_PROP),
                        O2, getConstantIRI(RESTAURANT_PROP)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constr, ext));

        MappingEntryCluster assertion2 = new DictionaryPatternMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY,
                SUBSTITUTION_FACTORY,
                TERM_FACTORY);

        ImmutableExpression conjunctionCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getVariable("NAME"),
                        TERM_FACTORY.getDBConstant("Name", dbTypeFactory.getDBStringType())),
                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getVariable("TYPE"),
                        TERM_FACTORY.getDBConstant("Restaurant", dbTypeFactory.getDBStringType())));
        ImmutableFunctionalTerm subjectCondition = TERM_FACTORY.getIfElseNull(conjunctionCondition, generatePersonURI(SHOP_URI_TEMPLATE, ID2));
        assert ((ConstructionNode) assertion2.getIQTree().getRootNode()).getSubstitution().get(S2).equals(subjectCondition);
    }

    @Test
    public void dictionaryPatternIfElseNullCompressionTest() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID1, 1, NAME1,
                2, TERM_FACTORY.getDBConstant("Bar", dbTypeFactory.getDBStringType())));
        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(SHOP_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(BAR_PROP)));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, ext1));

        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID1, 1, NAME1,
                2, TERM_FACTORY.getDBConstant("Restaurant", dbTypeFactory.getDBStringType())));
        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(SHOP_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(RESTAURANT_PROP)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, ext2));

        MappingEntryCluster assertion1 = new DictionaryPatternMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY,
                SUBSTITUTION_FACTORY,
                TERM_FACTORY);
        MappingEntryCluster assertion2 = new DictionaryPatternMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY,
                SUBSTITUTION_FACTORY,
                TERM_FACTORY);

        DictionaryPatternMappingEntryCluster mergedAssertion = (DictionaryPatternMappingEntryCluster) assertion1.merge(assertion2).get();
        ImmutableSet<Variable> subjects = mergedAssertion.getRDFFactTemplates().getTriplesOrQuadsVariables().stream()
                .map(t -> t.get(0)).collect(ImmutableSet.toImmutableSet());
        ImmutableSet<Variable> predicates = mergedAssertion.getRDFFactTemplates().getTriplesOrQuadsVariables().stream()
                .map(t -> t.get(1)).collect(ImmutableSet.toImmutableSet());
        ImmutableSet<Variable> objects = mergedAssertion.getRDFFactTemplates().getTriplesOrQuadsVariables().stream()
                .map(t -> t.get(2)).collect(ImmutableSet.toImmutableSet());

        ImmutableSet<ImmutableTerm> hasDisjunction = ((ConstructionNode) mergedAssertion.getIQTree().getRootNode())
                .getSubstitution()
                .stream().map(Map.Entry::getValue)
                .map(this::flattenAndExtractFunctionalTerms)
                .flatMap(ImmutableSet::stream)
                .filter(t -> t.getFunctionSymbol() instanceof DBOrFunctionSymbol)
                .collect(ImmutableCollectors.toSet());
        assert subjects.size() == 1 && predicates.size() == 1 && objects.size() == 2 && hasDisjunction.size() == 2;
    }

    @Test
    public void mergeOnJoinWithSameImplicitConditionTest() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext1, ext2));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);


        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent();
    }

    @Test
    public void mergeOnJoinWithDifferentImplicitConditionTest() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME1));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree1 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext1, ext2));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree1));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ExtensionalDataNode ext3 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));
        ExtensionalDataNode ext4 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree joinSubtree2 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext3, ext4));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree2));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    @Test
    public void mergeOnJoinMultipleTablesTest1() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));
        ExtensionalDataNode ext3 = IQ_FACTORY.createExtensionalDataNode(T3, ImmutableMap.of(0, ID3, 1,ID1, 2, ID2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree1 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext1, ext2, ext3));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree1));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree joinSubtree2 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext3, ext2, ext1));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree2));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent();
    }

    @Test
    public void mergeOnJoinMultipleTablesTest2() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));
        ExtensionalDataNode ext3 = IQ_FACTORY.createExtensionalDataNode(T3, ImmutableMap.of(0, ID3, 1,ID1, 2, ID2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree1 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext1, ext2, ext3));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree1));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ExtensionalDataNode ext4 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1));
        ExtensionalDataNode ext5 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));
        ExtensionalDataNode ext6 = IQ_FACTORY.createExtensionalDataNode(T3, ImmutableMap.of(0, ID3, 1,ID1, 2, ID2));
        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(SHOP_URI_TEMPLATE, ID2),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME2)));
        IQTree joinSubtree2 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext4, ext5, ext6));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree2));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    @Test
    public void mergeOnJoinWithExplicitConditionTest() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode()
                        .changeOptionalFilterCondition(Optional.of(TERM_FACTORY.getDBStartsWith(ImmutableList.of(NAME1, TERM_FACTORY.getDBStringConstant("A"))))),
                ImmutableList.of(ext1, ext2));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);


        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    @Test
    public void mergeOnJoinWithConstantTest() {
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2,
                2, TERM_FACTORY.getDBConstant("Restaurant", dbTypeFactory.getDBStringType())));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext1, ext2));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);


        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    @Test
    public void mergeWithSameNamedGraphTest(){
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 3, ID2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1, G1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2),
                        G1, getConstantIRI(RDF_FACTORY.createIRI("http://example.org/graph1"))));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctQuadAtom(S1, P1, O1, G1),
                IQ_FACTORY.createUnaryIQTree(constr1, ext1));

        SimpleMappingEntryCluster assertion1 = new SimpleMappingEntryCluster(
                iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 2, AGE2));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree( IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(AGE2)),
                ext2);
        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1, G1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(AGE_PROP),
                        O1, getRDFLiteral(AGE2),
                        G1, getConstantIRI(RDF_FACTORY.createIRI("http://example.org/graph1"))));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctQuadAtom(S1, P1, O1, G1),
                IQ_FACTORY.createUnaryIQTree(constr2, childTree));

        FilterMappingEntryCluster assertion2 = new FilterMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent()
                && mergedAssertion.get().getRDFFactTemplates().getVariables().stream().filter(v -> v.getName().startsWith("g")).count() == 1;
    }

    @Test
    public void mergeWithDifferentNamedGraphTest(){
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 3, ID2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1, G1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2),
                        G1, getConstantIRI(RDF_FACTORY.createIRI("http://example.org/graph1"))));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctQuadAtom(S1, P1, O1, G1),
                IQ_FACTORY.createUnaryIQTree(constr1, ext1));

        SimpleMappingEntryCluster assertion1 = new SimpleMappingEntryCluster(
                iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 2, AGE2));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree( IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(AGE2)),
                ext2);
        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1, G1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(AGE_PROP),
                        O1, getRDFLiteral(AGE2),
                        G1, getConstantIRI(RDF_FACTORY.createIRI("http://example.org/graph2"))));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctQuadAtom(S1, P1, O1, G1),
                IQ_FACTORY.createUnaryIQTree(constr2, childTree));

        FilterMappingEntryCluster assertion2 = new FilterMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY, QUERY_TRANSFORMER_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent()
                && mergedAssertion.get().getRDFFactTemplates().getVariables().stream().filter(v -> v.getName().startsWith("g")).count() == 2;
    }

    @Test
    public void complexJoinSubtreeMergeTest(){
        ExtensionalDataNode ext1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 3, ID2));
        ExtensionalDataNode ext2 = IQ_FACTORY.createExtensionalDataNode(T2, ImmutableMap.of(0, ID2, 1, NAME2));

        ConstructionNode constr1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(WORKS_PROP),
                        O1, getRDFLiteral(ID2)));
        IQTree joinSubtree1 =IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(ext1, ext2));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr1, joinSubtree1));
        JoinMappingEntryCluster assertion1 = new JoinMappingEntryCluster(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);


        ConstructionNode constr2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generatePersonURI(PERSON_URI_TEMPLATE, ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        FilterNode filter = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(NAME1));
        IQTree joinSubtree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(IQ_FACTORY.createUnaryIQTree(filter, ext1), ext2));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constr2, joinSubtree));
        JoinMappingEntryCluster assertion2 = new JoinMappingEntryCluster(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY, TERM_FACTORY);

        Optional<MappingEntryCluster> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    private boolean hasFilterNode(IQTree tree) {
        if (tree.getRootNode() instanceof FilterNode) {
            return true;
        } else {
            return tree.getChildren().stream().anyMatch(this::hasFilterNode);
        }
    }

    private ImmutableSet<ImmutableFunctionalTerm> flattenAndExtractFunctionalTerms(ImmutableTerm originalTerm) {
        if (!(originalTerm instanceof ImmutableFunctionalTerm)) {
            return ImmutableSet.of();
        } else {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) originalTerm;
            return ImmutableSet.<ImmutableFunctionalTerm>builder()
                    .add(functionalTerm)
                    .addAll(functionalTerm.getTerms().stream()
                            .map(this::flattenAndExtractFunctionalTerms)
                            .flatMap(ImmutableSet::stream)
                            .collect(ImmutableSet.toImmutableSet()))
                    .build();
        }
    }
}
