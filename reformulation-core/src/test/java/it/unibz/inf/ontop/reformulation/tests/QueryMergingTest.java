package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static org.junit.Assert.assertTrue;

public class QueryMergingTest {

    private static MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();
    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;
    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private static AtomPredicate P1_PREDICATE = new AtomPredicateImpl("p1", 2);
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable Y = DATA_FACTORY.getVariable("y");
    private static Variable S = DATA_FACTORY.getVariable("s");
    private static Variable T = DATA_FACTORY.getVariable("t");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static Variable B1 = DATA_FACTORY.getVariable("b1");
    private static Variable C = DATA_FACTORY.getVariable("c");
    private static DistinctVariableOnlyDataAtom ANS1_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static DistinctVariableOnlyDataAtom P1_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");
    private static Constant ONE = DATA_FACTORY.getConstantLiteral("1", INTEGER);
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
    private static AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
    private static AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
    private static ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
    private static ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
    private static ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, B, C));


    @Test
    public void testPruning1() throws EmptyQueryException {
        GroundFunctionalTerm xValue = (GroundFunctionalTerm) generateURI1(ONE);

        IntermediateQuery mainQuery = createBasicSparqlQuery(ImmutableMap.of(X, xValue), xValue, Y);

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        ImmutableSet<Variable> unionProjectedVariables = ImmutableSet.of(S, B);
        UnionNode unionNode = new UnionNodeImpl(unionProjectedVariables);
        subQueryBuilder.addChild(subQueryRoot, unionNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(unionProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI2(A))), Optional.empty());
        subQueryBuilder.addChild(unionNode, leftConstructionNode);
        subQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(unionProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(C))), Optional.empty());
        subQueryBuilder.addChild(unionNode, rightConstructionNode);
        subQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, B, ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);


        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }


    @Test
    public void testEx1() {
        // TODO: complete
    }

    private static IntermediateQuery createBasicSparqlQuery(
            ImmutableMap<Variable, ImmutableTerm> topBindings,
            VariableOrGroundTerm p1Arg1, VariableOrGroundTerm p1Arg2) {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(topBindings), Optional.empty());

        queryBuilder.init(ANS1_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, p1Arg1, p1Arg2));
        queryBuilder.addChild(rootNode, dataNode);

        return queryBuilder.build();
    }

    private static void optimizeAndCompare(IntermediateQuery mainQuery, IntermediateQuery subQuery,
                                           IntermediateQuery expectedQuery)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  mainQuery);
        System.out.println("\n Sub-query: \n" +  subQuery);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        // Updates the query (in-place optimization)
        mainQuery.applyProposal(new QueryMergingProposalImpl(subQuery), REQUIRE_USING_IN_PLACE_EXECUTOR);

        System.out.println("\n Optimized query: \n" +  mainQuery);

        assertTrue(areEquivalent(mainQuery, expectedQuery));

    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_2, argument);
    }
}


