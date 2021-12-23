package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.*;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: test
 */
public class NodeDeletionTest {

    @Test(expected = EmptyQueryException.class)
    public void testSimpleJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = TERM_FACTORY.getVariable("x");
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(x));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 1), x);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, rootNode);

        DBConstant falseValue = TERM_FACTORY.getDBBooleanConstant(false);
        ImmutableExpression falseCondition = TERM_FACTORY.getIsTrue(falseValue);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode table1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(x));
        queryBuilder.addChild(joinNode, table1);

        ExtensionalDataNode table2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(x));
        queryBuilder.addChild(joinNode, table2);

        IQ initialQuery = queryBuilder.buildIQ();
        System.out.println("Initial query: " + initialQuery.toString());

        /*
         * Should throw the EmptyQueryException
         */
        IQ optimizedQuery = optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }

    @Test
    public void testInvalidRightPartOfLeftJoin1() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable y = TERM_FACTORY.getVariable("y");

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(x,y));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), x, y);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, rootNode);

        DBConstant falseValue = TERM_FACTORY.getDBBooleanConstant(false);
        ImmutableExpression falseCondition = TERM_FACTORY.getIsTrue(falseValue);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, ljNode);

        ExtensionalDataNode table1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(x));
        queryBuilder.addChild(ljNode, table1, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(ljNode, joinNode, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        ExtensionalDataNode table2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode, table2);

        ExtensionalDataNode table3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode, table3);

        IQ initialQuery = queryBuilder.buildIQ();
        System.out.println("Initial query: " + initialQuery.toString());

        /*
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = IQ_CONVERTER.convert(optimize(initialQuery));
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootNode()).get();
        assertTrue(viceRootNode instanceof ExtensionalDataNode);
        assertEquals(((ExtensionalDataNode) viceRootNode).getRelationDefinition(), TABLE1_AR1);
        assertTrue(optimizedQuery.getChildren(viceRootNode).isEmpty());
    }

    @Test
    public void testUnion1() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable y = TERM_FACTORY.getVariable("y");

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), x, y);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, rootNode);

        DBConstant falseValue = TERM_FACTORY.getDBBooleanConstant(false);
        ImmutableExpression falseCondition = TERM_FACTORY.getIsTrue(falseValue);

        UnionNode topUnion = IQ_FACTORY.createUnionNode(projectedVariables);
        queryBuilder.addChild(rootNode, topUnion);

        //DistinctVariableOnlyDataAtom subAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getAtomPredicate("ansu1", 2), x, y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode1);

        ExtensionalDataNode table1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(constructionNode1, table1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode2);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(constructionNode2, joinNode1);

        ExtensionalDataNode table2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode1, table2);

        ExtensionalDataNode table3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode1, table3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode3);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(constructionNode3, joinNode2);

        ExtensionalDataNode table4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode2, table4);

        ExtensionalDataNode table5 = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode2, table5);

        IQ initialQuery = queryBuilder.buildIQ();
        System.out.println("Initial query: " + initialQuery.toString());

        /*
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = IQ_CONVERTER.convert(optimize(initialQuery));
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode newRootNode = optimizedQuery.getRootNode();
        assertTrue(newRootNode instanceof ExtensionalDataNode);
        assertEquals(((ExtensionalDataNode) newRootNode).getRelationDefinition(), TABLE1_AR2);
        assertTrue(optimizedQuery.getChildren(newRootNode).isEmpty());
    }

    @Test
    public void testUnion2() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable y = TERM_FACTORY.getVariable("y");

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), x, y);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables);


        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, rootNode);

        DBConstant falseValue = TERM_FACTORY.getDBBooleanConstant(false);
        ImmutableExpression falseCondition = TERM_FACTORY.getIsTrue(falseValue);

        UnionNode topUnion = IQ_FACTORY.createUnionNode(projectedVariables);
        queryBuilder.addChild(rootNode, topUnion);

        //DataAtom subAtom = ATOM_FACTORY.getDataAtom(ATOM_FACTORY.getRDFAnswerPredicate("ansu1", 2), x, y);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode1);

        ExtensionalDataNode table1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(constructionNode1, table1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode2);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(constructionNode2, joinNode1);

        ExtensionalDataNode table2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode1, table2);

        ExtensionalDataNode table3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode1, table3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.addChild(topUnion, constructionNode3);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode3, joinNode2);

        ExtensionalDataNode table4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode2, table4);

        ExtensionalDataNode table5 = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode2, table5);

        IQ initialQuery = queryBuilder.buildIQ();
        System.out.println("Initial query: " + initialQuery.toString());

        /*
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = IQ_CONVERTER.convert(optimize(initialQuery));
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode optimizedRootNode = optimizedQuery.getRootNode();
        assertTrue(optimizedRootNode instanceof UnionNode);
        assertEquals(2, optimizedQuery.getChildren(optimizedRootNode).size());
    }

    @Test(expected = EmptyQueryException.class)
    public void testInvalidLeftPartOfLeftJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable y = TERM_FACTORY.getVariable("y");

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(x,y));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), x, y);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, rootNode);

        DBConstant falseValue = TERM_FACTORY.getDBBooleanConstant(false);
        ImmutableExpression falseCondition = TERM_FACTORY.getIsTrue(falseValue);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, ljNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(falseCondition);
        queryBuilder.addChild(ljNode, joinNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);

        ExtensionalDataNode table2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode, table2);

        ExtensionalDataNode table3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(x, y));
        queryBuilder.addChild(joinNode, table3);

        ExtensionalDataNode table4 = createExtensionalDataNode(TABLE4_AR1, ImmutableList.of(x));
        queryBuilder.addChild(ljNode, table4, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);


        IQ initialQuery = queryBuilder.buildIQ();
        System.out.println("Initial query: " + initialQuery.toString());

        /*
         * Should throw the EmptyQueryException
         */
        IQ optimizedQuery = optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }

    private IQ optimize(IQ initialIQ) throws EmptyQueryException {
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        if (optimizedIQ.getTree().isDeclaredAsEmpty())
            throw new EmptyQueryException();

        return optimizedIQ;
    }
}
