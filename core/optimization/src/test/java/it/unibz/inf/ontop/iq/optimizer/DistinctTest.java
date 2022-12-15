package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.DependencyTestDBMetadata.PK_TABLE1_AR2;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class DistinctTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistinctTest.class);

    @Test
    public void testDistinctConstructionConstant1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR2, ImmutableMap.of());

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(A, ONE));

        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        dataNode));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createSliceNode(0, 1),
                        dataNode));

        normalizeAndCompare(initialTree, expectedTree, projectionAtom);
    }

    @Test
    public void testDistinctConstructionConstant2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR2, ImmutableMap.of(1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(A, ONE));

        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        dataNode));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        dataNode));

        normalizeAndCompare(initialTree, expectedTree, projectionAtom);
    }

    private static void normalizeAndCompare(IQTree initialTree, IQTree expectedTree, DistinctVariableOnlyDataAtom projectionAtom) {
        normalizeAndCompare(
                IQ_FACTORY.createIQ(projectionAtom, initialTree),
                IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    private static void normalizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        LOGGER.info("Initial IQ: " + initialIQ );
        LOGGER.info("Expected IQ: " + expectedIQ);

        IQ normalizedIQ = initialIQ.normalizeForOptimization();
        LOGGER.info("Normalized IQ: " + normalizedIQ);

        assertEquals(expectedIQ, normalizedIQ);
    }
}
