package it.unibz.inf.ontop.materialization;

import static it.unibz.inf.ontop.utils.MaterializationTestingTools.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.materialization.impl.FilterMappingAssertionInfo;
import it.unibz.inf.ontop.materialization.impl.RDFFactTemplatesImpl;
import it.unibz.inf.ontop.materialization.impl.SimpleMappingAssertionInfo;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;

import java.util.Optional;

public class MappingAssertionTest {
    private static final OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
    private static final DBTypeFactory dbTypeFactory = builder.getDBTypeFactory();

    private static final RelationDefinition T1 = builder.createDatabaseRelation("person",
            "id", dbTypeFactory.getDBStringType(), false,
            "name", dbTypeFactory.getDBStringType(), false,
            "age", dbTypeFactory.getDBLargeIntegerType(), false);

    private final Variable ID1 = TERM_FACTORY.getVariable("id1");
    private final Variable NAME1 = TERM_FACTORY.getVariable("name1");
    private final Variable AGE1 = TERM_FACTORY.getVariable("age1");
    private final Variable ID2 = TERM_FACTORY.getVariable("id2");
    private final Variable NAME2 = TERM_FACTORY.getVariable("name2");
    private final Variable AGE2 = TERM_FACTORY.getVariable("age2");
    private final Variable S1 = TERM_FACTORY.getVariable("s1");
    private final Variable P1 = TERM_FACTORY.getVariable("p1");
    private final Variable O1 = TERM_FACTORY.getVariable("o1");
    private final Variable S2 = TERM_FACTORY.getVariable("s2");
    private final Variable P2 = TERM_FACTORY.getVariable("p2");
    private final Variable O2 = TERM_FACTORY.getVariable("o2");

    private final IRI RDF_TYPE_PROP = RDF_FACTORY.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private final IRI NAME_PROP = RDF_FACTORY.createIRI("http://example.org/name");
    private final IRI AGE_PROP = RDF_FACTORY.createIRI("http://example.org/age");
    private static final ImmutableList<Template.Component> URI_TEMPLATE = Template.of("http://example.org/person/", 0);


    @Test
    public void simpleAssertionMergeTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generateURI(ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq1 = IQ_FACTORY.createIQ(
                    ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                    IQ_FACTORY.createUnaryIQTree(constructionNode1, extensionalNode1));
        MappingAssertionInformation assertion1 = new SimpleMappingAssertionInfo(T1,
                (ImmutableMap<Integer, Variable>) extensionalNode1.getArgumentMap(),
                iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY);

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID2, 2, AGE2));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generateURI(ID2),
                        P2, getConstantIRI(AGE_PROP),
                        O2, getRDFLiteral(AGE2)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, extensionalNode2));
        MappingAssertionInformation assertion2 = new SimpleMappingAssertionInfo(T1,
                (ImmutableMap<Integer, Variable>) extensionalNode2.getArgumentMap(),
                iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY);

        Optional<MappingAssertionInformation> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent();

    }

    @Test
    public void conflictingVariablesInRelationTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generateURI(ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, extensionalNode1));
        MappingAssertionInformation assertion1 = new SimpleMappingAssertionInfo(T1,
                (ImmutableMap<Integer, Variable>) extensionalNode1.getArgumentMap(),
                iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                iq1.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY);

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, AGE1));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generateURI(ID1),
                        P1, getConstantIRI(AGE_PROP),
                        O1, getRDFLiteral(AGE1)));
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, extensionalNode2));
        MappingAssertionInformation assertion2 = new SimpleMappingAssertionInfo(T1,
                (ImmutableMap<Integer, Variable>) extensionalNode2.getArgumentMap(),
                iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                iq2.getVariableGenerator(),
                IQ_FACTORY, SUBSTITUTION_FACTORY);

        MappingAssertionInformation mergedAssertion = assertion1.merge(assertion2).get();
        assert ((ConstructionNode) mergedAssertion.getIQTree().getRootNode()).getSubstitution().getRangeSet()
                .equals(ImmutableSet.of(generateURI(ID1), getConstantIRI(NAME_PROP), getConstantIRI(AGE_PROP), getRDFLiteral(NAME1)));
    }

    @Test
    public void simplifyNotNullFilterTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode nameFilter = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generateURI(ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(nameFilter, extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingAssertionInformation info = new FilterMappingAssertionInfo(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                extensionalNode1,
                childTree,
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);
        assert !hasFilterNode(info.getIQTree());

    }

    @Test
    public void filterOnNotProjectedVariableTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode ageFilter = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(AGE1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generateURI(ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(ageFilter, extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingAssertionInformation info = new FilterMappingAssertionInfo(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                extensionalNode1,
                childTree,
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);

        assert hasFilterNode(info.getIQTree());

    }

    @Test
    public void simplifyNotNullConjunctionFilterTest() {
        ExtensionalDataNode extensionalNode1 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        var conjunctionCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBIsNotNull(ID1),
                TERM_FACTORY.getDBIsNotNull(NAME1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, generateURI(ID1),
                        P1, getConstantIRI(NAME_PROP),
                        O1, getRDFLiteral(NAME1)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(conjunctionCondition), extensionalNode1);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(constructionNode1, childTree));

        MappingAssertionInformation info = new FilterMappingAssertionInfo(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                extensionalNode1,
                childTree,
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);

        assert !hasFilterNode(info.getIQTree());
    }

    @Test
    public void mergeOnNonCompatibleFiltersTest() {
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode ageFilter1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE1, TERM_FACTORY.getDBConstant("18", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode nameTypeConstructionNode = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, getRDFLiteral(NAME1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(NAME_PROP)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(ageFilter1, extensionalDataNode);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(nameTypeConstructionNode, childTree));

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID2, 1, NAME2, 2, AGE2));
        FilterNode ageFilter2 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE2, TERM_FACTORY.getDBConstant("20", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generateURI(ID2),
                        P2, getConstantIRI(NAME_PROP),
                        O2, getRDFLiteral(NAME2)));
        IQTree childTree2 = IQ_FACTORY.createUnaryIQTree(ageFilter2, extensionalNode2);
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, childTree2));

        MappingAssertionInformation assertion1 = new FilterMappingAssertionInfo(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                extensionalDataNode,
                childTree,
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);

        MappingAssertionInformation assertion2 = new FilterMappingAssertionInfo(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                extensionalNode2,
                childTree2,
                iq2.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);

        Optional<MappingAssertionInformation> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isEmpty();
    }

    @Test
    public void mergeOnCompatibleFiltersTest() {
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID1, 1, NAME1, 2, AGE1));
        FilterNode ageFilter1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE1, TERM_FACTORY.getDBConstant("18", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode nameTypeConstructionNode = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S1, P1, O1),
                SUBSTITUTION_FACTORY.getSubstitution(S1, getRDFLiteral(NAME1),
                        P1, getConstantIRI(RDF_TYPE_PROP),
                        O1, getConstantIRI(NAME_PROP)));
        IQTree childTree = IQ_FACTORY.createUnaryIQTree(ageFilter1, extensionalDataNode);
        IQ iq1 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S1, P1, O1),
                IQ_FACTORY.createUnaryIQTree(nameTypeConstructionNode, childTree));

        ExtensionalDataNode extensionalNode2 = IQ_FACTORY.createExtensionalDataNode(T1, ImmutableMap.of(0, ID2, 1, NAME2, 2, AGE2));
        FilterNode ageFilter2 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(AGE2, TERM_FACTORY.getDBConstant("18", dbTypeFactory.getDBLargeIntegerType())));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode( ImmutableSet.of(S2, P2, O2),
                SUBSTITUTION_FACTORY.getSubstitution(S2, generateURI(ID2),
                        P2, getConstantIRI(NAME_PROP),
                        O2, getRDFLiteral(NAME2)));
        IQTree childTree2 = IQ_FACTORY.createUnaryIQTree(ageFilter2, extensionalNode2);
        IQ iq2 = IQ_FACTORY.createIQ(
                ATOM_FACTORY.getDistinctTripleAtom(S2, P2, O2),
                IQ_FACTORY.createUnaryIQTree(constructionNode2, childTree2));

        MappingAssertionInformation assertion1 = new FilterMappingAssertionInfo(iq1.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq1.getProjectionAtom().getArguments())),
                extensionalDataNode,
                childTree,
                iq1.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);

        MappingAssertionInformation assertion2 = new FilterMappingAssertionInfo(iq2.getTree(),
                new RDFFactTemplatesImpl(ImmutableList.of(iq2.getProjectionAtom().getArguments())),
                extensionalNode2,
                childTree2,
                iq2.getVariableGenerator(),
                IQ_FACTORY, TERM_FACTORY, SUBSTITUTION_FACTORY);

        Optional<MappingAssertionInformation> mergedAssertion = assertion1.merge(assertion2);
        assert mergedAssertion.isPresent();
    }

    private ImmutableFunctionalTerm generateURI(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE, ImmutableList.of(argument));
    }

    private IRIConstant getConstantIRI(IRI iri) {
        return TERM_FACTORY.getConstantIRI(iri);
    }

    private ImmutableFunctionalTerm getRDFLiteral(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, RDF_FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#string"));
    }

    private boolean hasFilterNode(IQTree tree) {
        if (tree.getRootNode() instanceof FilterNode) {
            return true;
        } else {
            return tree.getChildren().stream().anyMatch(this::hasFilterNode);
        }
    }
}
