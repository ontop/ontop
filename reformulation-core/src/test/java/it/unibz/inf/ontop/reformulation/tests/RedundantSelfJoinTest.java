package it.unibz.inf.ontop.reformulation.tests;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.sql.DBMetadataExtractor;
import org.junit.Ignore;
import org.junit.Test;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.model.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.ExpressionOperation.LT;
import static it.unibz.inf.ontop.model.ExpressionOperation.OR;
import static it.unibz.inf.ontop.model.impl.OBDAVocabulary.NULL;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

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
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable A = DATA_FACTORY.getVariable("A");
    private final static Variable B = DATA_FACTORY.getVariable("B");
    private final static Variable C = DATA_FACTORY.getVariable("C");
    private final static Variable D = DATA_FACTORY.getVariable("D");
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

    private final MetadataForQueryOptimization metadata;

    private static URITemplatePredicate URI_PREDICATE_ONE_VAR =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");

    public RedundantSelfJoinTest() {
        metadata = initMetadata();
    }

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
                DBMetadataExtractor.createDummyMetadata(),
                uniqueKeyBuilder.build(),
                new UriTemplateMatcher());
    }

    @Test
    public void testJoiningConditionTest() throws  EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        FilterNode filterNode = new FilterNodeImpl(EXPRESSION1);
        queryBuilder1.addChild(constructionNode1, filterNode);
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder1.addChild(filterNode, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\n Expected query: \n" +  query1);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
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

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    /**
     * TODO: explain
     */
    @Test
    public void testSelfJoinElimination2() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(metadata);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testNonEliminationTable1() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(metadata);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, Z, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testSelfJoinElimination3() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(metadata);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testNonEliminationTable3() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(metadata);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Z, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testSelfJoinElimination4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
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

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testSelfJoinElimination5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);

        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O)));

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testPropagation1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
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

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testPropagation2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
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

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    /**
     * Ignored for the moment because the looping mechanism is not implemented
     */
    @Test
    public void testLoop1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
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

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }


    @Test
    public void testTopJoinUpdate() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
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

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testDoubleUniqueConstraints1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(O, TWO)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, TWO));
        expectedQueryBuilder.addChild(newConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testDoubleUniqueConstraints2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(M, TWO)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode expectedDataNode =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N, O));
        expectedQueryBuilder.addChild(newConstructionNode, expectedDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testDoubleUniqueConstraints3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode expectedDataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode1);
        ExtensionalDataNode expectedDataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N, O2));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test(expected = EmptyQueryException.class)
    public void testNonUnification1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();
        System.out.println("\n Optimized query (should not be produced): \n" +  optimizedQuery);
    }

    @Test
    public void testNonUnification2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode newRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(N, NULL, O, NULL)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, dataNode1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        IntermediateQuery optimizedQuery = results.getResultingQuery();

        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

        Optional<QueryNode> optionalClosestAncestor = results.getOptionalClosestAncestor();
        assertTrue(optionalClosestAncestor.isPresent());
        assertTrue(optionalClosestAncestor.get().isSyntacticallyEquivalentTo(newRootNode));
        assertFalse(results.getOptionalNextSibling().isPresent());
    }

    @Test
    public void testJoiningConditionRemoval() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(O, ONE)), Optional.empty());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query : \n" +  expectedQuery);

        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        IntermediateQuery optimizedQuery = results.getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

        assertFalse(results.getOptionalNewNode().isPresent());
        assertTrue(results.getOptionalReplacingChild().isPresent());
        assertTrue(results.getOptionalReplacingChild().get().isSyntacticallyEquivalentTo(dataNode5));
    }

    @Test(expected = EmptyQueryException.class)
    public void testInsatisfiedJoiningCondition() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
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


    private static P2<IntermediateQueryBuilder, InnerJoinNode> initAns1(MetadataForQueryOptimization metadata)
            throws IntermediateQueryBuilderException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);

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
