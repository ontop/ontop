package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class ConjunctionOfDisjunctionsMergingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConjunctionOfDisjunctionsMergingTest.class);

    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate(2);

    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable B = TERM_FACTORY.getVariable("B");

    private ImmutableExpression or(ImmutableExpression... exprs) {
        return TERM_FACTORY.getDisjunction(Arrays.stream(exprs).collect(ImmutableCollectors.toList()));
    }

    private ImmutableExpression and(ImmutableExpression... exprs) {
        return TERM_FACTORY.getConjunction(Arrays.stream(exprs).collect(ImmutableCollectors.toList()));
    }

    private ImmutableExpression is(ImmutableTerm left, ImmutableTerm right) {
        return TERM_FACTORY.getStrictEquality(left, right);
    }

    private ImmutableExpression is(ImmutableTerm left, String right) {
        return TERM_FACTORY.getStrictEquality(left, constant(right));
    }

    private ImmutableExpression is(String left, ImmutableTerm right) {
        return TERM_FACTORY.getStrictEquality(constant(left), right);
    }

    private ImmutableExpression is(String left, String right) {
        return TERM_FACTORY.getStrictEquality(constant(left), constant(right));
    }

    private ImmutableExpression notNull(Variable variable) {
        return TERM_FACTORY.getDBIsNotNull(variable);
    }

    private ImmutableExpression in(ImmutableTerm left, String... items) {
        return TERM_FACTORY.getImmutableExpression(
                TERM_FACTORY.getDBFunctionSymbolFactory().getDBIn(items.length + 1),
                Streams.concat(
                        Stream.of(left),
                        Arrays.stream(items)
                                .map(this::constant)
                ).collect(ImmutableCollectors.toList())
        );
    }

    private ImmutableExpression in(ImmutableTerm left, ImmutableTerm... items) {
        return TERM_FACTORY.getImmutableExpression(
                TERM_FACTORY.getDBFunctionSymbolFactory().getDBIn(items.length + 1),
                Streams.concat(
                        Stream.of(left),
                        Arrays.stream(items)
                ).collect(ImmutableCollectors.toList())
        );
    }

    private ImmutableTerm constant(String value) {
        return TERM_FACTORY.getDBStringConstant(value);
    }

    @Test
    public void mergingTest1() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, A, B);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, A, 1, B));

        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(
                and(
                        or(is(A, "X"), is(A, "Y"), is(A, "Z")),
                        or(is(B, "V"), is(B, "W")),
                        or(is(A, "W"), is(A, "X"))
                )
        );
        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(filterNode1,
                        dataNode1));

        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(
                in(B, "V", "W")
        );
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, (DBConstant)constant("X"), 1, B));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B), SUBSTITUTION_FACTORY.getSubstitution(A, constant("X")));
        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                    IQ_FACTORY.createUnaryIQTree(filterNode2,
                        dataNode2)));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void mergingTest2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, A, B);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, A, 1, B));

        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(
                and(
                        or(is("%", A), is("[%]", A), is("perc", A)),
                        or(is("W/m²", A), is("W/m2", A), is("W/mq", A))
                )
        );
        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(filterNode1,
                        dataNode1));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1, IQ_FACTORY.createEmptyNode(projectionAtom1.getVariables()));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void mergingTest3() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, A, B);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, A, 1, B));

        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(
                or(
                        and(is(A, "X"), is(B, "V")),
                        and(is(A, "Y"), is(B, "W")),
                        and(is(A, "Z"), is(A, "X"))
                )
        );
        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(filterNode1,
                        dataNode1));

        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(
                and(
                        or(is(B, "V"), is(A, "Y")),
                        in(A, "X", "Y"),
                        or(is(A, "X"), is(B, "W")),
                        in(B, "V", "W")
                )
        );
        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                        IQ_FACTORY.createUnaryIQTree(filterNode2,
                                dataNode1));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void mergingTest4() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, A, B);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, A, 1, B));

        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(
                and(
                        or(is(A, "X"), is(A, "Y"), is(B, "Z"), is(B, "W")),
                        or(is(B, "V"), is(B, "W")),
                        or(is(A, "W"), is(A, "X"))
                )
        );
        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(filterNode1,
                        dataNode1));

        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(
                or(
                        and(in(B, "V", "W"), is(A, "X")),
                        and(is(B, "W"), in(A, "W", "X"))
                )
        );
        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(filterNode2,
                        dataNode1));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void mergingTest5() {
        var A1 = TERM_FACTORY.getVariable("A1");
        var B1 = TERM_FACTORY.getVariable("B1");
        var C1 = TERM_FACTORY.getVariable("C1");
        var D1 = TERM_FACTORY.getVariable("D1");
        var E1 = TERM_FACTORY.getVariable("E1");
        var A2 = TERM_FACTORY.getVariable("A2");
        var B2 = TERM_FACTORY.getVariable("B2");
        var C2 = TERM_FACTORY.getVariable("C2");
        var D2 = TERM_FACTORY.getVariable("D2");
        var E2 = TERM_FACTORY.getVariable("E2");
        var A3 = TERM_FACTORY.getVariable("A3");
        var B3 = TERM_FACTORY.getVariable("B3");
        var C3 = TERM_FACTORY.getVariable("C3");
        var D3 = TERM_FACTORY.getVariable("D3");
        var E3 = TERM_FACTORY.getVariable("E3");
        var values = ImmutableList.of("C75.5" ,
                "C75.4" ,
                "C75.3",
                "C75.2",
                "C75.9");

        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, A, B);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR5, ImmutableMap.of(0, A1, 1, B1, 2, C1, 3, D1, 4, E1));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR5, ImmutableMap.of(0, A2, 1, B2, 2, C2, 3, D2, 4, E2));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR5, ImmutableMap.of(0, A3, 1, B3, 2, C3, 3, D3, 4, E3));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, X, 1, Y, 2, Z));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(
            and(
                    is(E1, E2),
                    is(E1, E3)
            )
        );

        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(
                and(
                        notNull(C1),
                        TERM_FACTORY.getImmutableExpression(
                                TERM_FACTORY.getDBFunctionSymbolFactory().getDBOr(values.size()),
                                values.stream()
                                        .map(v -> and(
                                                is("SOME_CONSTANT", A2),
                                                is(v, B2)
                                        ))
                                        .collect(ImmutableCollectors.toList())),
                        notNull(X),
                        is(D3, Y),
                        is("SOME_CONSTANT", A1),
                        is("SOME_CONSTANT", A3),
                        is("SOME_CONSTANT", Z)
                )
        );

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        A,
                        TERM_FACTORY.getDBLower(A1),
                        B,
                        TERM_FACTORY.getDBLower(B1))
        );

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                    IQ_FACTORY.createUnaryIQTree(filterNode1,
                            IQ_FACTORY.createNaryIQTree(joinNode1,
                                    ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4)
                            )
                    )
                )
        );

        ExtensionalDataNode dataNode2_1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR5, ImmutableMap.of(0, TERM_FACTORY.getDBStringConstant("SOME_CONSTANT"), 1, B1, 2, C1, 4, E3));
        ExtensionalDataNode dataNode2_2 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR5, ImmutableMap.of(0, TERM_FACTORY.getDBStringConstant("SOME_CONSTANT"), 1, B2, 4, E3));
        ExtensionalDataNode dataNode2_3 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR5, ImmutableMap.of(0, TERM_FACTORY.getDBStringConstant("SOME_CONSTANT"), 3, Y, 4, E3));
        ExtensionalDataNode dataNode2_4 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(1, Y, 2, TERM_FACTORY.getDBStringConstant("SOME_CONSTANT")));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(
                and(
                        in(B2, "C75.5","C75.4","C75.3","C75.2","C75.9"),
                        notNull(C1)
                )
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        A,
                        TERM_FACTORY.getDBLower(TERM_FACTORY.getDBStringConstant("SOME_CONSTANT")),
                        B,
                        TERM_FACTORY.getDBLower(B1))
        );

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                    IQ_FACTORY.createNaryIQTree(joinNode,
                            ImmutableList.of(dataNode2_1, dataNode2_2, dataNode2_3, dataNode2_4))));

        optimizeAndCompare(query1, query2);
    }

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) {
        IQ optimizedQuery = GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER.optimize(initialQuery);
        LOGGER.debug("Initial query: {}", initialQuery);
        Assert.assertEquals(expectedQuery, optimizedQuery);
        LOGGER.debug("Optimized query: {}", optimizedQuery);
    }
}
