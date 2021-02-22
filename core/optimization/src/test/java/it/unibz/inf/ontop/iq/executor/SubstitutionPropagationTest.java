package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static org.junit.Assert.assertTrue;

/**
 * Tests the substitution propagation
 */
public class SubstitutionPropagationTest {


    private static final AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private static final AtomPredicate ANS1_PREDICATE_2 = ATOM_FACTORY.getRDFAnswerPredicate( 2);


    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable W = TERM_FACTORY.getVariable("w");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable AF0 = TERM_FACTORY.getVariable("af0");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable BF1 = TERM_FACTORY.getVariable("bf1");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable CF0 = TERM_FACTORY.getVariable("cf0");
    private static final Variable D = TERM_FACTORY.getVariable("d");
    private static final Variable DF1 = TERM_FACTORY.getVariable("df1");
    private static final Variable E = TERM_FACTORY.getVariable("e");
    private static final Variable F = TERM_FACTORY.getVariable("f");
    private static final Variable G = TERM_FACTORY.getVariable("g");
    private static final Variable H = TERM_FACTORY.getVariable("h");
    private static final Variable I = TERM_FACTORY.getVariable("i");
    private static final Variable L = TERM_FACTORY.getVariable("l");
    private static final Variable M = TERM_FACTORY.getVariable("m");
    private static final Variable N = TERM_FACTORY.getVariable("n");
    private static final Constant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private static final Constant ONE_STR = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBStringType());
    private static final Constant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private static final Constant TWO_STR = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBStringType());

    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_2 =  Template.of("http://example.org/ds2/", 0, "/", 1);

    private static final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private static final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, B));
    private static final ExtensionalDataNode DATA_NODE_3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));
    private static final ExtensionalDataNode DATA_NODE_4 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private static final ExtensionalDataNode DATA_NODE_5 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, E));
    private static final ExtensionalDataNode DATA_NODE_6 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F));
    private static final ExtensionalDataNode DATA_NODE_7 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H));

    @Test
    public void testURI1PropOtherBranch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, rightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test(expected = EmptyQueryException.class)
    public void testURI1PropURI2Branch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        System.out.println("\n Original query: \n" +  initialQuery);

        BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

    }

    @Test(expected = EmptyQueryException.class)
    public void testURI2PropURI1Branch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        System.out.println("\n Original query: \n" +  initialQuery);

        // Updates the query (in-place optimization)
        BINDING_LIFT_OPTIMIZER.optimize(initialQuery);
    }

    @Test
    public void testURI2PropOtherBranch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        expectedQueryBuilder.addChild(joinNode, rightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode1);
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode2);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testURI1PropOtherBranchWithUnion2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(constructionNode3, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode1);
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode2);
        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }


    @Test(expected = EmptyQueryException.class)
    public void testURI1PropOtherBranchWithJoin() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(constructionNode3, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        System.out.println("\n Original query: \n" +  initialQuery);

        // Updates the query (in-place optimization)
        BINDING_LIFT_OPTIMIZER.optimize(initialQuery);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion3() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(constructionNode3, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newDataNode1);
        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(initialUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1Swapped() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(initialUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D),
                        Y, generateURI1(D)));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C, D));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(C, D)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testURI2PropOtherBranchWithUnion2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(constructionNode3, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B)));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }


    @Test
    public void testURI2PropOtherBranchWithUnion2Swapped() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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
        initialQueryBuilder.addChild(constructionNode3, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(CF0, DF1),
                        Y, generateURI1(DF1)));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(CF0, DF1));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(CF0, DF1)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(CF0, DF1)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(CF0, DF1)));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testUnsatisfiedFilter() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        LeftJoinNode leftJoin = IQ_FACTORY.createLeftJoinNode();
        initialQueryBuilder.init(projectionAtom, leftJoin);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A, B),
                        Y, generateURI1(B)));
        initialQueryBuilder.addChild(leftJoin, leftConstructionNode, LEFT);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(
                X, generateURI1(TERM_FACTORY.getDBStringConstant("two"))));
        initialQueryBuilder.addChild(leftJoin, filterNode, RIGHT);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(C, D)));
        initialQueryBuilder.addChild(filterNode, rightConstructionNode);

        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testIncompatibleRightOfLJ() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, NULL));
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(expectedRootNode, newDataNode1);

        propagateAndCompare(initialQueryBuilder.build(), expectedQueryBuilder.build());
    }

    @Test
    public void testPropagationFromUselessConstructionNode() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        ConstructionNode newRootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(AF0),
                        Y, generateURI1(BF1)));

        expectedQueryBuilder.init(projectionAtom, newRootConstructionNode);
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF0, BF1));
        expectedQueryBuilder.addChild(newRootConstructionNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(AF0, BF1)));
        expectedQueryBuilder.addChild(newUnionNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(AF0, BF1)));

        propagateAndCompare(initialQueryBuilder.build(), expectedQueryBuilder.build());
    }

    @Test
    public void testUselessConstructionNodes() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        System.out.println("\n Original query: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, thirdConstructionNode);
        expectedQueryBuilder.addChild(thirdConstructionNode, DATA_NODE_1);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1SecondChild() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(B)));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode1);
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode2);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEx18() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.init(projectionAtom, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getConstantIRI(
                        RDF_FACTORY.createIRI("http://example.org/ds2/1/2"))));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, IQ_FACTORY.createTrueNode());

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, rightConstructionNode);
        expectedQueryBuilder.addChild(rightConstructionNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(ONE_STR, TWO_STR)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEx19() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode,
                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEx19NoRootConstructionNode() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode,
                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEx20() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI1(A)));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newDataNode1);

        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, A)));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEx21() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, A);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(B)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B, C));

        initialQueryBuilder.addChild(leftConstructionNode, leftDataNode);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A,X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, D));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, A),
                rightConstructionNode.getSubstitution());
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newLeftDataNode);

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEx22() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);

        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newLeftDataNode);
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A, 1, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    /**
     * Opposite direction to ex22
     */
    @Test
    public void testEx23() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, joinNode);
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newLeftDataNode);
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A, 1, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);


        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    /**
     *
     * See BindingLiftTest.testEqualityLiftingNonProjected2() to see how this point can be reached.
     */
    @Test
    public void testEqualityLiftingNonProjected2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, unionNode);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(unionNode, newDataNode1);
        expectedQueryBuilder.addChild(unionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A)));

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEqualityLiftingNonProjected3() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.init(projectionAtom, newUnionNode);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode1);

        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newDataNode2);

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEqualityLiftingNonProjected4() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.init(projectionAtom, newUnionNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode1);

        expectedQueryBuilder.addChild(newUnionNode, joinNode);

        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newLeftDataNode);

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testEqualityLiftingNonProjected5() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        expectedQueryBuilder.init(projectionAtom, newUnionNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newUnionNode, newDataNode1);

        expectedQueryBuilder.addChild(newUnionNode, joinNode);

        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newLeftDataNode);

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    /**
     * Makes sure the substitution propagation is blocked by the first ancestor construction node.
     */
    @Test
    public void testEqualityLiftingNonProjected6() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, B);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, rootNode);

        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B));
        initialQueryBuilder.addChild(rootNode, intermediateConstructionNode);

        ConstructionNode lastConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, C));
        initialQueryBuilder.addChild(intermediateConstructionNode, lastConstructionNode);

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));
        initialQueryBuilder.addChild(lastConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, B));
        expectedQueryBuilder.init(projectionAtom, newRightDataNode);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testLiftingDefinitionEqualVariables() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
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

        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, F));
        initialQueryBuilder.addChild(subConstructionNode, dataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, uriA, Y, uriA));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, A));
        expectedQueryBuilder.addChild(newConstructionNode, newDataNode1);

        propagateAndCompare(initialQuery, expectedQueryBuilder.build());
    }


    private static void propagateAndCompare(
            IntermediateQuery query, IntermediateQuery expectedQuery)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IntermediateQuery newQuery = BINDING_LIFT_OPTIMIZER.optimize(query);

        System.out.println("\n Optimized query: \n" +  newQuery);

        assertTrue(areEquivalent(newQuery, expectedQuery));
    }


    private static ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private static ImmutableFunctionalTerm generateURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(argument1, argument2));
    }

}
