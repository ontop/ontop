package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.template.Template;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class NoQueryContextTest {

    @Test
    public void testUserName() {
        var iqTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getImmutableFunctionalTerm(TERM_FACTORY.getDBFunctionSymbolFactory().getOntopUser()),
                        TERM_FACTORY.getDBStringConstant("roger"))),
                IQ_FACTORY.createTrueNode());

        optimizeAndCompare(iqTree, iqTree);
    }

    @Test
    public void testRole() {
        var iqTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(
                        TERM_FACTORY.getImmutableExpression(
                                TERM_FACTORY.getDBFunctionSymbolFactory().getOntopContainsRole(),
                                TERM_FACTORY.getDBStringConstant("admin"))),
                IQ_FACTORY.createTrueNode());

        optimizeAndCompare(iqTree, iqTree);
    }

    @Test
    public void testGroup() {
        var iqTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(
                        TERM_FACTORY.getImmutableExpression(
                                TERM_FACTORY.getDBFunctionSymbolFactory().getOntopContainsGroup(),
                                TERM_FACTORY.getDBStringConstant("admin"))),
                IQ_FACTORY.createTrueNode());

        optimizeAndCompare(iqTree, iqTree);
    }

    @Test
    public void testGroupOrRole() {
        var iqTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(
                        TERM_FACTORY.getImmutableExpression(
                                TERM_FACTORY.getDBFunctionSymbolFactory().getOntopContainsRoleOrGroup(),
                                TERM_FACTORY.getDBStringConstant("admin"))),
                IQ_FACTORY.createTrueNode());

        optimizeAndCompare(iqTree, iqTree);
    }

    @Test
    public void testBnodeTemplate() {
        var bnodeFunctionalTerm = TERM_FACTORY.getBnodeFunctionalTerm(
                Template.builder()
                        .string("something")
                        .build(),
                ImmutableList.of());

        var iqTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, bnodeFunctionalTerm)),
                IQ_FACTORY.createTrueNode());

        optimizeAndCompare(iqTree, iqTree);
    }

    private void optimizeAndCompare(IQTree initialTree, IQTree expectedTree) {
        var projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(initialTree.getVariables().size()),
                ImmutableList.copyOf(initialTree.getVariables()));

        optimizeAndCompare(
                IQ_FACTORY.createIQ(projectionAtom, initialTree),
                IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    private void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        var newIQ = GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER.optimize(initialIQ, null);
        assertEquals(expectedIQ, newIQ);
    }

}
