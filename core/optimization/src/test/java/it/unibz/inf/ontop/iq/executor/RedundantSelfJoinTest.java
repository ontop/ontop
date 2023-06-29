package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static junit.framework.TestCase.assertEquals;

/**
 * Optimizations for inner joins based on unique constraints (like PKs).
 *
 * For self-joins
 *
 */
public class RedundantSelfJoinTest {

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;
    private final static NamedRelationDefinition TABLE5;
    private final static NamedRelationDefinition TABLE6;
    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private final static AtomPredicate ANS1_PREDICATE_2 = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable B = TERM_FACTORY.getVariable("B");
    private final static Variable C = TERM_FACTORY.getVariable("C");
    private final static Variable F0 = TERM_FACTORY.getVariable("f0");
    private final static Constant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static Constant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static Constant THREE = TERM_FACTORY.getDBConstant("3", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    private final static Variable M = TERM_FACTORY.getVariable("m");
    private final static Variable M1 = TERM_FACTORY.getVariable("m1");
    private final static Variable N = TERM_FACTORY.getVariable("n");
    private final static Variable N1 = TERM_FACTORY.getVariable("n1");
    private final static Variable N2 = TERM_FACTORY.getVariable("n2");
    private final static Variable O = TERM_FACTORY.getVariable("o");
    private final static Variable O1 = TERM_FACTORY.getVariable("o1");
    private final static Variable O2 = TERM_FACTORY.getVariable("o2");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getStrictEquality(M, N);

    static{
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        /*
         * Table 1: non-composite unique constraint and regular field
         */
        TABLE1 = builder.createDatabaseRelation("table1",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        TABLE2 = builder.createDatabaseRelation("table2",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(2));

        /*
         * Table 3: composite unique constraint over the first TWO columns
         */
        TABLE3 = builder.createDatabaseRelation("table3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1), TABLE3.getAttribute(2));

        /*
         * Table 4: unique constraint over the first column
         */
        TABLE4 = builder.createDatabaseRelation("table4",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));

        /*
         * Table 5: unique constraint over the second column
         */
        TABLE5 = builder.createDatabaseRelation("table5",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE5.getAttribute(2));

        /*
         * Table 6: two atomic unique constraints over the first and third columns
         */
        TABLE6 = builder.createDatabaseRelation("table6",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE6.getAttribute(1));
        UniqueConstraint.builder(TABLE6, "table6-uc3")
                .addDeterminant(3)
                .build();
    }

    @Test
    public void testJoiningConditionTest()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2))));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, M));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode3));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4))));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode5, dataNode6)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * TODO: explain
     */
    @Test
    public void testSelfJoinElimination2()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(1), Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2))));

        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, Y, 2, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNonEliminationTable1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(1), Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(Z, Y, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2))));

        IQ expectedIQ =  IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, Y, 2, Z)),
                                dataNode2))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinElimination3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(1), Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2))));

        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, Y, 2, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNonEliminationTable3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(1), Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Z, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2))));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinElimination4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N2, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4))));

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                dataNode5,
                                createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(2, O)))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinElimination5()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                        dataNode2,
                        createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O)))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testPropagation1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2, dataNode3))));

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode5, dataNode6)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testPropagation2()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)),
                        dataNode3)));

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, dataNode5);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLoop1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4))));

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode5, dataNode6)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testTopJoinUpdateOptimal() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O1, N1));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4))));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O, N));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode5, dataNode6)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testTopJoinUpdateSubOptimal()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O1, N1));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4))));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O, N));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode5, dataNode6)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDoubleUniqueConstraints1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N1, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2))));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, TWO));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, dataNode3));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDoubleUniqueConstraints2()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N2, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2, dataNode3))));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M, TWO));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, expectedDataNode));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDoubleUniqueConstraints3()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N1, O2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2, dataNode3))));

        ExtensionalDataNode expectedDataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O));
        ExtensionalDataNode expectedDataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, TWO, 1, N));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(expectedDataNode1, expectedDataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNonUnification1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, ONE, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, TWO, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2, dataNode3))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNonUnification2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N2, O2));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, ONE, O));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, TWO, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, dataNode1),
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3, dataNode4)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, NULL, O, NULL));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode, IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoiningConditionRemoval() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ImmutableExpression joiningCondition = TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getStrictEquality(O, ONE),
                TERM_FACTORY.getStrictEquality(O, TWO));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2))));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, ONE));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode5));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testUnsatisfiedJoiningCondition() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ImmutableExpression joiningCondition = TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getStrictEquality(O, TWO),
                TERM_FACTORY.getStrictEquality(O, THREE));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNoModification1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ImmutableExpression joiningCondition = TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getStrictEquality(O, TWO),
                TERM_FACTORY.getStrictEquality(O, THREE));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, M, 2, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNoModification2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, M, 2, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1EqualityInJoiningCondition() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, M));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode, leftNode, dataNode4)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, M));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode, leftNode, dataNode4)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, M));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O,
                        TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(F0), M)));
        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, M));
        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(F0, M),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, M));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode,
                                leftNode,
                                IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode4))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ3()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode, leftNode, dataNode4)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode, leftNode, dataNode3)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N1));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode, leftNode, dataNode4)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ6()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(O, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1,ImmutableList.of( M, N, M));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M,
                        TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(F0), O)));
        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, O));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(F0, O),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(O, O, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode,
                                leftNode,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, dataNode4))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ7() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(O, N));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, N));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, ONE, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljNode,
                                leftNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode2, dataNode3)))));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(N, ONE),
                TERM_FACTORY.getStrictEquality(O, ONE)));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, ONE, ONE));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLJNode, leftNode, dataNode4)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }



    @Test
    public void testSubstitutionPropagationWithBlockingUnion1() {
        Constant constant = TERM_FACTORY.getDBStringConstant("constant");
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B, constant));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, X));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(unionNode.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                dataNode3,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)))))));

        /*
         * The following is only one possible syntactic variant of the expected query,
         * namely the one expected based on the current state of the implementation of self join elimination
         * and substitution propagation.
         */
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, X, 2, constant));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(dataNode3, dataNode4)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSubstitutionPropagationWithBlockingUnion2() {
        Constant constant = TERM_FACTORY.getDBStringConstant("constant");
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, constant));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, Y, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, X, 1, Y));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(unionNode.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                dataNode3,
                                IQ_FACTORY.createUnaryIQTree(constructionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)))))));

        /*
         * The following is only one possible syntactic variant of the expected query,
         * namely the one expected based on the current state of the implementation of self join elimination
         * and substitution propagation.
         */
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, Y));
        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, Y, 2, constant));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        dataNode3,
                        IQ_FACTORY.createUnaryIQTree(newConstructionNode, newDataNode))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinEliminationFunctionalGroundTerm1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, N);
        GroundFunctionalTerm groundFunctionalTerm =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("fct", 1), ONE);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, groundFunctionalTerm, 1, M));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, groundFunctionalTerm, 2, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2)));

        ExtensionalDataNode expectedDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, groundFunctionalTerm, 1, M, 2, N));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(groundFunctionalTerm)),
                        expectedDataNode));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinEliminationFunctionalGroundTerm2()  {
        GroundFunctionalTerm groundFunctionalTerm =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("fct", 1), ONE);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 1, groundFunctionalTerm));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 1, TWO));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, M);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(
                        TERM_FACTORY.getStrictEquality(groundFunctionalTerm, TWO)), dataNode2));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinEliminationFunctionalGroundTerm3() {
        GroundFunctionalTerm groundFunctionalTerm1 =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("fct", 1), ONE);

        GroundFunctionalTerm groundFunctionalTerm2 =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("g", 0));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 1, groundFunctionalTerm1));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 1, groundFunctionalTerm2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, M);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2)));

        ImmutableExpression condition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBIsNotNull(groundFunctionalTerm1),
                TERM_FACTORY.getStrictEquality(groundFunctionalTerm2, groundFunctionalTerm1));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(condition), dataNode1));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void test2LevelJoinWithAggregate() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(B),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getDBCount(false)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, B));

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                dataNode3,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, dataNode2)))));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, B));

        NaryIQTree expectedTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                dataNode3,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, dataNode4)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(2), B, C);

        optimizeAndCompare(IQ_FACTORY.createIQ(projectionAtom, initialTree),
                IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("\nBefore optimization: \n" +  initialIQ);
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        System.out.println("\n After optimization: \n" +  optimizedIQ);

        System.out.println("\n Expected query: \n" +  expectedIQ);
        assertEquals(expectedIQ, optimizedIQ);
    }
}
