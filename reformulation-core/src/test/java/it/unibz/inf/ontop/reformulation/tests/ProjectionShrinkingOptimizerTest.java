package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.ProjectionShrinkingOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.ProjectionShrinkingOptimizerImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static junit.framework.TestCase.assertTrue;

public class ProjectionShrinkingOptimizerTest {
    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 2);
    private final static AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 3);
    private final static AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 3);
    private final static AtomPredicate ANS1_PREDICATE1 = new AtomPredicateImpl("ans1", 1);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
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

    private final MetadataForQueryOptimization metadata;

    public ProjectionShrinkingOptimizerTest() {
        this.metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        return new EmptyMetadataForQueryOptimization();
    }


    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER),
                argument);
    }

    @Test
    public void testUnion() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(X));

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

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery query2 = IntermediateQueryUtils.convertToBuilder(query1).build();

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndImplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);


        ProjectionShrinkingOptimizerImpl projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom2 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode2 = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(X));

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

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION1));
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        IntermediateQuery query2 = IntermediateQueryUtils.convertToBuilder(query1).build();
        System.out.println("\nBefore optimization: \n" +  query1);


        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testUnionAndExplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION2));
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, W, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode2);
        queryBuilder1.addChild(unionNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();
        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(X));

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

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        FilterNode filterNode1 = new FilterNodeImpl(EXPRESSION1);
        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(X,Y,Z));
        ExtensionalDataNode dataNode4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode5 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, filterNode1);
        queryBuilder1.addChild(filterNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode4);
        queryBuilder1.addChild(unionNode1, dataNode5);

        IntermediateQuery query1 = queryBuilder1.build();
        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(Y,Z));

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

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X,generateInt(A),Y,generateInt(B))), Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X,generateInt(A))), Optional.empty());
        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNodeAndImplicitJoinCondition1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X,generateInt(A),Y,generateInt(B))), Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery query2 = IntermediateQueryUtils.convertToBuilder(query1).build();

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testConstructionNodeAndImplicitJoinCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X,generateInt(A),Y,generateInt(B))), Optional.empty());
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        queryBuilder1.addChild(innerJoinNode1, dataNode1);
        queryBuilder1.addChild(innerJoinNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        ProjectionShrinkingOptimizer projectionShrinkingOptimizer = new ProjectionShrinkingOptimizerImpl();
        IntermediateQuery optimizedQuery = projectionShrinkingOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X,generateInt(A))), Optional.empty());


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
