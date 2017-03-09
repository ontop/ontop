package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.ExpressionOperation.NEQ;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static java.util.Optional.empty;
import static org.junit.Assert.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class TrueNodesRemovalOptimizerTest {

    private final AtomPredicate TABLE1_ARITY_1_PREDICATE = new AtomPredicateImpl("table1", 1);
    private final AtomPredicate TABLE2_ARITY_1_PREDICATE = new AtomPredicateImpl("table2", 1);
    private final AtomPredicate TABLE3_ARITY_2_PREDICATE = new AtomPredicateImpl("table3", 2);

    private final AtomPredicate ANS1_ARITY_0_PREDICATE = new AtomPredicateImpl("ans1", 0);
    private final AtomPredicate ANS1_ARITY_1_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private final AtomPredicate ANS1_ARITY_2_PREDICATE = new AtomPredicateImpl("ans1", 2);

    private final Variable A = DATA_FACTORY.getVariable("a");
    private final Variable B = DATA_FACTORY.getVariable("b");
    private final Variable X = DATA_FACTORY.getVariable("x");
    private final Variable Y = DATA_FACTORY.getVariable("y");

    private ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_ARITY_1_PREDICATE, A));
    private ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_ARITY_1_PREDICATE, B));
    private ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_ARITY_2_PREDICATE, A, B));

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER),
                argument);
    }


    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent1() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, jn);

        TrueNode trueNode = new TrueNodeImpl();
        queryBuilder.addChild(jn, trueNode);
        queryBuilder.addChild(jn, DATA_NODE_1);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent2() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        ImmutableExpression expression = DATA_FACTORY.getImmutableExpression(NEQ, A, B);
        InnerJoinNode jn = new InnerJoinNodeImpl(Optional.of(expression));
        queryBuilder.addChild(rootNode, jn);

        TrueNode trueNode = new TrueNodeImpl();
        queryBuilder.addChild(jn, trueNode);
        queryBuilder.addChild(jn, DATA_NODE_3);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        FilterNode filterNode = new FilterNodeImpl(expression);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, filterNode);
        expectedQueryBuilder.addChild(filterNode, DATA_NODE_3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent3() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A), Y, generateInt(B))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, jn);

        queryBuilder.addChild(jn, new TrueNodeImpl());
        queryBuilder.addChild(jn, DATA_NODE_1);
        queryBuilder.addChild(jn, DATA_NODE_2);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, jn);
        expectedQueryBuilder.addChild(jn, DATA_NODE_1);
        expectedQueryBuilder.addChild(jn, DATA_NODE_2);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSingleTrueNodeRemoval_leftJoinParent() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode ljn = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, ljn);

        queryBuilder.addChild(ljn, DATA_NODE_1, LEFT);
        queryBuilder.addChild(ljn, new TrueNodeImpl(), RIGHT);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testSingleTrueNodeRemoval_constructionNodeParent() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        UnionNode un = new UnionNodeImpl(ImmutableSet.of());
        queryBuilder.addChild(rootNode, un);

        ConstructionNode cn = new ConstructionNodeImpl(ImmutableSet.of());
        queryBuilder.addChild(un, cn);
        queryBuilder.addChild(un, DATA_NODE_1);
        queryBuilder.addChild(cn, new TrueNodeImpl());

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, un);
        expectedQueryBuilder.addChild(un, new TrueNodeImpl());
        expectedQueryBuilder.addChild(un, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testSingleTrueNodeChainRemoval() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);
        InnerJoinNode jn = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, jn);
        ConstructionNode cn = new ConstructionNodeImpl(ImmutableSet.of());
        queryBuilder.addChild(jn,cn);
        queryBuilder.addChild(jn, DATA_NODE_1);
        queryBuilder.addChild(cn, new TrueNodeImpl());

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    @Test
    public void testSingleTrueNodeNonRemoval_leftJoinParent() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode ljn = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, ljn);
        queryBuilder.addChild(ljn, new TrueNodeImpl(), LEFT);
        queryBuilder.addChild(ljn, DATA_NODE_1, RIGHT);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, ljn);
        expectedQueryBuilder.addChild(ljn, new TrueNodeImpl(), LEFT);
        expectedQueryBuilder.addChild(ljn, DATA_NODE_1, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSingleTrueNodeNonRemoval_UnionParent() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        UnionNode un = new UnionNodeImpl(ImmutableSet.of());
        queryBuilder.addChild(rootNode, un);
        queryBuilder.addChild(un, DATA_NODE_1);
        queryBuilder.addChild(un, new TrueNodeImpl());

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, un);
        expectedQueryBuilder.addChild(un, DATA_NODE_1);
        expectedQueryBuilder.addChild(un, new TrueNodeImpl());

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testMultipleTrueNodesRemoval1() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn1 = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, jn1);

        queryBuilder.addChild(jn1, new TrueNodeImpl());
        InnerJoinNode jn2 = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(jn1, jn2);
        queryBuilder.addChild(jn2, new TrueNodeImpl());
        queryBuilder.addChild(jn2, DATA_NODE_1);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testMultipleTrueNodesRemoval2() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = new InnerJoinNodeImpl(empty());
        queryBuilder.addChild(rootNode, jn);
        queryBuilder.addChild(jn, new TrueNodeImpl());
        LeftJoinNode ljn = new LeftJoinNodeImpl(empty());
        queryBuilder.addChild(jn, ljn);
        queryBuilder.addChild(ljn, DATA_NODE_1, LEFT);
        queryBuilder.addChild(ljn, new TrueNodeImpl(), RIGHT);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNodes removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNodes removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    @Test
    public void testTrueNodesPartialRemoval1() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = new InnerJoinNodeImpl(empty());
        queryBuilder.addChild(rootNode, jn);
        queryBuilder.addChild(jn, new TrueNodeImpl());
        LeftJoinNode ljn = new LeftJoinNodeImpl(empty());
        queryBuilder.addChild(jn, ljn);
        queryBuilder.addChild(ljn, new TrueNodeImpl(), LEFT);
        queryBuilder.addChild(ljn, DATA_NODE_1, RIGHT);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNodes removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, ljn);
        expectedQueryBuilder.addChild(ljn, new TrueNodeImpl(), LEFT);
        expectedQueryBuilder.addChild(ljn, DATA_NODE_1, RIGHT);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNodes removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testTrueNodesPartialRemoval2() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_0_PREDICATE);
        ConstructionNode rootNode = new ConstructionNodeImpl(ImmutableSet.of());
        queryBuilder.init(projectionAtom, rootNode);


        InnerJoinNode jn = new InnerJoinNodeImpl(empty());
        queryBuilder.addChild(rootNode, jn);
        queryBuilder.addChild(jn, new TrueNodeImpl());
        UnionNode un = new UnionNodeImpl(ImmutableSet.of());
        queryBuilder.addChild(jn, un);
        queryBuilder.addChild(un, new TrueNodeImpl());
        queryBuilder.addChild(un, DATA_NODE_1);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore true Node removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, un);
        expectedQueryBuilder.addChild(un, new TrueNodeImpl());
        expectedQueryBuilder.addChild(un, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer substitutionOptimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after TrueNode Removal: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

}
