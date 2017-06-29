package it.unibz.inf.ontop.intermediatequery;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import org.junit.Test;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: test
 */
public class NodeDeletionTest {

    @Test(expected = EmptyQueryException.class)
    public void testSimpleJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(x));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                DATA_FACTORY.getAtomPredicate("ans1", 1), x);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableExpression falseCondition = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND, falseValue, falseValue);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode table1 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(
                DATA_FACTORY.getAtomPredicate("table1", 1), x));
        queryBuilder.addChild(joinNode, table1);

        ExtensionalDataNode table2 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(
                DATA_FACTORY.getAtomPredicate("table2", 1), x));
        queryBuilder.addChild(joinNode, table2);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        /**
         * Should throw the EmptyQueryException
         */
        IntermediateQuery optimizedQuery = INNER_JOIN_OPTIMIZER.optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }

    @Test
    public void testInvalidRightPartOfLeftJoin1() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(x,y));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                DATA_FACTORY.getAtomPredicate("ans1", 2), x, y);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableExpression falseCondition = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND, falseValue, falseValue);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, ljNode);

        String table1Name = "table1";
        ExtensionalDataNode table1 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate(table1Name, 1), x));
        queryBuilder.addChild(ljNode, table1, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(ljNode, joinNode, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        ExtensionalDataNode table2 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table2", 2), x, y));
        queryBuilder.addChild(joinNode, table2);

        ExtensionalDataNode table3 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table3", 2), x, y));
        queryBuilder.addChild(joinNode, table3);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = INNER_JOIN_OPTIMIZER.optimize(initialQuery);
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof ExtensionalDataNode);
        assertEquals(((ExtensionalDataNode) viceRootNode).getProjectionAtom().getPredicate().getName(), table1Name);
        assertTrue(optimizedQuery.getChildren(viceRootNode).isEmpty());
    }

    @Test
    public void testUnion1() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                DATA_FACTORY.getAtomPredicate("ans1", 2), x, y);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableExpression falseCondition = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND, falseValue, falseValue);

        UnionNode topUnion = IQ_FACTORY.createUnionNode(projectedVariables);
        queryBuilder.addChild(rootNode, topUnion);

        //DistinctVariableOnlyDataAtom subAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(DATA_FACTORY.getAtomPredicate("ansu1", 2), x, y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode1);

        String table1Name = "table1";
        ExtensionalDataNode table1 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate(table1Name, 2), x, y));
        queryBuilder.addChild(constructionNode1, table1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode2);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(constructionNode2, joinNode1);

        ExtensionalDataNode table2 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table2", 2), x, y));
        queryBuilder.addChild(joinNode1, table2);

        ExtensionalDataNode table3 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table3", 2), x, y));
        queryBuilder.addChild(joinNode1, table3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode3);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(constructionNode3, joinNode2);

        ExtensionalDataNode table4 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table4", 2), x, y));
        queryBuilder.addChild(joinNode2, table4);

        ExtensionalDataNode table5 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table5", 2), x, y));
        queryBuilder.addChild(joinNode2, table5);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = INNER_JOIN_OPTIMIZER.optimize(initialQuery);
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof ConstructionNode);
        assertEquals(optimizedQuery.getChildren(viceRootNode).size(), 1);

        QueryNode viceViceRootNode = optimizedQuery.getFirstChild(viceRootNode).get();
        assertTrue(viceViceRootNode instanceof ExtensionalDataNode);
        assertEquals(((ExtensionalDataNode) viceViceRootNode).getProjectionAtom().getPredicate().getName(), table1Name);
        assertTrue(optimizedQuery.getChildren(viceViceRootNode).isEmpty());
    }

    @Test
    public void testUnion2() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                DATA_FACTORY.getAtomPredicate("ans1", 2), x, y);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables);


        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableExpression falseCondition = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND, falseValue, falseValue);

        UnionNode topUnion = IQ_FACTORY.createUnionNode(projectedVariables);
        queryBuilder.addChild(rootNode, topUnion);

        //DataAtom subAtom = DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("ansu1", 2), x, y);
        
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode1);

        String table1Name = "table1";
        ExtensionalDataNode table1 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate(table1Name, 2), x, y));
        queryBuilder.addChild(constructionNode1, table1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode2);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(constructionNode2, joinNode1);

        ExtensionalDataNode table2 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table2", 2), x, y));
        queryBuilder.addChild(joinNode1, table2);

        ExtensionalDataNode table3 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table3", 2), x, y));
        queryBuilder.addChild(joinNode1, table3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode3);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode3, joinNode2);

        ExtensionalDataNode table4 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table4", 2), x, y));
        queryBuilder.addChild(joinNode2, table4);

        ExtensionalDataNode table5 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table5", 2), x, y));
        queryBuilder.addChild(joinNode2, table5);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = INNER_JOIN_OPTIMIZER.optimize(initialQuery);
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof UnionNode);
        assertEquals(optimizedQuery.getChildren(viceRootNode).size(), 2);
    }

    @Test(expected = EmptyQueryException.class)
    public void testInvalidLeftPartOfLeftJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(x,y));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                DATA_FACTORY.getAtomPredicate("ans1", 2), x, y);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableExpression falseCondition = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND, falseValue, falseValue);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, ljNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(ljNode, joinNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);

        ExtensionalDataNode table2 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table2", 2), x, y));
        queryBuilder.addChild(joinNode, table2);

        ExtensionalDataNode table3 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table3", 2), x, y));
        queryBuilder.addChild(joinNode, table3);

        ExtensionalDataNode table4 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(DATA_FACTORY.getAtomPredicate("table4", 1), x));
        queryBuilder.addChild(ljNode, table4, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);


        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        /**
         * Should throw the EmptyQueryException
         */
        IntermediateQuery optimizedQuery = INNER_JOIN_OPTIMIZER.optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }
}
