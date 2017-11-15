package it.unibz.inf.ontop.iq.optimizer;

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

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.NEQ;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static org.junit.Assert.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class TrueNodesRemovalOptimizerTest {

    private final AtomPredicate TABLE1_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 1);
    private final AtomPredicate TABLE2_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 1);
    private final AtomPredicate TABLE3_ARITY_2_PREDICATE = ATOM_FACTORY.getAtomPredicate("table3", 2);

    private final AtomPredicate ANS1_ARITY_0_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 0);
    private final AtomPredicate ANS1_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private final AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 2);

    private final Variable A = TERM_FACTORY.getVariable("a");
    private final Variable B = TERM_FACTORY.getVariable("b");
    private final Variable X = TERM_FACTORY.getVariable("x");
    private final Variable Y = TERM_FACTORY.getVariable("y");

    private ExtensionalDataNode DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_ARITY_1_PREDICATE, A));
    private ExtensionalDataNode DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_ARITY_1_PREDICATE, B));
    private ExtensionalDataNode DATA_NODE_3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_ARITY_2_PREDICATE, A, B));

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(XSD.INTEGER),
                argument);
    }


    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent1() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn);

        TrueNode trueNode = IQ_FACTORY.createTrueNode();
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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(NEQ, A, B);
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode(expression);
        queryBuilder.addChild(rootNode, jn);

        TrueNode trueNode = IQ_FACTORY.createTrueNode();
        queryBuilder.addChild(jn, trueNode);
        queryBuilder.addChild(jn, DATA_NODE_3);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);
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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A), Y, generateInt(B)));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn);

        queryBuilder.addChild(jn, IQ_FACTORY.createTrueNode());
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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, ljn);

        queryBuilder.addChild(ljn, DATA_NODE_1, LEFT);
        queryBuilder.addChild(ljn, IQ_FACTORY.createTrueNode(), RIGHT);

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
    public void testSingleTrueNodeChainRemoval() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn);
        ConstructionNode cn = IQ_FACTORY.createConstructionNode(ImmutableSet.of());
        queryBuilder.addChild(jn,cn);
        queryBuilder.addChild(jn, DATA_NODE_1);
        queryBuilder.addChild(cn, IQ_FACTORY.createTrueNode());

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode optimization: \n" + unOptimizedQuery);

        IntermediateQuery expectedQuery = unOptimizedQuery.createSnapshot();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer optimizer = new TrueNodesRemovalOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(unOptimizedQuery);

        System.out.println("\nQuery after: \n" + optimizedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    @Test
    public void testSingleTrueNodeNonRemoval_leftJoinParent() throws EmptyQueryException {

        //Unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, ljn);
        queryBuilder.addChild(ljn, IQ_FACTORY.createTrueNode(), LEFT);
        queryBuilder.addChild(ljn, DATA_NODE_1, RIGHT);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, ljn);
        expectedQueryBuilder.addChild(ljn, IQ_FACTORY.createTrueNode(), LEFT);
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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, NULL));
        queryBuilder.init(projectionAtom, rootNode);

        UnionNode un = IQ_FACTORY.createUnionNode(ImmutableSet.of());
        queryBuilder.addChild(rootNode, un);
        queryBuilder.addChild(un, DATA_NODE_1);
        queryBuilder.addChild(un, IQ_FACTORY.createTrueNode());

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNode removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, un);
        expectedQueryBuilder.addChild(un, DATA_NODE_1);
        expectedQueryBuilder.addChild(un, IQ_FACTORY.createTrueNode());

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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn1);

        queryBuilder.addChild(jn1, IQ_FACTORY.createTrueNode());
        InnerJoinNode jn2 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(jn1, jn2);
        queryBuilder.addChild(jn2, IQ_FACTORY.createTrueNode());
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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn);
        queryBuilder.addChild(jn, IQ_FACTORY.createTrueNode());
        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(jn, ljn);
        queryBuilder.addChild(ljn, DATA_NODE_1, LEFT);
        queryBuilder.addChild(ljn, IQ_FACTORY.createTrueNode(), RIGHT);

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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn);
        queryBuilder.addChild(jn, IQ_FACTORY.createTrueNode());
        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(jn, ljn);
        queryBuilder.addChild(ljn, IQ_FACTORY.createTrueNode(), LEFT);
        queryBuilder.addChild(ljn, DATA_NODE_1, RIGHT);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore TrueNodes removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, ljn);
        expectedQueryBuilder.addChild(ljn, IQ_FACTORY.createTrueNode(), LEFT);
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
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_0_PREDICATE);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of());
        queryBuilder.init(projectionAtom, rootNode);


        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, jn);
        queryBuilder.addChild(jn, IQ_FACTORY.createTrueNode());
        UnionNode un = IQ_FACTORY.createUnionNode(ImmutableSet.of());
        queryBuilder.addChild(jn, un);
        queryBuilder.addChild(un, IQ_FACTORY.createTrueNode());
        queryBuilder.addChild(un, DATA_NODE_1);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore true Node removal: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, un);
        expectedQueryBuilder.addChild(un, IQ_FACTORY.createTrueNode());
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
