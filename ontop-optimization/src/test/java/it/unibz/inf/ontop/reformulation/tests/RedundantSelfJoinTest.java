package it.unibz.inf.ontop.reformulation.tests;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.model.impl.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FixedPointJoinLikeOptimizer;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;
import org.junit.Test;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.model.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.ExpressionOperation.LT;
import static it.unibz.inf.ontop.model.ExpressionOperation.OR;
import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.foldBooleanExpressions;
import static it.unibz.inf.ontop.model.impl.OBDAVocabulary.NULL;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

/**
 * Optimizations for inner joins based on unique constraints (like PKs).
 *
 * For self-joins
 *
 */
public class RedundantSelfJoinTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 3);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 3);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 3);
    private final static AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 2);
    private final static AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 2);
    private final static AtomPredicate TABLE6_PREDICATE = new AtomPredicateImpl("table6", 3);
    private final static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private final static AtomPredicate ANS1_PREDICATE_1 = new AtomPredicateImpl("ans1", 1);
    private final static AtomPredicate ANS1_PREDICATE_2 = new AtomPredicateImpl("ans1", 2);
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable A = DATA_FACTORY.getVariable("A");
    private final static Variable B = DATA_FACTORY.getVariable("B");
    private final static Variable C = DATA_FACTORY.getVariable("C");
    private final static Variable D = DATA_FACTORY.getVariable("D");
    private final static Variable E = DATA_FACTORY.getVariable("E");
    private final static Variable P1 = DATA_FACTORY.getVariable("P");
    private final static Constant ONE = DATA_FACTORY.getConstantLiteral("1");
    private final static Constant TWO = DATA_FACTORY.getConstantLiteral("2");
    private final static Constant THREE = DATA_FACTORY.getConstantLiteral("3");

    private final static Variable M = DATA_FACTORY.getVariable("m");
    private final static Variable M1 = DATA_FACTORY.getVariable("m1");
    private final static Variable N = DATA_FACTORY.getVariable("n");
    private final static Variable N1 = DATA_FACTORY.getVariable("n1");
    private final static Variable N2 = DATA_FACTORY.getVariable("n2");
    private final static Variable O = DATA_FACTORY.getVariable("o");
    private final static Variable O1 = DATA_FACTORY.getVariable("o1");
    private final static Variable O2 = DATA_FACTORY.getVariable("o2");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, M, N);

    private static final MetadataForQueryOptimization METADATA = initMetadata();

    private static URITemplatePredicate URI_PREDICATE_ONE_VAR =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");

    private static MetadataForQueryOptimization initMetadata() {
        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();

        /**
         * Table 1: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE1_PREDICATE, ImmutableList.of(1));

        /**
         * Table 2: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE2_PREDICATE, ImmutableList.of(2));

        /**
         * Table 3: composite unique constraint over the first TWO columns
         */
        uniqueKeyBuilder.put(TABLE3_PREDICATE, ImmutableList.of(1, 2));

        /**
         * Table 4: unique constraint over the first column
         */
        uniqueKeyBuilder.put(TABLE4_PREDICATE, ImmutableList.of(1));

        /**
         * Table 5: unique constraint over the second column
         */
        uniqueKeyBuilder.put(TABLE5_PREDICATE, ImmutableList.of(2));

        /**
         * Table 6: two atomic unique constraints over the first and third columns
         */
        uniqueKeyBuilder.put(TABLE6_PREDICATE, ImmutableList.of(1));
        uniqueKeyBuilder.put(TABLE6_PREDICATE, ImmutableList.of(3));


        return new MetadataForQueryOptimizationImpl(
                DBMetadataTestingTools.createDummyMetadata(),
                uniqueKeyBuilder.build(),
                new UriTemplateMatcher());
    }

    @Test
    public void testJoiningConditionTest() throws  EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSION1));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        FilterNode filterNode = new FilterNodeImpl(EXPRESSION1);
        queryBuilder1.addChild(constructionNode1, filterNode);
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder1.addChild(filterNode, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\n Expected query: \n" +  query1);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    /**
     * TODO: explain
     */
    @Test
    public void testSelfJoinElimination2() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\n After optimization: \n" +  query);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testNonEliminationTable1() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, Z, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\n After optimization: \n" +  query);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testSelfJoinElimination3() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;
        System.out.println("\n After optimization: \n" +  query);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testNonEliminationTable3() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Z, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;
        System.out.println("\n After optimization: \n" +  query);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testSelfJoinElimination4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode4);
        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O)));

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testSelfJoinElimination5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);

        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O)));

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testPropagation1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testPropagation2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        queryBuilder.addChild(leftJoinNode, dataNode3, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        queryBuilder1.addChild(constructionNode1, leftJoinNode);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(leftJoinNode, dataNode5, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode6, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\n Expected query: \n" +  query1);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    /**
     * Ignored for the moment because the looping mechanism is not implemented
     */
    @Test
    public void testLoop1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }


    @Test
    public void testTopJoinUpdate() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(LT, O1, N1)));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;

        System.out.println("\n After optimization: \n" +  query);


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(LT, O, N)));
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testDoubleUniqueConstraints1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N1, TWO));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(O, TWO)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, TWO));
        expectedQueryBuilder.addChild(newConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;
        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testDoubleUniqueConstraints2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(M, TWO)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode expectedDataNode =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N, O));
        expectedQueryBuilder.addChild(newConstructionNode, expectedDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                ;
        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testDoubleUniqueConstraints3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N1, O2));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode expectedDataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode1);
        ExtensionalDataNode expectedDataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N, O2));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test(expected = EmptyQueryException.class)
    public void testNonUnification1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, ONE, O));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, TWO, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\n Optimized query (should not be produced): \n" +  query);
    }

    @Test
    public void testNonUnification2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(M));
        queryBuilder.addChild(leftJoinNode, leftConstructionNode, LEFT);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N2, O2));
        queryBuilder.addChild(leftConstructionNode, dataNode1);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(leftJoinNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, ONE, O));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, TWO, O));

        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(N, NULL, O, NULL)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, dataNode1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));

        Optional<QueryNode> optionalClosestAncestor = results.getOptionalClosestAncestor();
        assertTrue(optionalClosestAncestor.isPresent());
        assertTrue(optionalClosestAncestor.get().isSyntacticallyEquivalentTo(newRootNode));
        assertFalse(results.getOptionalNextSibling().isPresent());
    }

    @Test
    public void testJoiningConditionRemoval() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = DATA_FACTORY.getImmutableExpression(OR,
                DATA_FACTORY.getImmutableExpression(EQ, O, ONE),
                DATA_FACTORY.getImmutableExpression(EQ, O, TWO));

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(joiningCondition));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(O, ONE)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query : \n" +  expectedQuery);

        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));

        assertFalse(results.getOptionalNewNode().isPresent());
        assertTrue(results.getOptionalReplacingChild().isPresent());
        assertTrue(results.getOptionalReplacingChild().get().isSyntacticallyEquivalentTo(dataNode5));
    }

    @Test(expected = EmptyQueryException.class)
    public void testInsatisfiedJoiningCondition() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = DATA_FACTORY.getImmutableExpression(OR,
                DATA_FACTORY.getImmutableExpression(EQ, O, TWO),
                DATA_FACTORY.getImmutableExpression(EQ, O, THREE));

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(joiningCondition));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
    }

    @Test
    public void testNoModification1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = DATA_FACTORY.getImmutableExpression(OR,
                DATA_FACTORY.getImmutableExpression(EQ, O, TWO),
                DATA_FACTORY.getImmutableExpression(EQ, O, THREE));

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(joiningCondition));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        int initialVersion = query.getVersionNumber();

        IntermediateQuery expectedQuery = query.createSnapshot();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
        assertEquals("The version number has changed", initialVersion, query.getVersionNumber());
    }

    @Test
    public void testNoModification2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        int initialVersion = query.getVersionNumber();

        IntermediateQuery expectedQuery = query.createSnapshot();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\nAfter optimization: \n" +  query);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
        assertEquals("The version number has changed", initialVersion, query.getVersionNumber());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);



        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(EQ, M, N)));
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);
        expectedQueryBuilder.addChild(newLJNode, dataNode3, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testOptimizationOnRightPartOfLJ2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, M));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(
                projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(O, M)),
                Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(EQ, M, N)));
        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, M));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testOptimizationOnRightPartOfLJ3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);
        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testOptimizationOnRightPartOfLJ4() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);
        expectedQueryBuilder.addChild(newLJNode, dataNode3, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testOptimizationOnRightPartOfLJ5() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N1));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testOptimizationOnRightPartOfLJ6() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, O, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, M));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(
                projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(M, O)),
                Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(EQ, N, O)));
        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, O, O, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testOptimizationOnRightPartOfLJ7() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, O, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, N));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, ONE, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = new LeftJoinNodeImpl(foldBooleanExpressions(
                DATA_FACTORY.getImmutableExpression(EQ, N, ONE),
                DATA_FACTORY.getImmutableExpression(EQ, O, ONE)));
        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, ONE, ONE));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }



    @Test
    public void testSubstitutionPropagationWithBlockingUnion1() throws EmptyQueryException {
        Constant constant = DATA_FACTORY.getConstantLiteral("constant");
        ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> unicityConstraints =
                ImmutableMultimap.of (TABLE1_PREDICATE, ImmutableList.of(1));
        MetadataForQueryOptimization metadata = new MetadataForQueryOptimizationImpl(
                DBMetadataTestingTools.createDummyMetadata(), unicityConstraints, new UriTemplateMatcher()
        );
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B,
                constant));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, X,
                C));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X,
                D));
        initialQueryBuilder.addChild(initialRootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, dataNode3);
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, dataNode1);
        initialQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        System.out.println("Initial query: "+ initialQuery);
        /**
         * The following is only one possible syntactic variant of the expected query,
         * namely the one expected based on the current state of the implementation of self join elimination
         * and substitution propagation.
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, X,
                constant));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, dataNode3);
        expectedQueryBuilder.addChild(unionNode, dataNode4);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("Expected query: "+ expectedQuery);
        IntermediateQuery optimizedQuery = new FixedPointJoinLikeOptimizer().optimize(initialQuery);
        System.out.println("Optimized query: "+ optimizedQuery);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test
    public void testSubstitutionPropagationWithBlockingUnion2() throws EmptyQueryException {
        Constant constant = DATA_FACTORY.getConstantLiteral("constant");
        ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> unicityConstraints =
                ImmutableMultimap.of (TABLE1_PREDICATE, ImmutableList.of(1));
        MetadataForQueryOptimization metadata = new MetadataForQueryOptimizationImpl(
                DBMetadataTestingTools.createDummyMetadata(), unicityConstraints, new UriTemplateMatcher()
        );
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X,
                Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X, Y));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, X,
                constant));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, Y,
                C));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X,
                Y));
        initialQueryBuilder.addChild(initialRootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, dataNode3);
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, dataNode1);
        initialQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        System.out.println("Initial query: "+ initialQuery);

        /**
         * The following is only one possible syntactic variant of the expected query,
         * namely the one expected based on the current state of the implementation of self join elimination
         * and substitution propagation.
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y));
        ConstructionNode constructionNode = new ConstructionNodeImpl(
                ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl(
                        ImmutableMap.of(Y, X)),
                        Optional.empty()
                );
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, dataNode3);
        expectedQueryBuilder.addChild(unionNode, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, dataNode1);


        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("Expected query: "+ expectedQuery);
        IntermediateQuery optimizedQuery = new FixedPointJoinLikeOptimizer().optimize(initialQuery);
        System.out.println("Optimized query: "+ optimizedQuery);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    private static P2<IntermediateQueryBuilder, InnerJoinNode> initAns1(MetadataForQueryOptimization metadata)
            throws IntermediateQueryBuilderException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(metadata);

        DistinctVariableOnlyDataAtom ans1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                new AtomPredicateImpl("ans1", 1), Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(ans1Atom.getVariables());
        queryBuilder.init(ans1Atom, rootNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(rootNode, joinNode);

        return P.p(queryBuilder, joinNode);
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, URI_TEMPLATE_STR_2, argument);
    }

}
