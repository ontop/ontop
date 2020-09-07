package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertTrue;

public class ValuesNodeNormalizationTest {

    @Test
    public void test1() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createConstructionNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR)), IQ_FACTORY
                        .createTrueNode());

        assertTrue(baseTest(initialTree, expectedTree));
    }

    @Test
    public void test2() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(ONE_STR, THREE_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createConstructionNode(ImmutableSet.of(X, Y), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(Y), ImmutableList.of(ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        assertTrue(baseTest(initialTree, expectedTree));
    }

    @Test
    public void test3() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(ONE_STR, TWO_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of(X, Y), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR, Y, TWO_STR)), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(), ImmutableList.of(ImmutableList.of(), ImmutableList.of()))
        );

        assertTrue(baseTest(initialTree, expectedTree));
    }

    @Test
    public void test4() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(), ImmutableList.of(ImmutableList.of()));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createTrueNode();

        assertTrue(baseTest(initialTree, expectedTree));
    }

    private Boolean baseTest(IQTree initialTree, IQTree expectedTree) {
        IQTree normalizedTree = initialTree.normalizeForOptimization(CORE_UTILS_FACTORY
                .createVariableGenerator(initialTree.getVariables()));
        System.out.println("Tree before normalizing:");
        System.out.println(initialTree.toString());
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree.toString());
        System.out.println('\n' + "Normalized tree:");
        System.out.println(normalizedTree.toString());
        return normalizedTree.isEquivalentTo(expectedTree);
    }
}
