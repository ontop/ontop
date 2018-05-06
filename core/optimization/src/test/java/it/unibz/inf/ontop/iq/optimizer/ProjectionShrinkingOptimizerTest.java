package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class ProjectionShrinkingOptimizerTest {

    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable B = TERM_FACTORY.getVariable("B");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Y, Z);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, W, X);


    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(XSD.INTEGER),
                argument
        );
    }

    @Test
    public void testUnion() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));

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
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR2, X, Y));

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
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR2, X, Y));

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
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR2, X, Y));

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
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, W, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR2, X, Y));

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
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_AR3, X, Y, Z));

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
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B));

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
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A)));
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
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B));

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
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B));

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
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A)));


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
