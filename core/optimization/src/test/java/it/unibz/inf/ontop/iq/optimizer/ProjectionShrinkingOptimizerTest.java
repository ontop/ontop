package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
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

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getStrictNEquality(Y, Z);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getStrictNEquality(W, X);


    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.INTEGER);

    }

    @Test
    public void testUnion() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2,
                ImmutableMap.of(0, X));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2,
                ImmutableMap.of(0, X));

        queryBuilder2.init(projectionAtom1, unionNode2);
        queryBuilder2.addChild(unionNode2, newDataNode1);
        queryBuilder2.addChild(unionNode2, newDataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    private static IntermediateQuery optimize(IntermediateQuery initialQuery) throws EmptyQueryException {
        IQ initialIQ = IQ_CONVERTER.convert(initialQuery);
        return IQ_CONVERTER.convert(
                initialIQ.normalizeForOptimization());
    }

    @Test
    public void testUnionAndImplicitJoinCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2,
                ImmutableMap.of(0, Y));

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery query2 = query1.createSnapshot();

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndImplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        InnerJoinNode innerJoinNode2 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        queryBuilder2.init(projectionAtom2, innerJoinNode2);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2,
                ImmutableMap.of(0, X));

        queryBuilder2.addChild(innerJoinNode2, newDataNode1);
        queryBuilder2.addChild(innerJoinNode2, unionNode2);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2,
                ImmutableMap.of(0, X));

        queryBuilder2.addChild(unionNode2, newDataNode2);

        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2,
                ImmutableMap.of(0, X));

        queryBuilder2.addChild(unionNode2, newDataNode3);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void testUnionAndExplicitJoinCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        IntermediateQuery query2 = query1.createSnapshot();
        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndExplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION2);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(W, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();
        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2,
                ImmutableMap.of(0, X));
        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2,
                ImmutableMap.of(0, X));

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, innerJoinNode1);
        queryBuilder2.addChild(innerJoinNode1, dataNode1);
        queryBuilder2.addChild(innerJoinNode1, unionNode2);
        queryBuilder2.addChild(unionNode2, newDataNode2);
        queryBuilder2.addChild(unionNode2, newDataNode3);


        IntermediateQuery query2 = queryBuilder2.build();


        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndFilter() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y,Z));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode1);
        queryBuilder1.addChild(filterNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode4);
        queryBuilder1.addChild(unionNode1, dataNode5);

        IntermediateQuery query1 = queryBuilder1.build();
        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(Y,Z));

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, filterNode1);
        queryBuilder2.addChild(filterNode1, unionNode2);

        ExtensionalDataNode newDataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3,
                ImmutableMap.of(1, Y, 2, Z));
        ExtensionalDataNode newDataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE5_AR3,
                ImmutableMap.of(1, Y, 2, Z));

        queryBuilder2.addChild(unionNode2, newDataNode4);
        queryBuilder2.addChild(unionNode2, newDataNode5);


        IntermediateQuery query2 = queryBuilder2.build();


        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNode() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A)));
        queryBuilder2.init(projectionAtom1, constructionNode3);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        queryBuilder2.addChild(constructionNode3, newDataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNodeAndImplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        Variable XF0 = TERM_FACTORY.getVariable("Xf0");

        InnerJoinNode newInnerJoinNode1 = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getRDFFunctionalTerm(A,
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdIntegerDatatype())), XF0));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2,
                ImmutableMap.of(0, XF0,1, Z));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2,
                ImmutableMap.of(0, A));


        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, newInnerJoinNode1);
        queryBuilder2.addChild(newInnerJoinNode1, newDataNode1);
        queryBuilder2.addChild(newInnerJoinNode1, newDataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
}
