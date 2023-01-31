package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.junit.Test;

import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.Assert.assertEquals;

public class ConvertValuesToUnionTest {

    @Test
    public void testLeftJoinValuesNode() {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable y = TERM_FACTORY.getVariable("y");
        DBConstant c1 = TERM_FACTORY.getDBStringConstant("c1");
        DBConstant c2 = TERM_FACTORY.getDBStringConstant("c2");

        ValuesNode leftValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(x),
                ImmutableList.of(ImmutableList.of(c1), ImmutableList.of(c1)));

        ValuesNode rightValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(y),
                ImmutableList.of(ImmutableList.of(c2), ImmutableList.of(c2)));

        IQTree initialTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftValuesNode, rightValuesNode);

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(x), SUBSTITUTION_FACTORY.getSubstitution(x, c1)),
                IQ_FACTORY.createTrueNode());

        IQTree newChild3 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(y), SUBSTITUTION_FACTORY.getSubstitution(y, c2)),
                IQ_FACTORY.createTrueNode());

        IQTree expectedTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(x)), ImmutableList.of(newChild1, newChild1)),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(y)), ImmutableList.of(newChild3, newChild3)));

        transformAndCheck(initialTree, expectedTree);
    }

    private static void transformAndCheck(IQTree initialTree, IQTree expectedTree) {
        VariableGenerator variableGenerator = CORE_SINGLETONS.getCoreUtilsFactory().createVariableGenerator(initialTree.getKnownVariables());
        IQTree newTree = CONVERT_VALUES_TO_UNION_NORMALIZER.transform(initialTree, variableGenerator);

        assertEquals(expectedTree, newTree);
    }


}
