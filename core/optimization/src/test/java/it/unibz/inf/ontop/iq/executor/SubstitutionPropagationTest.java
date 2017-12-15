package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the substitution propagation
 */
public class SubstitutionPropagationTest {


    private static final AtomPredicate TABLE1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 2);
    private static final AtomPredicate TABLE2_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 2);
    private static final AtomPredicate TABLE3_PREDICATE = ATOM_FACTORY.getAtomPredicate("table3", 2);
    private static final AtomPredicate TABLE4_PREDICATE = ATOM_FACTORY.getAtomPredicate("table4", 2);
    private static final AtomPredicate TABLE5_PREDICATE = ATOM_FACTORY.getAtomPredicate("table5", 2);
    private static final AtomPredicate TABLE6_PREDICATE = ATOM_FACTORY.getAtomPredicate("table6", 2);

    private static final AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private static final AtomPredicate ANS1_PREDICATE_2 = ATOM_FACTORY.getAtomPredicate("ans1", 2);


    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable W = TERM_FACTORY.getVariable("w");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable D = TERM_FACTORY.getVariable("d");
    private static final Variable E = TERM_FACTORY.getVariable("e");
    private static final Variable F = TERM_FACTORY.getVariable("f");
    private static final Variable G = TERM_FACTORY.getVariable("g");
    private static final Variable H = TERM_FACTORY.getVariable("h");
    private static final Variable I = TERM_FACTORY.getVariable("i");
    private static final Variable L = TERM_FACTORY.getVariable("l");
    private static final Variable M = TERM_FACTORY.getVariable("m");
    private static final Variable N = TERM_FACTORY.getVariable("n");
    private static final ValueConstant ONE = TERM_FACTORY.getConstantLiteral("1", TYPE_FACTORY.getXsdIntegerDatatype());
    private static final ValueConstant TWO = TERM_FACTORY.getConstantLiteral("2", TYPE_FACTORY.getXsdIntegerDatatype());


    private static final URITemplatePredicate URI1_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(2);
    private static final URITemplatePredicate URI2_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(3);
    private static final Constant URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static final Constant URI_TEMPLATE_STR_2 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");

    private static final ExtensionalDataNode DATA_NODE_1 = buildExtensionalDataNode(TABLE1_PREDICATE, A, B);
    private static final ExtensionalDataNode DATA_NODE_2 = buildExtensionalDataNode(TABLE2_PREDICATE, C, B);
    private static final ExtensionalDataNode DATA_NODE_3 = buildExtensionalDataNode(TABLE3_PREDICATE, C, D);
    private static final ExtensionalDataNode DATA_NODE_4 = buildExtensionalDataNode(TABLE1_PREDICATE, A, B);
    private static final ExtensionalDataNode DATA_NODE_5 = buildExtensionalDataNode(TABLE2_PREDICATE, C, E);
    private static final ExtensionalDataNode DATA_NODE_6 = buildExtensionalDataNode(TABLE3_PREDICATE, E, F);
    private static final ExtensionalDataNode DATA_NODE_7 = buildExtensionalDataNode(TABLE4_PREDICATE, G, H);

    @Test
    public void testURI1PropOtherBranch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);

        ExtensionalDataNode rightDataNode = buildExtensionalDataNode(TABLE3_PREDICATE, A, D);
        expectedQueryBuilder.addChild(joinNode, rightDataNode);

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertTrue(optionalNextSibling.isPresent());
        assertTrue(optionalNextSibling.get().isSyntacticallyEquivalentTo(rightDataNode));
    }

    @Test(expected = EmptyQueryException.class)
    public void testURI1PropURI2Branch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        System.out.println("\n Original query: \n" +  initialQuery);
        System.out.println(propagationProposal);

        // Updates the query (in-place optimization)
        initialQuery.applyProposal(propagationProposal);

//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = leftConstructionNode;
//        expectedQueryBuilder.init(projectionAtom, newRootNode);
//        expectedQueryBuilder.addChild(newRootNode, joinNode);
//        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
//        expectedQueryBuilder.addChild(joinNode, new EmptyNodeImpl(ImmutableSet.of(A)));
//
//        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);

    }

    @Test(expected = EmptyQueryException.class)
    public void testURI2PropURI1Branch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        /**
         * Now propagates the right proposal
         */
        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        System.out.println("\n Original query: \n" +  initialQuery);
        System.out.println(propagationProposal);

        // Updates the query (in-place optimization)
        initialQuery.applyProposal(propagationProposal);
    }

    @Test
    public void testURI2PropOtherBranch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        ExtensionalDataNode rightDataNode = buildExtensionalDataNode(TABLE3_PREDICATE, A, B);
        expectedQueryBuilder.addChild(joinNode, rightDataNode);

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());

        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertTrue(optionalNextSibling.isPresent());
        assertTrue(optionalNextSibling.get().isSyntacticallyEquivalentTo(rightDataNode));

        Optional<QueryNode> optionalAncestor = results.getOptionalClosestAncestor();
        assertTrue(optionalAncestor.isPresent());
        assertTrue(optionalAncestor.get().isSyntacticallyEquivalentTo(joinNode));
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C));
        initialQueryBuilder.addChild(rightConstructionNode, initialUnionNode);

        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_3);
        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_5);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, D));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, E));

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertTrue(optionalNextSibling.isPresent());
        assertTrue(optionalNextSibling.get().isSyntacticallyEquivalentTo(newUnionNode));
    }

    @Test
    public void testURI1PropOtherBranchWithUnion2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.addChild(joinNode, initialUnionNode);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(E)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, D));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, F));
        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertTrue(optionalNextSibling.isPresent());
        assertTrue(optionalNextSibling.get().isSyntacticallyEquivalentTo(newUnionNode));
    }


    @Test(expected = EmptyQueryException.class)
    public void testURI1PropOtherBranchWithJoin() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(joinNode, joinNode2);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode2, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(E,F)));
        initialQueryBuilder.addChild(joinNode2, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        System.out.println("\n Original query: \n" +  initialQuery);
        System.out.println(propagationProposal);

        // Updates the query (in-place optimization)
        initialQuery.applyProposal(propagationProposal);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion3() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.init(projectionAtom, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.addChild(joinNode, initialUnionNode);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(E,F)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, D));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C, D));
        initialQueryBuilder.addChild(rightConstructionNode, initialUnionNode);

        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_3);
        initialQueryBuilder.addChild(initialUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, C, D));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, B));

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertTrue(optionalNextSibling.isPresent());
        assertTrue(optionalNextSibling.get().isSyntacticallyEquivalentTo(newUnionNode));
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1Swapped() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));


        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C, D));
        initialQueryBuilder.addChild(leftConstructionNode, initialUnionNode);

        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_1);

        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_3);
        initialQueryBuilder.addChild(initialUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, C, D));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = rightConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, B));

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertFalse(optionalNextSibling.isPresent());
    }
    @Test
    public void testURI2PropOtherBranchWithUnion2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.addChild(joinNode, initialUnionNode);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(E, F)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, B));
        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertTrue(optionalNextSibling.isPresent());
        assertTrue(optionalNextSibling.get().isSyntacticallyEquivalentTo(newUnionNode));
    }


    @Test
    public void testURI2PropOtherBranchWithUnion2Swapped() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.addChild(joinNode, initialUnionNode);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(E, F)));
        initialQueryBuilder.addChild(initialUnionNode, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = rightConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, B));
        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertFalse(optionalNextSibling.isPresent());

    }

    @Test
    public void testUnsatisfiedFilter() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        LeftJoinNode leftJoin = IQ_FACTORY.createLeftJoinNode();
        initialQueryBuilder.init(projectionAtom, leftJoin);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(leftJoin, leftConstructionNode, LEFT);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getImmutableExpression(ExpressionOperation.EQ,
                X, generateURI1(TERM_FACTORY.getConstantLiteral("two"))));
        initialQueryBuilder.addChild(leftJoin, filterNode, RIGHT);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(filterNode, rightConstructionNode);

        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /*
         * Results
         */
        assertFalse(results.getNewNodeOrReplacingChild().isPresent());
        assertFalse(results.getOptionalNextSibling().isPresent());
    }

    @Test
    public void testIncompatibleRightOfLJ() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        initialQueryBuilder.addChild(initialRootNode, leftJoinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        initialQueryBuilder.addChild(leftJoinNode, leftConstructionNode, LEFT);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D),
                        Y, generateURI1(D)));
        initialQueryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        /**
         * Throw from the LJ the substitution of the left construction node
         */
        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode,
                        leftConstructionNode.getSubstitution());


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, NULL));
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);
        expectedQueryBuilder.addChild(expectedRootNode, DATA_NODE_1);

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQueryBuilder.build(),
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Checks the results
         */
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(DATA_NODE_1));
    }

    @Test
    public void testPropagationFromUselessConstructionNode() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        ConstructionNode uselessConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.addChild(initialRootNode, uselessConstructionNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        initialQueryBuilder.addChild(uselessConstructionNode, unionNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C),
                        Y, generateURI1(D)));
        initialQueryBuilder.addChild(unionNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(uselessConstructionNode,
                        leftConstructionNode.getSubstitution());


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(leftConstructionNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQueryBuilder.build(),
                expectedQueryBuilder.build(), propagationProposal);
    }

    @Test(expected = EmptyQueryException.class)
    public void testPropagationFromUselessConstructionNode2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        ConstructionNode uselessConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.addChild(initialRootNode, uselessConstructionNode);

        ConstructionNode thirdConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A,B),
                        Y, NULL));
        initialQueryBuilder.addChild(uselessConstructionNode, thirdConstructionNode);
        initialQueryBuilder.addChild(thirdConstructionNode, DATA_NODE_1);

        ImmutableSubstitution<ImmutableFunctionalTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(
                X, generateURI1(C), Y, generateURI1(D));

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(uselessConstructionNode,
                        substitution);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        System.out.println("\n Original query: \n" +  initialQuery);
        System.out.println(propagationProposal);

        initialQuery.applyProposal(propagationProposal);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1SecondChild() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C));
        initialQueryBuilder.addChild(rightConstructionNode, initialUnionNode);

        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_3);
        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_5);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        ConstructionNode constructNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(B)));
        expectedQueryBuilder.addChild(joinNode, constructNode);
        expectedQueryBuilder.addChild(constructNode, buildExtensionalDataNode(TABLE1_PREDICATE, C, B));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, C, D));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, C, E));

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);

        /**
         * Results
         */
        assertFalse(results.getOptionalNewNode().isPresent());
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        assertTrue(optionalReplacingChild.isPresent());
        assertTrue(optionalReplacingChild.get().isSyntacticallyEquivalentTo(newUnionNode));

        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        assertFalse(optionalNextSibling.isPresent());
    }

    @Test
    public void testEx18() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.init(projectionAtom, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(ONE, TWO)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, IQ_FACTORY.createTrueNode());

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);

        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(A, ONE, B, TWO));

        expectedQueryBuilder.addChild(joinNode, newRightConstructionNode);
        expectedQueryBuilder.addChild(newRightConstructionNode, IQ_FACTORY.createTrueNode());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEx19() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, B)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEx19NoRootConstructionNode() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.init(projectionAtom, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, B)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEx20() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.init(projectionAtom, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, Y));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        ConstructionNode leftSubConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(A)));
        initialQueryBuilder.addChild(leftConstructionNode, leftSubConstructionNode);
        initialQueryBuilder.addChild(leftSubConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ConstructionNode rightSubConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y,C),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Y, generateURI1(D)));
        initialQueryBuilder.addChild(rightConstructionNode, rightSubConstructionNode);
        initialQueryBuilder.addChild(rightSubConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, Y));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, leftSubConstructionNode);
        expectedQueryBuilder.addChild(leftSubConstructionNode, DATA_NODE_1);


        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Y, generateURI1(C)));

        expectedQueryBuilder.addChild(joinNode, newRightConstructionNode);
        expectedQueryBuilder.addChild(newRightConstructionNode, buildExtensionalDataNode(TABLE3_PREDICATE, C, C));

        NodeCentricOptimizationResults<? extends QueryNode> results = propagateAndCompare(initialQuery,
                expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEx21() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, A);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, B, C));

        initialQueryBuilder.addChild(leftConstructionNode, leftDataNode);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A,X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, D));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, A),
                leftConstructionNode.getSubstitution());
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, leftDataNode);

        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(
                        A, B));

        expectedQueryBuilder.addChild(joinNode, newRightConstructionNode);
        expectedQueryBuilder.addChild(newRightConstructionNode, buildExtensionalDataNode(TABLE3_PREDICATE, B, D));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEx22() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(A)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateURI1(D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, A));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    /**
     * Opposite direction to ex22
     */
    @Test
    public void testEx23() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(A)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateURI1(D)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, rightConstructionNode);
        expectedQueryBuilder.addChild(rightConstructionNode, joinNode);
        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C,D),
                SUBSTITUTION_FACTORY.getSubstitution(C, D));
        expectedQueryBuilder.addChild(joinNode, newLeftConstructionNode);
        expectedQueryBuilder.addChild(newLeftConstructionNode, buildExtensionalDataNode(TABLE1_PREDICATE, D, B));

        expectedQueryBuilder.addChild(joinNode, rightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    /**
     *
     * See BindingLiftTest.testEqualityLiftingNonProjected2() to see how this point can be reached.
     */
    @Test
    public void testEqualityLiftingNonProjected2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        initialQueryBuilder.init(projectionAtom, unionNode);

        initialQueryBuilder.addChild(unionNode, DATA_NODE_1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, unionNode);

        expectedQueryBuilder.addChild(unionNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(unionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, D)));

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, F));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);


        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEqualityLiftingNonProjected3() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,B));
        initialQueryBuilder.addChild(rootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, DATA_NODE_1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, ONE));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, rootNode);

        expectedQueryBuilder.addChild(rootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, DATA_NODE_1);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));
        expectedQueryBuilder.addChild(unionNode, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, D)));

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, F));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);


        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEqualityLiftingNonProjected4() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,B));
        initialQueryBuilder.addChild(rootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, DATA_NODE_1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, C));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, rootNode);

        expectedQueryBuilder.addChild(rootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, DATA_NODE_1);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(B, A));
        expectedQueryBuilder.addChild(unionNode, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, D)));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, F));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);


        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testEqualityLiftingNonProjected5() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,B));
        initialQueryBuilder.addChild(rootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, DATA_NODE_1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, rootNode);

        expectedQueryBuilder.addChild(rootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, DATA_NODE_1);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(B, generateURI1(A)));
        expectedQueryBuilder.addChild(unionNode, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, D)));

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, F));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);


        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(rightConstructionNode, rightConstructionNode.getSubstitution());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    /**
     * Makes sure the substitution propagation is blocked by the first ancestor construction node.
     */
    @Test
    public void testEqualityLiftingNonProjected6() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, B);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, rootNode);

        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B));
        initialQueryBuilder.addChild(rootNode, intermediateConstructionNode);

        ConstructionNode lastConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, C));
        initialQueryBuilder.addChild(intermediateConstructionNode, lastConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, F));
        initialQueryBuilder.addChild(lastConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, rootNode);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(B, A));
        expectedQueryBuilder.addChild(rootNode, newConstructionNode);
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, F));
        expectedQueryBuilder.addChild(newConstructionNode, newRightDataNode);


        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(lastConstructionNode, lastConstructionNode.getSubstitution());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testLiftingDefinitionEqualVariables() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y,X));
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        ImmutableFunctionalTerm uriA = generateURI1(A);

        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, uriA));
        initialQueryBuilder.addChild(initialRootNode, subConstructionNode);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, F));
        initialQueryBuilder.addChild(subConstructionNode, dataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, uriA, Y, uriA));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode, dataNode);

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(subConstructionNode, subConstructionNode.getSubstitution());

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }


    private static NodeCentricOptimizationResults<? extends QueryNode> propagateAndCompare(
            IntermediateQuery query, IntermediateQuery expectedQuery,
            SubstitutionPropagationProposal<? extends QueryNode> propagationProposal)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  query);
        System.out.println(propagationProposal);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        // Updates the query (in-place optimization)
        NodeCentricOptimizationResults<? extends QueryNode> results = query.applyProposal(propagationProposal);

        System.out.println("\n Optimized query: \n" +  query);

        assertTrue(areEquivalent(query, expectedQuery));

        return results;

    }


    private static ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI1_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI2_PREDICATE, URI_TEMPLATE_STR_2, argument1, argument2);
    }
    
    private static ExtensionalDataNode buildExtensionalDataNode(AtomPredicate predicate, VariableOrGroundTerm... arguments) {
        return IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(predicate, arguments));
    }

}
