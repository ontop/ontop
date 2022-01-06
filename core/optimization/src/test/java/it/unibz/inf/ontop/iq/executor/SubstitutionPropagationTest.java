package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests the substitution propagation
 */
public class SubstitutionPropagationTest {

    private static final AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private static final AtomPredicate ANS1_PREDICATE_2 = ATOM_FACTORY.getRDFAnswerPredicate( 2);

    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
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
    private static final Constant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private static final Constant ONE_STR = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBStringType());
    private static final Constant TWO_STR = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBStringType());

    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_2 =  Template.of("http://example.org/ds2/", 0, "/", 1);

    private static final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private static final ExtensionalDataNode DATA_NODE_3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

    @Test
    public void testURI1PropOtherBranch() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3)))));

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(DATA_NODE_1, rightDataNode))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI1PropURI2Branch() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3)))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI2PropURI1Branch() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3)))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI2PropOtherBranch() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3)))));

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(DATA_NODE_1, rightDataNode))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, C));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(dataNode3, dataNode5)))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(newDataNode1, newDataNode2))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(E)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_3),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)))))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(newDataNode1, newDataNode2))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testURI1PropOtherBranchWithJoin()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(E,F)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_3),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)))))))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(E,F)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_3),
                                IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                        createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F))))))));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(DATA_NODE_1, newDataNode1))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C, D));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(
                                                DATA_NODE_3,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)))))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                        createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B)),
                                        createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B))))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1Swapped()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B), Y, generateURI1(B)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C, D));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(
                                                DATA_NODE_3,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D))))),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_1)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D), Y, generateURI1(D)));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C, D));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                        createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D)),
                                        createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)))),
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(C, D))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI2PropOtherBranchWithUnion2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B), Y, generateURI1(B)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(E, F)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_3),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F)))))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                        createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B)),
                                        createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B))))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testURI2PropOtherBranchWithUnion2Swapped()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B), Y, generateURI1(B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(E, F)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_3),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F))))),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_1)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(CF0, DF1), Y, generateURI1(DF1)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(CF0, DF1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                        createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(CF0, DF1)),
                                        createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(CF0, DF1)))),
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(CF0, DF1))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testUnsatisfiedFilter()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        LeftJoinNode leftJoin = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B), Y, generateURI1(B)));
        FilterNode filterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getStrictEquality(X, generateURI1(TERM_FACTORY.getDBStringConstant("two"))));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoin,
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        IQ_FACTORY.createUnaryIQTree(filterNode,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIncompatibleRightOfLJ() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
       ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D), Y, generateURI1(D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3))));

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, NULL));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode, newDataNode1));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testPropagationFromUselessConstructionNode() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode uselessConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createUnaryIQTree(uselessConstructionNode,
                                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3))))));

        ConstructionNode newRootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(AF0), Y, generateURI1(BF1)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF0, BF1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(AF0, BF1)),
                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(AF0, BF1))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testUselessConstructionNodes()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode uselessConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode thirdConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A,B), Y, NULL));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createUnaryIQTree(uselessConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(thirdConstructionNode, DATA_NODE_1))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(thirdConstructionNode, DATA_NODE_1));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1SecondChild() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        UnionNode initialUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, C));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(initialUnionNode, ImmutableList.of(dataNode3, dataNode5)))))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(B)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)),
                                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(newDataNode1, newDataNode2))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEx18() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getConstantIRI(
                        RDF_FACTORY.createIRI("http://example.org/ds2/1/2"))));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, IQ_FACTORY.createTrueNode()))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(ONE_STR, TWO_STR))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEx19() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3)))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEx19NoRootConstructionNode() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A, B)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C, D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEx20() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, Y));
        ConstructionNode leftSubConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(A)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C)));
        ConstructionNode rightSubConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y,C),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(D)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(leftSubConstructionNode, DATA_NODE_1)),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(rightSubConstructionNode, DATA_NODE_3)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(A)));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                newDataNode1,
                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, A))))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEx21() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, A);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(B)));
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B, C));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A,X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, D));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, leftDataNode),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, A),
                rightConstructionNode.getSubstitution());
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(newLeftDataNode, newRightDataNode))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEx22() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(A)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode)))));

        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A, 1, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(newLeftDataNode, newRightDataNode))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Opposite direction to ex22
     */
    @Test
    public void testEx23() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateURI1(A)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode)))));

        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A, 1, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(newLeftDataNode, newRightDataNode))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    /**
     *
     * See BindingLiftTest.testEqualityLiftingNonProjected2() to see how this point can be reached.
     */
    @Test
    public void testEqualityLiftingNonProjected2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ConstructionNode topRightConstructionNode = IQ_FACTORY.createConstructionNode(unionNode.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, C));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createUnaryIQTree(topRightConstructionNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        dataNode3,
                                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode)))))));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        newDataNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A)),
                                newRightDataNode)))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEqualityLiftingNonProjected3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,B));
        ConstructionNode topRightConstructionNode = IQ_FACTORY.createConstructionNode(unionNode.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, ONE));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createUnaryIQTree(topRightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                                DATA_NODE_3,
                                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode))))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                        newDataNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(newDataNode2, newRightDataNode)))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEqualityLiftingNonProjected4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,B));
        ConstructionNode topRightConstructionNode = IQ_FACTORY.createConstructionNode(unionNode.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, C));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createUnaryIQTree(topRightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                                DATA_NODE_3,
                                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode))))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                        newDataNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(newLeftDataNode, newRightDataNode)))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testEqualityLiftingNonProjected5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,B));
        ConstructionNode topRightConstructionNode = IQ_FACTORY.createConstructionNode(unionNode.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, generateURI1(C)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createUnaryIQTree(topRightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                                DATA_NODE_3,
                                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode))))))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                        newDataNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(newLeftDataNode, newRightDataNode)))));

        propagateAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Makes sure the substitution propagation is blocked by the first ancestor construction node.
     */
    @Test
    public void testEqualityLiftingNonProjected6() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, B);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B));
        ConstructionNode lastConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(A, C, B, C));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, F));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(intermediateConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(lastConstructionNode, rightDataNode))));

        ExtensionalDataNode newRightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newRightDataNode);

        propagateAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftingDefinitionEqualVariables() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y,X));
        ImmutableFunctionalTerm uriA = generateURI1(A);
        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, uriA));
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, F));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode, dataNode)));


        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, uriA, Y, uriA));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newDataNode1));

        propagateAndCompare(initialIQ, expectedIQ);
    }


    private static void propagateAndCompare(IQ query, IQ expectedQuery) {
        System.out.println("\n Original query: \n" +  query);
        IQ newQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query);
        System.out.println("\n Optimized query: \n" +  newQuery);

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, newQuery);
    }


    private static ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private static ImmutableFunctionalTerm generateURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(argument1, argument2));
    }

}
