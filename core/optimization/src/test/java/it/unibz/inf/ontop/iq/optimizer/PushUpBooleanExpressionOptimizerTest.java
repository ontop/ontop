package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GTE;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static junit.framework.TestCase.assertTrue;

public class PushUpBooleanExpressionOptimizerTest {
    
    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private final static AtomPredicate ANS1_PREDICATE3 = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static Variable U = TERM_FACTORY.getVariable("U");
    private final static Variable V = TERM_FACTORY.getVariable("V");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getDBNonStrictNumericEquality(X, Z);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getStrictNEquality(Y, Z);
    private final static ImmutableExpression EXPRESSION3 = TERM_FACTORY.getDBDefaultInequality(
            GTE, W, Z);
    private final static ImmutableExpression EXPRESSION4 = TERM_FACTORY.getDBDefaultInequality(
            LT, V, W);
    private final static ImmutableExpression EXPRESSION5 = TERM_FACTORY.getStrictNEquality(X, TERM_FACTORY.getDBStringConstant("a"));

    @Test
    public void testPropagationFomInnerJoinProvider() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getConjunction (EXPRESSION1, EXPRESSION2)
        );
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE4_AR3, X, Y, Z)
        );
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y)
        );
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_AR2, Z, W)
        );

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, joinNode2);
        queryBuilder1.addChild(joinNode2, dataNode2);
        queryBuilder1.addChild(joinNode2, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, dataNode2);
        queryBuilder2.addChild(joinNode2, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomInnerJoinProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getConjunction (EXPRESSION1, EXPRESSION2)
        );
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE2_AR2, Z, W));

        queryBuilder1.init(projectionAtom, unionNode);
        queryBuilder1.addChild(unionNode, dataNode1);
        queryBuilder1.addChild(unionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationFomFilterNodeProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE1_AR2, X, Y));

        queryBuilder1.init(projectionAtom, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, filterNode);
        queryBuilder1.addChild(filterNode, dataNode1);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION1));

        queryBuilder2.init(projectionAtom, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomFilterNodeProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE3_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE4_AR3, X, Y, Z));

        queryBuilder1.init(projectionAtom, unionNode);
        queryBuilder1.addChild(unionNode, dataNode1);
        queryBuilder1.addChild(unionNode, filterNode);
        queryBuilder1.addChild(filterNode, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomLeftJoinProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction (EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode (ATOM_FACTORY.getDataAtom (TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom (TABLE2_AR2, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, leftJoinNode);
        queryBuilder1.addChild(leftJoinNode, dataNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode3, RIGHT);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testPropagationToExistingFilterRecipient() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction
                (EXPRESSION2));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, W, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, filterNode1);
        queryBuilder1.addChild(filterNode1, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION3, EXPRESSION2));


        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testRecursivePropagation() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION1));
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION3);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, W, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, joinNode2);
        queryBuilder1.addChild(joinNode2, dataNode2);
        queryBuilder1.addChild(joinNode2, filterNode);
        queryBuilder1.addChild(filterNode, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION3));

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode3);
        queryBuilder2.addChild(joinNode3, dataNode1);
        queryBuilder2.addChild(joinNode3, dataNode2);
        queryBuilder2.addChild(joinNode3, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void testPropagationToLeftJoinRecipient() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(Optional.empty());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, joinNode1, RIGHT);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.empty());

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
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(Optional.empty());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, leftJoinNode);
        queryBuilder1.addChild(leftJoinNode, joinNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode1, RIGHT);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.empty());

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

    @Ignore("TODO: support it")
    @Test
    public void testCompletePropagationThroughUnion() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, W, X, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, unionNode1);
        queryBuilder1.addChild(unionNode1, filterNode1);
        queryBuilder1.addChild(unionNode1, filterNode2);
        queryBuilder1.addChild(filterNode1, dataNode1);
        queryBuilder1.addChild(filterNode2, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        FilterNode filterNode3 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Z));

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, filterNode3);
        queryBuilder2.addChild(filterNode3, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode1);
        queryBuilder2.addChild(unionNode2, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationThroughUnion() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, W, X, Z));

        queryBuilder1.init(projectionAtom, unionNode1);
        queryBuilder1.addChild(unionNode1, filterNode1);
        queryBuilder1.addChild(unionNode1, filterNode2);
        queryBuilder1.addChild(filterNode1, dataNode1);
        queryBuilder1.addChild(filterNode2, dataNode2);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQuery query2 = query1.createSnapshot();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Ignore("Shall we support it?")
    @Test
    public void testPartialPropagationThroughUnion() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION3));
        FilterNode filterNode3 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR3, X, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, W, X, Z));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_AR3, X, V, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, unionNode1);
        queryBuilder1.addChild(unionNode1, filterNode1);
        queryBuilder1.addChild(unionNode1, filterNode2);
        queryBuilder1.addChild(unionNode1, filterNode3);
        queryBuilder1.addChild(filterNode1, dataNode1);
        queryBuilder1.addChild(filterNode2, dataNode2);
        queryBuilder1.addChild(filterNode3, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        FilterNode filterNode4 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode5 = IQ_FACTORY.createFilterNode(EXPRESSION2);
        FilterNode filterNode6 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Z));

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, filterNode4);
        queryBuilder2.addChild(filterNode4, unionNode2);
        queryBuilder2.addChild(unionNode2, filterNode5);
        queryBuilder2.addChild(unionNode2, filterNode6);
        queryBuilder2.addChild(unionNode2, dataNode3);
        queryBuilder2.addChild(filterNode5, dataNode1);
        queryBuilder2.addChild(filterNode6, dataNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Ignore("TODO: support it")
    @Test
    public void testMultiplePropagationsThroughUnion() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI(Y))));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(Y));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION3, EXPRESSION5));
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        FilterNode filterNode3 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION3, EXPRESSION2));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR3, X, W, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, W, Y, Z));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE7_AR4, Y, W, U, Z));

        queryBuilder1.init(projectionAtom, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, filterNode1);
        queryBuilder1.addChild(unionNode1, constructionNode2);
        queryBuilder1.addChild(filterNode1, dataNode1);
        queryBuilder1.addChild(constructionNode2, unionNode2);
        queryBuilder1.addChild(unionNode2, filterNode2);
        queryBuilder1.addChild(unionNode2, filterNode3);
        queryBuilder1.addChild(filterNode2, dataNode2);
        queryBuilder1.addChild(filterNode3, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        FilterNode filterNode4 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        FilterNode filterNode5 = IQ_FACTORY.createFilterNode(EXPRESSION5);
        FilterNode filterNode6 = IQ_FACTORY.createFilterNode(EXPRESSION2);
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(W, X, Z));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(W, Y, Z));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, W, Z), SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI(Y))));

        queryBuilder2.init(projectionAtom, constructionNode1);
        queryBuilder2.addChild(constructionNode1, filterNode4);
        queryBuilder2.addChild(filterNode4, unionNode3);
        queryBuilder2.addChild(unionNode3, filterNode5);
        queryBuilder2.addChild(unionNode3, constructionNode3);
        queryBuilder2.addChild(filterNode5, dataNode1);
        queryBuilder2.addChild(constructionNode3, unionNode4);
        queryBuilder2.addChild(unionNode4, dataNode2);
        queryBuilder2.addChild(unionNode4, filterNode6);
        queryBuilder2.addChild(filterNode6, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    private static ImmutableFunctionalTerm generateURI(VariableOrGroundTerm... arguments) {
        String uriTemplateString = "http://example.org/ds1/";
        for (VariableOrGroundTerm argument : arguments) {
            uriTemplateString = uriTemplateString.toString() + "{}";
        }
        ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
        builder.add(arguments);
        return TERM_FACTORY.getIRIFunctionalTerm(uriTemplateString, builder.build());
    }

    private IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IQ newIQ = IQ_CONVERTER.convert(query).normalizeForOptimization();
        return IQ_CONVERTER.convert(newIQ, query.getExecutorRegistry());
    }
}
