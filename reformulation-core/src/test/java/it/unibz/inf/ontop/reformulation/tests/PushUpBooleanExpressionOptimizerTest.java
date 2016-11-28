package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.PushUpBooleanExpressionOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.PushUpBooleanExpressionOptimizerImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;

public class PushUpBooleanExpressionOptimizerTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 3);
    private final static AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 3);
    private final static AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 1);
    private final static AtomPredicate ANS1_PREDICATE1 = new AtomPredicateImpl("ans1", 1);
    private final static AtomPredicate ANS1_PREDICATE3 = new AtomPredicateImpl("ans1", 3);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable V = DATA_FACTORY.getVariable("V");
    private final static Variable W = DATA_FACTORY.getVariable("W");
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Z);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Y, Z);
    private final static ImmutableExpression EXPRESSION3 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.GTE, W, Z);
    private final static ImmutableExpression EXPRESSION4 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.LT, V, W);
    private final static ImmutableExpression EXPRESSION5 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, X, DATA_FACTORY.getConstantLiteral("a"));

    private final MetadataForQueryOptimization metadata;

    public PushUpBooleanExpressionOptimizerTest() {
        this.metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        return new EmptyMetadataForQueryOptimization();
    }


    @Test
    public void testPropagationFomInnerJoinProvider() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, joinNode2);
        queryBuilder1.addChild(joinNode2, dataNode2);
        queryBuilder1.addChild(joinNode2, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, joinNode1);
        queryBuilder2.addChild(joinNode1, dataNode2);
        queryBuilder2.addChild(joinNode1, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomInnerJoinProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, unionNode);
        queryBuilder1.addChild(unionNode, dataNode1);
        queryBuilder1.addChild(unionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationFomFilterNodeProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        FilterNode filterNode = new FilterNodeImpl(EXPRESSION1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, filterNode);
        queryBuilder1.addChild(filterNode, dataNode1);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.of(EXPRESSION1));

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomFilterNodeProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X, Y, Z));
        FilterNode filterNode = new FilterNodeImpl(EXPRESSION1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, unionNode);
        queryBuilder1.addChild(unionNode, dataNode1);
        queryBuilder1.addChild(unionNode, filterNode);
        queryBuilder1.addChild(filterNode, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomLeftJoinProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, leftJoinNode);
        queryBuilder1.addChild(leftJoinNode, dataNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode3, RIGHT);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationToExistingFilterRecipient() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION3);
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION2));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, W, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, filterNode1);
        queryBuilder1.addChild(filterNode1, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        FilterNode filterNode2 = new FilterNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION3, EXPRESSION2).get());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());


        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, filterNode2);
        queryBuilder2.addChild(filterNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testRecursivePropagation() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.of(EXPRESSION1));
        FilterNode filterNode = new FilterNodeImpl(EXPRESSION3);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, W, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, joinNode2);
        queryBuilder1.addChild(joinNode2, dataNode2);
        queryBuilder1.addChild(joinNode2, filterNode);
        queryBuilder1.addChild(filterNode, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION3));

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode3);
        queryBuilder2.addChild(joinNode3, dataNode1);
        queryBuilder2.addChild(joinNode3, joinNode1);
        queryBuilder2.addChild(joinNode1, dataNode2);
        queryBuilder2.addChild(joinNode1, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void testPropagationToLeftJoinRecipient() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, joinNode1, RIGHT);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, dataNode1, LEFT);
        queryBuilder2.addChild(leftJoinNode2, joinNode2, RIGHT);
        queryBuilder2.addChild(joinNode2, dataNode2);
        queryBuilder2.addChild(joinNode2, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationThroughLeftJoin() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, leftJoinNode);
        queryBuilder1.addChild(leftJoinNode, joinNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode1, RIGHT);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        FilterNode filterNode = new FilterNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1, EXPRESSION2).get());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, filterNode);
        queryBuilder2.addChild(filterNode, leftJoinNode);
        queryBuilder2.addChild(leftJoinNode, joinNode2, LEFT);
        queryBuilder2.addChild(leftJoinNode, dataNode1, RIGHT);
        queryBuilder2.addChild(joinNode2, dataNode2);
        queryBuilder2.addChild(joinNode2, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationWithIntermediateProjector() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X), ImmutableMap.of(X, generateURI(Y, Z)));
        FilterNode filterNode = new FilterNodeImpl(EXPRESSION2);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, W));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));

        queryBuilder1.init(projectionAtom, constructionNode1);
        queryBuilder1.addChild(constructionNode1, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, filterNode);
        queryBuilder1.addChild(filterNode, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(X, Y, Z), ImmutableMap.of(X, generateURI(Y, Z)));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.of(EXPRESSION2));

        queryBuilder2.init(projectionAtom, constructionNode1);
        queryBuilder2.addChild(constructionNode1, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationWithIntermediateProjectors() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION5);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X), ImmutableMap.of(X, generateURI(Y, Z)));
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION2));
        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(Z), ImmutableMap.of(Z, generateURI(V, W)));
        FilterNode filterNode2 = new FilterNodeImpl(EXPRESSION4);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, V, W));

        queryBuilder1.init(projectionAtom, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode1);
        queryBuilder1.addChild(filterNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, constructionNode3);
        queryBuilder1.addChild(constructionNode3, filterNode2);
        queryBuilder1.addChild(filterNode2, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        FilterNode filterNode3 = new FilterNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION5, EXPRESSION2, EXPRESSION4).get());
        ConstructionNode constructionNode4 = new ConstructionNodeImpl(ImmutableSet.of(V, W, X, Y, Z), ImmutableMap.of(X, generateURI(Y, Z)));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode5 = new ConstructionNodeImpl(ImmutableSet.of(V, W, Z), ImmutableMap.of(Z, generateURI(V, W)));

        queryBuilder2.init(projectionAtom, constructionNode1);
        queryBuilder2.addChild(constructionNode1, filterNode3);
        queryBuilder2.addChild(filterNode3, constructionNode4);
        queryBuilder2.addChild(constructionNode4, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, constructionNode5);
        queryBuilder2.addChild(constructionNode5, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationThroughUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X));
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION1);
        FilterNode filterNode2 = new FilterNodeImpl(EXPRESSION1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, W, X, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, unionNode1);
        queryBuilder1.addChild(unionNode1, filterNode1);
        queryBuilder1.addChild(unionNode1, filterNode2);
        queryBuilder1.addChild(filterNode1, dataNode1);
        queryBuilder1.addChild(filterNode2, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        FilterNode filterNode3 = new FilterNodeImpl(EXPRESSION1);
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(X,Z));

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, filterNode3);
        queryBuilder2.addChild(filterNode3, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode1);
        queryBuilder2.addChild(unionNode2, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    private static ImmutableFunctionalTerm generateURI(VariableOrGroundTerm... arguments) {
        URITemplatePredicate uriTemplatePredicate = new URITemplatePredicateImpl(arguments.length + 1);
        String uriTemplateString = "http://example.org/ds1/";
        for (VariableOrGroundTerm argument : arguments) {
            uriTemplateString = uriTemplateString.toString() + "{}";
        }
        Constant uriTemplate = DATA_FACTORY.getConstantLiteral(uriTemplateString);
        ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
        builder.add(uriTemplate);
        builder.add(arguments);
        return DATA_FACTORY.getImmutableFunctionalTerm(uriTemplatePredicate, builder.build());
    }
}
