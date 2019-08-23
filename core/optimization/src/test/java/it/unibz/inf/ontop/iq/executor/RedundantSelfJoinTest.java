package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.*;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Optimizations for inner joins based on unique constraints (like PKs).
 *
 * For self-joins
 *
 */
public class RedundantSelfJoinTest {

    private final static RelationPredicate TABLE1_PREDICATE;
    private final static RelationPredicate TABLE2_PREDICATE;
    private final static RelationPredicate TABLE3_PREDICATE;
    private final static RelationPredicate TABLE4_PREDICATE;
    private final static RelationPredicate TABLE5_PREDICATE;
    private final static RelationPredicate TABLE6_PREDICATE;
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
    private final static Constant ONE = TERM_FACTORY.getConstantLiteral("1");
    private final static Constant TWO = TERM_FACTORY.getConstantLiteral("2");
    private final static Constant THREE = TERM_FACTORY.getConstantLiteral("3");

    private final static Variable M = TERM_FACTORY.getVariable("m");
    private final static Variable M1 = TERM_FACTORY.getVariable("m1");
    private final static Variable N = TERM_FACTORY.getVariable("n");
    private final static Variable N1 = TERM_FACTORY.getVariable("n1");
    private final static Variable N2 = TERM_FACTORY.getVariable("n2");
    private final static Variable O = TERM_FACTORY.getVariable("o");
    private final static Variable O1 = TERM_FACTORY.getVariable("o1");
    private final static Variable O2 = TERM_FACTORY.getVariable("o2");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, M, N);

    private static final DBMetadata METADATA;

    private static Constant URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds2/{}");

    static{
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        /*
         * Table 1: non-composite unique constraint and regular field
         */
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table1"));
        Attribute col1T1 = table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T1));
        TABLE1_PREDICATE = table1Def.getAtomPredicate();

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table2"));
        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute col2T2 = table2Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table2Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col2T2));
        TABLE2_PREDICATE = table2Def.getAtomPredicate();

        /*
         * Table 3: composite unique constraint over the first TWO columns
         */
        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table3"));
        Attribute col1T3 = table3Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute col2T3 = table3Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T3, col2T3));
        TABLE3_PREDICATE = table3Def.getAtomPredicate();

        /*
         * Table 4: unique constraint over the first column
         */
        DatabaseRelationDefinition table4Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table4"));
        Attribute col1T4 = table4Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table4Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table4Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T4));
        TABLE4_PREDICATE = table4Def.getAtomPredicate();

        /*
         * Table 5: unique constraint over the second column
         */
        DatabaseRelationDefinition table5Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table5"));
        table5Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute col2T5 = table5Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table5Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col2T5));
        TABLE5_PREDICATE = table5Def.getAtomPredicate();

        /*
         * Table 6: two atomic unique constraints over the first and third columns
         */
        DatabaseRelationDefinition table6Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table6"));
        Attribute col1T6 = table6Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table6Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        Attribute col3T6 = table6Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table6Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T6));
        table6Def.addUniqueConstraint(new UniqueConstraint.Builder(table6Def)
                .add(col3T6)
                .build("table6-uc3", false));
        TABLE6_PREDICATE = table6Def.getAtomPredicate();

        dbMetadata.freeze();
        METADATA = dbMetadata;
    }

    @Test
    public void testJoiningConditionTest() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M, N));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, N, N, O));
        expectedQueryBuilder.addChild(constructionNode1, dataNode3);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    /**
     * TODO: explain
     */
    @Test
    public void testSelfJoinElimination2() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testNonEliminationTable1() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, Z, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testSelfJoinElimination3() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder1.addChild(constructionNode, extensionalDataNode);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testNonEliminationTable3() throws IntermediateQueryBuilderException,
            InvalidQueryOptimizationProposalException, EmptyQueryException {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(METADATA);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Z, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, Y, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode);
        queryBuilder1.addChild(joinNode, dataNode1);
        queryBuilder1.addChild(joinNode, dataNode2);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testSelfJoinElimination4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O)));
        queryBuilder1.addChild(joinNode1, dataNode4);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testSelfJoinElimination5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(constructionNode1, joinNode1);

        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O)));

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testPropagation1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testPropagation2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        queryBuilder.addChild(leftJoinNode, dataNode3, RIGHT);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        queryBuilder1.addChild(constructionNode1, leftJoinNode);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(leftJoinNode, dataNode5, LEFT);
        queryBuilder1.addChild(leftJoinNode, dataNode6, RIGHT);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    /**
     * Ignored for the moment because the looping mechanism is not implemented
     */
    @Test
    public void testLoop1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }


    @Ignore("TODO: enable it after refactoring the self-join elimination")
    @Test
    public void testTopJoinUpdateOptimal() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(LT, O1, N1));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(LT, O, N));
        queryBuilder1.addChild(constructionNode1, joinNode1);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testTopJoinUpdateSubOptimal() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(LT, O1, N1));
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M1, N, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getImmutableExpression(LT, O, N));
        queryBuilder1.addChild(constructionNode1, filterNode);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(filterNode, joinNode1);
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode6 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N, O));

        queryBuilder1.addChild(joinNode1, dataNode5);
        queryBuilder1.addChild(joinNode1, dataNode6);

        optimizeAndCompare(query, queryBuilder1.build());
    }

    @Test
    public void testDoubleUniqueConstraints1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N1, TWO));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, TWO));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, TWO));
        expectedQueryBuilder.addChild(newConstructionNode, dataNode3);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testDoubleUniqueConstraints2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N2, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M, TWO));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        ExtensionalDataNode expectedDataNode =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N, O));
        expectedQueryBuilder.addChild(newConstructionNode, expectedDataNode);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testDoubleUniqueConstraints3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N1, O2));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode expectedDataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode1);
        ExtensionalDataNode expectedDataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, N, O2));
        expectedQueryBuilder.addChild(joinNode, expectedDataNode2);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test(expected = EmptyQueryException.class)
    public void testNonUnification1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);

        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, ONE, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, TWO, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = JOIN_LIKE_OPTIMIZER.optimize(query);
        System.out.println("\n Optimized query (should not be produced): \n" +  optimizedQuery);
    }

    @Test
    public void testNonUnification2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M));
        queryBuilder.addChild(leftJoinNode, leftConstructionNode, LEFT);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N2, O2));
        queryBuilder.addChild(leftConstructionNode, dataNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, ONE, O));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, TWO, TWO, O));

        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, NULL, O, NULL));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, dataNode1);

        optimizeAndCompare(query, expectedQueryBuilder.build());

//        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
//        System.out.println("\n Expected query : \n" +  expectedQuery);
//
//        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
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
    public void testJoiningConditionRemoval() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = TERM_FACTORY.getImmutableExpression(OR,
                TERM_FACTORY.getImmutableExpression(EQ, O, ONE),
                TERM_FACTORY.getImmutableExpression(EQ, O, TWO));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, ONE));
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);

        optimizeAndCompare(query, expectedQueryBuilder.build());

//        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
//
//        System.out.println("\n Expected query : \n" +  expectedQuery);
//
//        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
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

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = TERM_FACTORY.getImmutableExpression(OR,
                TERM_FACTORY.getImmutableExpression(EQ, O, TWO),
                TERM_FACTORY.getImmutableExpression(EQ, O, THREE));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        JOIN_LIKE_OPTIMIZER.optimize(query);
    }

    @Test
    public void testNoModification1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        ImmutableExpression joiningCondition = TERM_FACTORY.getImmutableExpression(OR,
                TERM_FACTORY.getImmutableExpression(EQ, O, TWO),
                TERM_FACTORY.getImmutableExpression(EQ, O, THREE));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        java.util.UUID initialVersion = query.getVersionNumber();

        IntermediateQuery expectedQuery = query.createSnapshot();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
        System.out.println("\nAfter optimization: \n" +  query);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
        assertEquals("The version number has changed", initialVersion, query.getVersionNumber());
    }

    @Test
    public void testNoModification2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, M, N1, O));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        UUID initialVersion = query.getVersionNumber();

        IntermediateQuery expectedQuery = query.createSnapshot();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\nAfter optimization: \n" +  query);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
        assertEquals("The version number has changed", initialVersion, query.getVersionNumber());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1EqualityInJoiningCondition() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();



        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ, M, N));
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ, M, N));
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, M));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O,
                        TERM_FACTORY.getImmutableExpression(IF_ELSE_NULL,
                                TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, F0),
                                M
                                )));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ, M, N));

        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(M, F0),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));
        expectedQueryBuilder.addChild(newLJNode, rightConstruction, RIGHT);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, M));
        expectedQueryBuilder.addChild(rightConstruction, dataNode4);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ4() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);
        expectedQueryBuilder.addChild(newLJNode, dataNode3, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ5() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, M, N1));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(constructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ6() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, O, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, M));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, M, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(M,
                        TERM_FACTORY.getImmutableExpression(IF_ELSE_NULL,
                                TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, F0),
                                O
                        )));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ, N, O));

        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(F0, O),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));
        expectedQueryBuilder.addChild(newLJNode, rightConstructionNode, RIGHT);
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, O, O, O));
        expectedQueryBuilder.addChild(rightConstructionNode, dataNode4);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }

    @Test
    public void testOptimizationOnRightPartOfLJ7() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, M, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, ljNode);

        ExtensionalDataNode leftNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, O, N));

        queryBuilder.addChild(ljNode, leftNode, LEFT);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, N));
        queryBuilder.addChild(joinNode, dataNode2);


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, ONE, O));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);

        LeftJoinNode newLJNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(AND,
                TERM_FACTORY.getImmutableExpression(EQ, N, ONE),
                TERM_FACTORY.getImmutableExpression(EQ, O, ONE)));

        expectedQueryBuilder.addChild(newConstructionNode, newLJNode);
        expectedQueryBuilder.addChild(newLJNode, leftNode, LEFT);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, M, ONE, ONE));
        expectedQueryBuilder.addChild(newLJNode, dataNode4, RIGHT);

        optimizeAndCompare(query, expectedQueryBuilder.build());
    }



    @Test
    public void testSubstitutionPropagationWithBlockingUnion1() throws EmptyQueryException {
        Constant constant = TERM_FACTORY.getConstantLiteral("constant");
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE1_PREDICATE, A, B, constant));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, X, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE4_PREDICATE, X, D));
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
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE1_PREDICATE, A, X, constant));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, dataNode3);
        expectedQueryBuilder.addChild(unionNode, dataNode4);

        optimizeAndCompare(initialQuery, expectedQueryBuilder.build());

    }

    @Test
    public void testSubstitutionPropagationWithBlockingUnion2() throws EmptyQueryException {
        Constant constant = TERM_FACTORY.getConstantLiteral("constant");
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, X, Y);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE1_PREDICATE, A, X, constant));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE1_PREDICATE, A, Y, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom (TABLE4_PREDICATE, X, Y));
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
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, X)
        );
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, dataNode3);
        expectedQueryBuilder.addChild(unionNode, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, dataNode1);


        optimizeAndCompare(initialQuery, expectedQueryBuilder.build());
    }

    private static void optimizeAndCompare(IntermediateQuery initialQuery, IntermediateQuery expectedQuery) throws EmptyQueryException {
        System.out.println("Initial query: "+ initialQuery);
        System.out.println("Expected query: "+ expectedQuery);
        IntermediateQuery optimizedQuery = JOIN_LIKE_OPTIMIZER.optimize(initialQuery);
        System.out.println("Optimized query: "+ optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    private static P2<IntermediateQueryBuilder, InnerJoinNode> initAns1(DBMetadata metadata)
            throws IntermediateQueryBuilderException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(metadata);

        DistinctVariableOnlyDataAtom ans1Atom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ans1Atom.getVariables());
        queryBuilder.init(ans1Atom, rootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        return P.p(queryBuilder, joinNode);
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_STR_2, argument);
    }

}
