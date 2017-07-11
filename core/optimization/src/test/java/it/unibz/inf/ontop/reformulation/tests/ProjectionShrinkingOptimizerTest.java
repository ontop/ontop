package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.ExpressionOperation;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.ProjectionShrinkingOptimizer;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import org.junit.Test;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.predicate.Predicate.COL_TYPE.INTEGER;
import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class ProjectionShrinkingOptimizerTest {
    private final static AtomPredicate TABLE1_PREDICATE = DATA_FACTORY.getAtomPredicate("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = DATA_FACTORY.getAtomPredicate("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = DATA_FACTORY.getAtomPredicate("table3", 2);
    private final static AtomPredicate TABLE4_PREDICATE = DATA_FACTORY.getAtomPredicate("table4", 3);
    private final static AtomPredicate TABLE5_PREDICATE = DATA_FACTORY.getAtomPredicate("table5", 3);
    private final static AtomPredicate ANS1_PREDICATE1 = DATA_FACTORY.getAtomPredicate("ans1", 1);
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable W = DATA_FACTORY.getVariable("W");
    private final static Variable A = DATA_FACTORY.getVariable("A");
    private final static Variable B = DATA_FACTORY.getVariable("B");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Y, Z);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, W, X);


    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER),
                argument
        );
    }

    @Test
    public void testUnion() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        queryBuilder2.init(projectionAtom1, constructionNode2);
        queryBuilder2.addChild(constructionNode2, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode1);
        queryBuilder2.addChild(unionNode2, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndImplicitJoinCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery query2 = query1.createSnapshot();

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndImplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);


        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode2 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, innerJoinNode2);
        queryBuilder2.addChild(innerJoinNode2, dataNode1);
        queryBuilder2.addChild(innerJoinNode2, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode2);
        queryBuilder2.addChild(unionNode2, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void testUnionAndExplicitJoinCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        IntermediateQuery query2 = query1.createSnapshot();
        System.out.println("\nBefore optimization: \n" +  query1);


        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndExplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION2);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, W, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();
        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, innerJoinNode1);
        queryBuilder2.addChild(innerJoinNode1, dataNode1);
        queryBuilder2.addChild(innerJoinNode1, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode2);
        queryBuilder2.addChild(unionNode2, dataNode3);


        IntermediateQuery query2 = queryBuilder2.build();


        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndFilter() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y,Z));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode1);
        queryBuilder1.addChild(filterNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode4);
        queryBuilder1.addChild(unionNode1, dataNode5);

        IntermediateQuery query1 = queryBuilder1.build();
        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(Y,Z));

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, filterNode1);
        queryBuilder2.addChild(filterNode1, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode4);
        queryBuilder2.addChild(unionNode2, dataNode5);


        IntermediateQuery query2 = queryBuilder2.build();


        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNode() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                DATA_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                DATA_FACTORY.getSubstitution(X,generateInt(A)));
        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNodeAndImplicitJoinCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                DATA_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery query2 = query1.createSnapshot();

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNodeAndImplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                DATA_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                DATA_FACTORY.getSubstitution(X,generateInt(A)));


        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, innerJoinNode1);
        queryBuilder2.addChild(innerJoinNode1, dataNode1);
        queryBuilder2.addChild(innerJoinNode1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
}
