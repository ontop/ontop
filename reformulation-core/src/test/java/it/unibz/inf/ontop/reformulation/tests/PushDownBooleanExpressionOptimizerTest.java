package it.unibz.inf.ontop.reformulation.tests;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.PushDownBooleanExpressionOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.PushDownBooleanExpressionOptimizerImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;


public class PushDownBooleanExpressionOptimizerTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 2);
    private final static AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 3);
    private final static AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 2);
    private final static AtomPredicate ANS1_PREDICATE1 = new AtomPredicateImpl("ans1", 4);
    private final static AtomPredicate ANS1_PREDICATE2 = new AtomPredicateImpl("ans1", 3);
    private final static AtomPredicate ANS1_PREDICATE3 = new AtomPredicateImpl("ans1", 5);
    private final static AtomPredicate ANS2_PREDICATE1 = new AtomPredicateImpl("ans2", 2);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable W = DATA_FACTORY.getVariable("W");
    private final static Variable A = DATA_FACTORY.getVariable("A");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Z);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Y, Z);
    private final static ImmutableExpression EXPRESSION3 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.LT, Z, W);
    private final static ImmutableExpression EXPRESSION4 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, Y, Z);
    private final static ImmutableExpression EXPRESSION5 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Z, W);
    private final static ImmutableExpression EXPRESSION6 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, W);
    private final static ImmutableExpression EXPRESSION7 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Y);

    private final MetadataForQueryOptimization metadata;

    public PushDownBooleanExpressionOptimizerTest() {
        this.metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        return new EmptyMetadataForQueryOptimization();
    }

    @Test
    public void testJoiningCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder1.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1,EXPRESSION2,EXPRESSION3));
        queryBuilder1.addChild(constructionNode, joinNode1);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, Z, W));
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, joinNode2);
        queryBuilder1.addChild(joinNode2, dataNode2);
        queryBuilder1.addChild(joinNode2, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder2.init(projectionAtom2, constructionNode2);
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(Optional.of(EXPRESSION2));
        queryBuilder2.addChild(constructionNode2, joinNode3);
        InnerJoinNode joinNode4 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1,EXPRESSION3));
        queryBuilder2.addChild(joinNode3, dataNode1);
        queryBuilder2.addChild(joinNode3, joinNode4);
        queryBuilder2.addChild(joinNode4, dataNode2);
        queryBuilder2.addChild(joinNode4, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningCondition2 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE1, X, Z);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION1));
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom2.getVariables());
        UnionNode unionNode1 = new UnionNodeImpl(projectionAtom2.getVariables());
        ConstructionNode constructionNode3 = new ConstructionNodeImpl(projectionAtom2.getVariables());
        ConstructionNode constructionNode4 = new ConstructionNodeImpl(projectionAtom2.getVariables());

        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, unionNode1);
        queryBuilder1.addChild(unionNode1, constructionNode3);
        queryBuilder1.addChild(unionNode1, constructionNode4);
        queryBuilder1.addChild(constructionNode3, dataNode2);
        queryBuilder1.addChild(constructionNode4, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        ConstructionNode constructionNode5 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode6 = new ConstructionNodeImpl(projectionAtom2.getVariables());
        UnionNode unionNode2 = new UnionNodeImpl(projectionAtom2.getVariables());
        ConstructionNode constructionNode7 = new ConstructionNodeImpl(projectionAtom2.getVariables());
        ConstructionNode constructionNode8 = new ConstructionNodeImpl(projectionAtom2.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION1);
        FilterNode filterNode2 = new FilterNodeImpl(EXPRESSION1);

        queryBuilder2.init(projectionAtom1, constructionNode5);
        queryBuilder2.addChild(constructionNode5, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, constructionNode6);
        queryBuilder2.addChild(constructionNode6, unionNode2);
        queryBuilder2.addChild(unionNode2, constructionNode7);
        queryBuilder2.addChild(unionNode2, constructionNode8);
        queryBuilder2.addChild(constructionNode7, filterNode1);
        queryBuilder2.addChild(constructionNode8, filterNode2);
        queryBuilder2.addChild(filterNode1, dataNode2);
        queryBuilder2.addChild(filterNode2, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningCondition3 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        FilterNode filterNode = new FilterNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION4, EXPRESSION5).get());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, Y, Z, W));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode);
        queryBuilder1.addChild(filterNode, leftJoinNode);
        queryBuilder1.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION4, EXPRESSION5).get());
        FilterNode filterNode2 = new FilterNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION4, EXPRESSION5).get());
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, filterNode1);
        queryBuilder2.addChild(filterNode1, leftJoinNode1);
        queryBuilder2.addChild(leftJoinNode1, dataNode1, LEFT);
        queryBuilder2.addChild(leftJoinNode1, filterNode2, RIGHT);
        queryBuilder2.addChild(filterNode2, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningCondition4 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z, W, A);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION1));
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, A));
        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, X, W));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, leftJoinNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode4, RIGHT);
        queryBuilder1.addChild(leftJoinNode2, dataNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode2, dataNode3, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z, W, A);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode3 = new LeftJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode4 = new LeftJoinNodeImpl(Optional.empty());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION1);

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, leftJoinNode3);
        queryBuilder2.addChild(leftJoinNode3, leftJoinNode4, LEFT);
        queryBuilder2.addChild(leftJoinNode3, dataNode4, RIGHT);
        queryBuilder2.addChild(leftJoinNode4, filterNode1, LEFT);
        queryBuilder2.addChild(leftJoinNode4, dataNode3, RIGHT);
        queryBuilder2.addChild(filterNode1, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningCondition5 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION6));
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, X, W));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.of(EXPRESSION6));
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION6);
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, dataNode2, LEFT);
        queryBuilder2.addChild(leftJoinNode2, filterNode1, RIGHT);
        queryBuilder2.addChild(filterNode1, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));

    }

    @Test
    public void testLeftJoinCondition1 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, W));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, innerJoinNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, RIGHT);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode2);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode2 = new InnerJoinNodeImpl(Optional.empty());

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, innerJoinNode2, LEFT);
        queryBuilder2.addChild(leftJoinNode2, dataNode3, RIGHT);
        queryBuilder2.addChild(innerJoinNode2, dataNode1);
        queryBuilder2.addChild(innerJoinNode2, dataNode2);


        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));

    }

    @Test
    public void testLeftJoinCondition2 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, W));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode1, innerJoinNode1, RIGHT);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode3);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());
        InnerJoinNode innerJoinNode2 = new InnerJoinNodeImpl(Optional.of(EXPRESSION7));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, dataNode2, LEFT);
        queryBuilder2.addChild(leftJoinNode2, innerJoinNode2, RIGHT);
        queryBuilder2.addChild(innerJoinNode2, dataNode1);
        queryBuilder2.addChild(innerJoinNode2, dataNode3);


        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));

    }

    @Test
    public void testLeftJoinAndFilterCondition1 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION6);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, W));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode1);
        queryBuilder1.addChild(filterNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, innerJoinNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, RIGHT);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode2);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode2 = new InnerJoinNodeImpl(Optional.of(EXPRESSION6));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, innerJoinNode2, LEFT);
        queryBuilder2.addChild(leftJoinNode2, dataNode3, RIGHT);
        queryBuilder2.addChild(innerJoinNode2, dataNode1);
        queryBuilder2.addChild(innerJoinNode2, dataNode2);


        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));

    }
    @Test
    public void testLeftJoinAndFilterCondition2 () throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION1);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, W));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode1);
        queryBuilder1.addChild(filterNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, innerJoinNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, RIGHT);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode2);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushDownBooleanExpressionOptimizer pushDownBooleanExpressionOptimizer = new PushDownBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushDownBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        FilterNode filterNode2 = new FilterNodeImpl(EXPRESSION1);
        FilterNode filterNode3 = new FilterNodeImpl(EXPRESSION1);
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.of(EXPRESSION7));
        InnerJoinNode innerJoinNode2 = new InnerJoinNodeImpl(Optional.empty());

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, filterNode2);
        queryBuilder2.addChild(filterNode2, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, innerJoinNode2, LEFT);
        queryBuilder2.addChild(leftJoinNode2, filterNode3, RIGHT);
        queryBuilder2.addChild(innerJoinNode2, dataNode1);
        queryBuilder2.addChild(innerJoinNode2, dataNode2);
        queryBuilder2.addChild(filterNode3, dataNode3);


        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
}
