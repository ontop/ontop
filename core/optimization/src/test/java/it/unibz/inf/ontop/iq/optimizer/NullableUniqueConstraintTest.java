package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class NullableUniqueConstraintTest {

    private static final NamedRelationDefinition TABLE1;
    private static final NamedRelationDefinition TABLE2;
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        /*
         * Table 1: non-composite unique constraint and regular field
         */
        TABLE1 = builder.createDatabaseRelation("TABLE1",
                "col1", integerDBType, true,
                "col2", integerDBType, true,
                "col3", integerDBType, true);
        UniqueConstraint.builder(TABLE1, "uc1")
                .addDeterminant(1)
                .build();

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        TABLE2 = builder.createDatabaseRelation("TABLE2",
            "col1", integerDBType, true,
            "col2", integerDBType, true,
            "col3", integerDBType, true);
        UniqueConstraint.builder(TABLE2, "uc2")
                .addDeterminant(1)
                .build();
    }

    @Test
    public void testJoinOnLeft1()  {
        ExtensionalDataNode leftNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B, C));
        ExtensionalDataNode leftNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(A, D, E));
        ExtensionalDataNode rightNode = createExtensionalDataNode(TABLE1, ImmutableList.of(A, TWO, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(leftNode1, leftNode2));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(), joinTree,
                        rightNode));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(G, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(F0, TWO), GF1)));

        ExtensionalDataNode newLeftNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, F0, GF1));

        ExtensionalDataNode newLeftNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2, ImmutableMap.of(0, A));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(newLeftNode1, newLeftNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newJoinTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinOnLeft2() {
        ExtensionalDataNode leftNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B, C));
        ExtensionalDataNode leftNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(A, D, E));
        ExtensionalDataNode rightNode = createExtensionalDataNode(TABLE1, ImmutableList.of(A, TWO, G));

        ImmutableFunctionalTerm hDefinition = TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(I), ONE);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, A, G, H);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(H, hDefinition));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(leftNode1, leftNode2));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, G, I),
                        SUBSTITUTION_FACTORY.getSubstitution(I, TERM_FACTORY.getProvenanceSpecialConstant())),
                        rightNode);

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(), joinTree,
                        rightTree));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ImmutableExpression bEquality = TERM_FACTORY.getStrictEquality(F0, TWO);

        ImmutableFunctionalTerm newHDefinition = TERM_FACTORY.getIfElseNull(bEquality, ONE);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        G, TERM_FACTORY.getIfElseNull(bEquality, GF1),
                        H, newHDefinition));

        ExtensionalDataNode newLeftNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, F0, GF1));

        ExtensionalDataNode newLeftNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2, ImmutableMap.of(0, A));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(newLeftNode1, newLeftNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newJoinTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinOnLeft3() {
        ExtensionalDataNode leftNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode leftNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A));
        ExtensionalDataNode rightNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, H, G));
        ExtensionalDataNode rightNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, G));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(rightNode1, rightNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, A, H, I);

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(leftNode1, leftNode2));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, H, I),
                        SUBSTITUTION_FACTORY.getSubstitution(I, TERM_FACTORY.getProvenanceSpecialConstant())),
                rightJoin);

        IQTree initialTree =
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(), joinTree,
                        rightTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ImmutableFunctionalTerm hDefinition = TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(I), HF0);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        H, hDefinition));

        ExtensionalDataNode newLeftNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, HF0, GF1));

        ExtensionalDataNode newLeftNode2 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2, ImmutableMap.of(0, A));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(newLeftNode1, newLeftNode2));

        ExtensionalDataNode newRightNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE2, ImmutableMap.of(1, GF1));

        UnaryIQTree newRightTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(GF1, I),
                        SUBSTITUTION_FACTORY.getSubstitution(I, TERM_FACTORY.getProvenanceSpecialConstant())),
                newRightNode1);

        IQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                newJoinTree, newRightTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNotSimplified1() {
        ExtensionalDataNode leftNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode rightNode = createExtensionalDataNode(TABLE1, ImmutableList.of(A, TWO, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);

        IQTree initialTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(),
                        leftNode1,
                        rightNode);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testFilterAbove1() {
        ExtensionalDataNode leftNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B, C));
        ExtensionalDataNode rightNode = createExtensionalDataNode(TABLE1, ImmutableList.of(A, TWO, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        filterNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                IQ_FACTORY.createLeftJoinNode(),
                                leftNode1,
                                rightNode)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(G, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(F0, TWO), GF1)));

        ExtensionalDataNode newDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(A, F0, GF1));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        newConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filterNode,
                                newDataNode)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterAboveSparse1() {
        ExtensionalDataNode leftNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1,
                ImmutableMap.of(0, A));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1,
                ImmutableMap.of(0, A, 1, TWO, 2, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        filterNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                IQ_FACTORY.createLeftJoinNode(),
                                leftNode1,
                                rightNode)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(G, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(F0, TWO), GF1)));

        ExtensionalDataNode newNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1,
                ImmutableMap.of(0, A, 1, F0, 2, GF1));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        newConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filterNode,
                                newNode)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSimpleJoin1()  {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B, 2, C));;
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, D, 2, E));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, E);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(dataNode1, dataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A,  2, E));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                        IQ_FACTORY.createUnaryIQTree(
                                newFilterNode,
                                newDataNode2));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSimpleJoin2() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B, 2, E));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, E);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(dataNode1, dataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBIsNotNull(A),
                        TERM_FACTORY.getDBIsNotNull(B)));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newFilterNode,
                                dataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    private void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(
                initialIQ.normalizeForOptimization());

        assertEquals(expectedIQ, optimizedIQ);
    }

}
