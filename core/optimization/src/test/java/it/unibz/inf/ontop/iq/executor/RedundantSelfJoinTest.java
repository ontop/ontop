package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

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
    private final static Variable D = TERM_FACTORY.getVariable("D");
    private final static Variable E = TERM_FACTORY.getVariable("E");
    private final static Variable F0 = TERM_FACTORY.getVariable("f0");
    private final static Variable P1 = TERM_FACTORY.getVariable("P");
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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, M));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        expectedQueryBuilder.addChild(constructionNode1, dataNode3);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1()  {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.init(projectionAtom1, joinNode1);
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    /**
     * TODO: explain
     */
    @Test
    public void testSelfJoinElimination2() throws IntermediateQueryBuilderException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1();
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testNonEliminationTable1() throws IntermediateQueryBuilderException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1();
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(Z, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testSelfJoinElimination3() throws IntermediateQueryBuilderException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1();
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testNonEliminationTable3() throws IntermediateQueryBuilderException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1();
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Z, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testSelfJoinElimination4() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O)));
        queryBuilder1.addChild(joinNode1, dataNode4);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testSelfJoinElimination5()  {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.init(projectionAtom1, joinNode1);

        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O)));

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testPropagation1() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.init(projectionAtom1, joinNode1);
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testPropagation2()  {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        queryBuilder.addChild(leftJoinNode, dataNode3, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        queryBuilder1.init(projectionAtom1, leftJoinNode);
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(leftJoinNode, dataNode5, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode6, RIGHT);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    /**
     * Ignored for the moment because the looping mechanism is not implemented
     */
    @Test
    public void testLoop1()  {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.init(projectionAtom1, joinNode1);
        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }


    @Ignore("TODO: enable it after refactoring the self-join elimination")
    @Test
    public void testTopJoinUpdateOptimal() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O1, N1));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O, N));
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testTopJoinUpdateSubOptimal()  {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O1, N1));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE2, ImmutableList.of(M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBDefaultInequality(LT, O, N));
        queryBuilder1.init(projectionAtom1, joinNode1);
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testDoubleUniqueConstraints1()  {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N1, TWO));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, TWO));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, TWO));
        expectedQueryBuilder.addChild(newConstructionNode, dataNode3);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testDoubleUniqueConstraints2()  {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE6, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M, TWO));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N, O));
        expectedQueryBuilder.addChild(newConstructionNode, expectedDataNode);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testDoubleUniqueConstraints3()  {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N1, O2));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode expectedDataNode1 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode1);
        ExtensionalDataNode expectedDataNode2 =  createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, N, O2));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode2);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test(expected = EmptyQueryException.class)
    public void testNonUnification1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, ONE, O));
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, TWO, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = optimize(query);
        System.out.println("\n Optimized query (should not be produced): \n" +  optimizedQuery);
    }

    @Test
    public void testNonUnification2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M));
        queryBuilder.addChild(leftJoinNode, leftConstructionNode, LEFT);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N2, O2));
        queryBuilder.addChild(leftConstructionNode, dataNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE6, ImmutableList.of(M, ONE, O));
        ExtensionalDataNode dataNode4 =  createExtensionalDataNode(TABLE6, ImmutableList.of(TWO, TWO, O));

        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, NULL, O, NULL));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, dataNode1);

        optimizeAndCompare(query, expectedQueryBuilder.build());

//        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
//        System.out.println("\n Expected query : \n" +  expectedQuery);
//
//        NodeCentricOptimizationResults<InnerJoinNode> results = optimize(query, expectedQuery);
//
//        System.out.println("\n Optimized query: \n" +  query);
//
//        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
//
//        Optional<QueryNode> optionalClosestAncestor = results.getOptionalClosestAncestor();
//        assertTrue(optionalClosestAncestor.isPresent());
//        assertTrue(optionalClosestAncestor.get().isSyntacticallyEquivalentTo(newRootNode));
//        assertFalse(results.getOptionalNextSibling().isPresent());
    }

    @Test
    public void testJoiningConditionRemoval() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getStrictEquality(O, ONE),
                TERM_FACTORY.getStrictEquality(O, TWO));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, ONE));
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);

        optimizeAndCompare(query, expectedQueryBuilder.build());

//        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
//
//        System.out.println("\n Expected query : \n" +  expectedQuery);
//
//        NodeCentricOptimizationResults<InnerJoinNode> results = optimize(query, expectedQuery);
//
//        System.out.println("\n After optimization: \n" +  query);
//
//        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
//
//        assertFalse(results.getOptionalNewNode().isPresent());
//        assertTrue(results.getOptionalReplacingChild().isPresent());
//        assertTrue(results.getOptionalReplacingChild().get().isSyntacticallyEquivalentTo(dataNode5));
    }

    @Test(expected = EmptyQueryException.class)
    public void testInsatisfiedJoiningCondition() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getStrictEquality(O, TWO),
                TERM_FACTORY.getStrictEquality(O, THREE));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        optimize(query);
    }

    @Test
    public void testNoModification1() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);

        ImmutableExpression joiningCondition = TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getStrictEquality(O, TWO),
                TERM_FACTORY.getStrictEquality(O, THREE));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.init(projectionAtom, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, M, 2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        java.util.UUID initialVersion = query.getVersionNumber();

        IntermediateQuery expectedQuery = query.createSnapshot();

        System.out.println("\nBefore optimization: \n" +  query);

        optimize(query, expectedQuery);
        System.out.println("\nAfter optimization: \n" +  query);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
        assertEquals("The version number has changed", initialVersion, query.getVersionNumber());
    }

    @Test
    public void testNoModification2() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, M, 2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQuery expectedQuery = query.createSnapshot();
        optimize(query, expectedQuery);
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1EqualityInJoiningCondition() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, M));
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, M));
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, M));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(F0),
                                M
                                )));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, M));

        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(M, F0),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));
        expectedQueryBuilder.addChild(newLJNode, rightConstruction, RIGHT);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, M));
        expectedQueryBuilder.addChild(rightConstruction, dataNode4);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ4() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);
        expectedQueryBuilder.addChild(newLJNode, dataNode3, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ5() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(M, N1));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ6()  {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(O, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1,ImmutableList.of( M, N, M));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(F0),
                                O
                        )));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(N, O));

        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(F0, O),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));
        expectedQueryBuilder.addChild(newLJNode, rightConstructionNode, RIGHT);
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(O, O, O));
        expectedQueryBuilder.addChild(rightConstructionNode, dataNode4);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ7() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = createExtensionalDataNode(TABLE4, ImmutableList.of(O, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, N));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, ONE, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(N, ONE),
                TERM_FACTORY.getStrictEquality(O, ONE)));

        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, ONE, ONE));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }



    @Test
    public void testSubstitutionPropagationWithBlockingUnion1() throws EmptyQueryException {
        Constant constant = TERM_FACTORY.getDBStringConstant("constant");
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B, constant));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, C));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4, ImmutableList.of(X, D));
        initialQueryBuilder.addChild(initialRootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, dataNode3);
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, dataNode1);
        initialQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        System.out.println("Initial query: "+ initialQuery);
        /**
         * The following is only one possible syntactic variant of the expected query,
         * namely the one expected based on the current state of the implementation of self join elimination
         * and substitution propagation.
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, constant));
        expectedQueryBuilder.init(projectionAtom, unionNode);
        expectedQueryBuilder.addChild(unionNode, dataNode3);
        expectedQueryBuilder.addChild(unionNode, dataNode4);

        optimizeAndCompare(initialQuery, expectedQueryBuilder.build());

    }

    @Test
    public void testSubstitutionPropagationWithBlockingUnion2() throws EmptyQueryException {
        Constant constant = TERM_FACTORY.getDBStringConstant("constant");
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, constant));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, Y, C));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4, ImmutableList.of(X, Y));
        initialQueryBuilder.addChild(initialRootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, dataNode3);
        initialQueryBuilder.addChild(unionNode, joinNode);
        initialQueryBuilder.addChild(joinNode, dataNode1);
        initialQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        /**
         * The following is only one possible syntactic variant of the expected query,
         * namely the one expected based on the current state of the implementation of self join elimination
         * and substitution propagation.
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, Y)
        );
        expectedQueryBuilder.init(projectionAtom, unionNode);
        expectedQueryBuilder.addChild(unionNode, dataNode3);
        expectedQueryBuilder.addChild(unionNode, constructionNode);

        ExtensionalDataNode newDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(A, Y, constant));

        expectedQueryBuilder.addChild(constructionNode, newDataNode);


        optimizeAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfJoinEliminationFunctionalGroundTerm1() throws EmptyQueryException {
        GroundFunctionalTerm groundFunctionalTerm =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("fct", 1),
                ONE);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, groundFunctionalTerm,
                1, M));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, groundFunctionalTerm,
                2, N));

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, N);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ExtensionalDataNode expectedDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, groundFunctionalTerm,
                1, M,
                2, N));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedDataNode);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinEliminationFunctionalGroundTerm2() throws EmptyQueryException {
        GroundFunctionalTerm groundFunctionalTerm =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("fct", 1),
                ONE);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, M,
                1, groundFunctionalTerm));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, M,
                1, TWO));

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, M);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(groundFunctionalTerm, TWO)),
                dataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinEliminationFunctionalGroundTerm3() throws EmptyQueryException {
        GroundFunctionalTerm groundFunctionalTerm1 =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("fct", 1),
                ONE);

        GroundFunctionalTerm groundFunctionalTerm2 =  (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                        .getRegularDBFunctionSymbol("g", 0));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, M,
                1, groundFunctionalTerm1));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(
                0, M,
                1, groundFunctionalTerm2));

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, M);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(groundFunctionalTerm2, groundFunctionalTerm1)),
                dataNode1);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial query: "+ initialIQ);
        System.out.println("Expected query: "+ expectedIQ);
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        System.out.println("Optimized query: "+ optimizedIQ);
    }

    private static void optimizeAndCompare(IntermediateQuery initialQuery, IntermediateQuery expectedQuery) {
        optimizeAndCompare(IQ_CONVERTER.convert(initialQuery), IQ_CONVERTER.convert(expectedQuery));
    }

    private IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IQ initialIQ =  IQ_CONVERTER.convert(query);

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        if (optimizedIQ.getTree().isDeclaredAsEmpty())
            throw new EmptyQueryException();

        return IQ_CONVERTER.convert(optimizedIQ);
    }

    private static P2<IntermediateQueryBuilder, InnerJoinNode> initAns1()
            throws IntermediateQueryBuilderException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();

        DistinctVariableOnlyDataAtom ans1Atom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ans1Atom.getVariables());
        queryBuilder.init(ans1Atom, rootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        return P.p(queryBuilder, joinNode);
    }

    private void optimize(IntermediateQuery query, IntermediateQuery expectedQuery) {
        optimize(IQ_CONVERTER.convert(query), IQ_CONVERTER.convert(expectedQuery));
    }

    private void optimize(IQ initialQuery, IQ expectedQuery) {
        System.out.println("\nBefore optimization: \n" +  initialQuery);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialQuery);

        System.out.println("\n After optimization: \n" +  optimizedIQ);

        Assert.assertEquals(expectedQuery, optimizedIQ);
    }

}
