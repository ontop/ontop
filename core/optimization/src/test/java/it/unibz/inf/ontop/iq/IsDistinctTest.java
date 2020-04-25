package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import org.junit.Test;

import static it.unibz.inf.ontop.DependencyTestDBMetadata.PK_TABLE1_AR1;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: enrich
 */
public class IsDistinctTest {

    public static final RelationDefinition NULLABLE_UC_TABLE1_AR1;
    public static final RelationDefinition NULLABLE_UC_TABLE2_AR2;


    static {
        OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        NULLABLE_UC_TABLE1_AR1 = builder.createRelationWithUC( 1, 1, true);
        NULLABLE_UC_TABLE2_AR2 = builder.createRelationWithUC( 2, 2, true);
    }

    @Test
    public void testExtensionalDataNodeNullableUC1() {
        ExtensionalDataNode tree = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE1_AR1, ImmutableMap.of(0, A));
        assertFalse(tree.isDistinct());
    }

    @Test
    public void testExtensionalDataNodeNullableUC2() {
        ExtensionalDataNode tree = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A));
        assertFalse(tree.isDistinct());
    }

    @Test
    public void testExtensionalDataNodeNullableUC3() {
        ExtensionalDataNode tree = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(1, A));
        assertFalse(tree.isDistinct());
    }

    @Test
    public void testExtensionalDataNodeNullableUC4() {
        ExtensionalDataNode tree = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, A));
        assertTrue(tree.isDistinct());
    }

    @Test
    public void testExtensionalDataNodeNullableUC5() {
        ExtensionalDataNode tree = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        assertFalse(tree.isDistinct());
    }

    @Test
    public void testFilterNullableUC1() {
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE1_AR1, ImmutableMap.of(0, A));

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode);

        assertTrue(tree.isDistinct());
    }

    @Test
    public void testFilterNullableUC2() {
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B)),
                dataNode);

        assertFalse(tree.isDistinct());
    }

    @Test
    public void testInnerJoinNullableUC1() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, B));

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBIsNotNull(A)),
                ImmutableList.of(dataNode1, dataNode2));

        assertTrue(tree.isDistinct());
    }

    @Test
    public void testInnerJoinNullableUC2() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, A));

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        assertTrue(tree.isDistinct());
    }

    @Test
    public void testInnerJoinNullableUC3() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, B));

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        assertFalse(tree.isDistinct());
    }

    @Test
    public void testLeftJoinNullableUC1() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, B));

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode1, dataNode2);

        assertFalse(tree.isDistinct());
    }

    @Test
    public void testLeftJoinNullableUC2() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, A));

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, dataNode2);

        assertFalse(tree.isDistinct());
    }

    @Test
    public void testLeftJoinNullableUC3() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, B));

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, dataNode2);

        assertFalse(tree.isDistinct());
    }

    @Test
    public void testLeftJoinNullableUC4() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A, 1, B));

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode1, dataNode2);

        assertTrue(tree.isDistinct());
    }

    @Test
    public void testLeftJoinNullableUC5() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A));

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode1, dataNode2);

        assertTrue(tree.isDistinct());
    }

    @Test
    public void testLeftJoinNullableUC6() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1,
                ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, A));

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, dataNode2);

        assertTrue(tree.isDistinct());
    }
}
