package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.ImmutableQueryModifiersImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.sql.*;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Types;
import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.DATA_FACTORY;
import static it.unibz.inf.ontop.OptimizationTestingTools.IQ_FACTORY;
import static it.unibz.inf.ontop.OptimizationTestingTools.createQueryBuilder;
import static it.unibz.inf.ontop.model.ExpressionOperation.NEQ;
import static junit.framework.TestCase.assertTrue;

/**
 * Elimination of redundant self-joins using a non unique functional constraint
 */
public class NonUniqueFunctionalConstraintTest {

    private final static AtomPredicate TABLE1_PREDICATE;
    private final static AtomPredicate TABLE2_PREDICATE;

    private final static AtomPredicate ANS1_PREDICATE_AR_1 = DATA_FACTORY.getAtomPredicate("ans1", 1);
    private final static AtomPredicate ANS1_PREDICATE_AR_2 = DATA_FACTORY.getAtomPredicate("ans1", 2);
    private final static AtomPredicate ANS1_PREDICATE_AR_3 = DATA_FACTORY.getAtomPredicate("ans1", 3);
    private final static Variable X = DATA_FACTORY.getVariable("x");
    private final static Variable Y = DATA_FACTORY.getVariable("y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Variable A = DATA_FACTORY.getVariable("a");
    private final static Variable B = DATA_FACTORY.getVariable("b");
    private final static Variable C = DATA_FACTORY.getVariable("c");
    private final static Variable D = DATA_FACTORY.getVariable("d");
    private final static Variable E = DATA_FACTORY.getVariable("e");
    private final static Variable F = DATA_FACTORY.getVariable("f");
    private final static Variable G = DATA_FACTORY.getVariable("g");
    private final static Constant ONE = DATA_FACTORY.getConstantLiteral("1");
    private final static Constant TWO = DATA_FACTORY.getConstantLiteral("2");
    private final static Constant THREE = DATA_FACTORY.getConstantLiteral("3");

    private final static ImmutableQueryModifiers DISTINCT_MODIFIER = new ImmutableQueryModifiersImpl(true, -1, -1, ImmutableList.of()) ;

    private static final DBMetadata METADATA;

    static{
        BasicDBMetadata dbMetadata = DBMetadataTestingTools.createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        /**
         * Table 1: PK + non-unique functional constraint + 2 dependent fields + 1 independent
         */
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table1"));
        Attribute col1T1 = table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute col2T1 = table1Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        Attribute col3T1 = table1Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        Attribute col4T1 = table1Def.addAttribute(idFactory.createAttributeID("col4"), Types.INTEGER, null, false);
        // Independent
        table1Def.addAttribute(idFactory.createAttributeID("col5"), Types.INTEGER, null, false);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T1));
        table1Def.addNonUniqueFunctionalConstraint(NonUniqueFunctionalConstraint.defaultBuilder()
                .addDeterminant(col2T1)
                .addDependent(col3T1)
                .addDependent(col4T1)
                .build());
        TABLE1_PREDICATE = Relation2DatalogPredicate.createAtomPredicateFromRelation(table1Def);

        /**
         * Table 2: non-composite unique constraint and regular field
         */
        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table2"));
        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute col2T2 = table2Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table2Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col2T2));
        TABLE2_PREDICATE = Relation2DatalogPredicate.createAtomPredicateFromRelation(table2Def);
        dbMetadata.freeze();
        METADATA = dbMetadata;
    }

    @Ignore
    @Test
    public void testRedundantSelfJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, Y, C, D));

        expectedQueryBuilder.addChild(rootNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    @Ignore
    @Test
    public void testRedundantSelfJoin2() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3,
                X, Y, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, Z, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, Y, Z, C));

        expectedQueryBuilder.addChild(rootNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    @Ignore
    @Test
    public void testRedundantSelfJoin3() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3,
                X, Y, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, D, A, Z, B, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(Z, Y), Optional.of(DISTINCT_MODIFIER));
        expectedQueryBuilder.init(projectionAtom, newRootNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, Y, F, G));

        expectedQueryBuilder.addChild(newRootNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    @Ignore
    @Test
    public void testRedundantSelfJoin4() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, Y, X, F));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, Y, X, D));

        expectedQueryBuilder.addChild(rootNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    @Ignore
    @Test
    public void testRedundantSelfJoin5() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, D, A, E, Z, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, Z, Y));

        expectedQueryBuilder.addChild(rootNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    /**
     * Y --> from an independent attribute
     */
    @Test
    public void testNonRedundantSelfJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, F, G, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, B, C, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    @Test
    public void testNonRedundantSelfJoin2() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, Y, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, Y, A, Y, E, F));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, Y, A, Y, C, F));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, joinNode);
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, ONE, B, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, D, A, TWO, E, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin2() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(DATA_FACTORY.getImmutableExpression(NEQ, B, TWO));
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, TWO, F, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin3() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(), Optional.of(DISTINCT_MODIFIER));

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(METADATA);
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(DATA_FACTORY.getImmutableExpression(NEQ, F, TWO));
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, A, B, B, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, E, A, TWO, F, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));
    }

    private static NodeCentricOptimizationResults<InnerJoinNode> optimizeAndCompare(IntermediateQuery query,
                                                                                    IntermediateQuery expectedQuery,
                                                                                    InnerJoinNode joinNode)
            throws EmptyQueryException {

        System.out.println("\nBefore optimization: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(
                new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));

        return results;
    }
}
