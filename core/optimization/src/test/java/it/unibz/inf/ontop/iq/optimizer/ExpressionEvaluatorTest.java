package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNotEquals;

/**
 * Test expression evaluation
 */
public class ExpressionEvaluatorTest {

    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);

    private final Variable X = TERM_FACTORY.getVariable("x");
    private final Variable Y = TERM_FACTORY.getVariable("y");
    private final Variable W = TERM_FACTORY.getVariable("w");
    private final Variable A = TERM_FACTORY.getVariable("a");
    private final Variable B = TERM_FACTORY.getVariable("b");
    private final Variable C = TERM_FACTORY.getVariable("c");
    private final Variable D = TERM_FACTORY.getVariable("d");

    private ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/stock/", 0);
    private ImmutableList<Template.Component> URI_TEMPLATE_STR_2 = Template.of("http://example.org/something/", 0, "/", 1);


    private ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, D));


    private final String languageTag =  "en-us";
    // TODO: avoid this language tag wrapping approach
    private final ImmutableFunctionalTerm wrappedLanguageTag = TERM_FACTORY.getRDFLiteralFunctionalTerm(
            TERM_FACTORY.getDBStringConstant(languageTag), XSD.STRING);

    private final ImmutableExpression EXPR_LANGMATCHES = TERM_FACTORY.getRDF2DBBooleanFunctionalTerm(
            TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getRequiredSPARQLFunctionSymbol(SPARQL.LANG_MATCHES, 2),
                TERM_FACTORY.getImmutableFunctionalTerm(
                        FUNCTION_SYMBOL_FACTORY.getRequiredSPARQLFunctionSymbol(SPARQL.LANG, 1),
                        W),
                wrappedLanguageTag));


    private IQ getExpectedQuery() {
        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateInt(D), W, generateLangString(B, languageTag)));

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(expectedProjectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)),
                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, D))))));
        System.out.println("\n Expected query: \n" +  expectedQuery);
        return expectedQuery;
    }

    /**
     * test LangMatches matching a lang function with a  typed literal value
     */
    @Test
    public void testLangLeftNodeFunction() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), W, generateLangString(B, languageTag)));
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateInt(D)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightNode, DATA_NODE_2)))));
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        unOptimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        IQ optimizedQuery = JOIN_LIKE_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertEquals(getExpectedQuery(), optimizedQuery);
    }

    /**
     * test LangMatches matching a lang function with a typed literal value
     */

    @Test
    public void testLangRightNode() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateInt(D)));
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        W, generateLangString(B, languageTag)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftNode, DATA_NODE_2),
                                IQ_FACTORY.createUnaryIQTree(rightNode, DATA_NODE_1)))));
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        // Construct expected query

        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateInt(D), W, generateLangString(B, languageTag)));

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        IQ expectedQuery = IQ_FACTORY.createIQ(expectedProjectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)),
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(C, B))))));
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, optimizedQuery);
    }

    /**
     * Reproduces a bug: NEQ(f(x11, x12), f(x21, x22) was evaluated as AND((x11 != x21),  (x12 != x22)),
     * instead of OR((x11 != x21), (x12 != x22))
     */
    @Test
    public void testNonEqualOperatorDistribution() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom
                (ANS1_ARITY_2_PREDICATE, X ,Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A,B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI2(C,D)));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictNEquality(X, Y));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(constructionNode1, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_2)))));
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        //----------------------------------------------------------------------
        // Construct expected query

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A,B), Y, generateURI2(C,D)));

        ImmutableExpression subExpression1 = TERM_FACTORY.getStrictNEquality(A, C);
        ImmutableExpression subExpression2 = TERM_FACTORY.getStrictNEquality(B, D);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getDisjunction(subExpression1, subExpression2));

        //build expected query
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(DATA_NODE_1, DATA_NODE_2))));
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, optimizedQuery);
    }


    @Test
    public void testIsNotNullUri1() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(generateURI1(X));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getDBIsNotNull(X));
    }

    @Test
    public void testIsNotNullUri2() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(generateURI2(X, Y));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(),
                TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBIsNotNull(X),
                        TERM_FACTORY.getDBIsNotNull(Y)));
    }

    @Test
    public void testIsNotNullUri3() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(
                generateURI2(TERM_FACTORY.getDBStringConstant("toto"), X));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getDBIsNotNull(X));
    }

    @Test
    public void testIsNotNullUri4() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(
                generateURI1(TERM_FACTORY.getDBStringConstant("toto")));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertFalse(optionalExpression.isPresent());
        assertSame(ImmutableExpression.Evaluation.BooleanValue.TRUE, result.getValue().get());
    }

    @Test
    public void testIsNotNullUriTrickyCase() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(
                generateURI1(TERM_FACTORY.getDBIsNull(X)));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();

        /*
         * Not necessary -> could be further optimized
         */
        if (optionalExpression.isPresent()) {
            ImmutableExpression optimizedExpression = optionalExpression.get();
            assertNotEquals("Invalid optimization for " + initialExpression,
                    optimizedExpression, TERM_FACTORY.getDBIsNotNull(X));
            fail("The expression " + initialExpression + " should be evaluated as true");
        }
        else {
            assertSame(ImmutableExpression.Evaluation.BooleanValue.TRUE, result.getValue().get());
        }

    }

    @Test
    public void testIsNullUri1() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNull(generateURI1(X));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getDBIsNull(X));
    }

    @Test
    public void testIsNullUri2() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNull(generateURI2(X, Y));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertEquals(optionalExpression.get(),
                TERM_FACTORY.getDisjunction(
                        TERM_FACTORY.getDBIsNull(X),
                        TERM_FACTORY.getDBIsNull(Y)));
    }

    @Test
    public void testIsNullUri3() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNull(
                generateURI2(TERM_FACTORY.getDBStringConstant("toto"), X));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getDBIsNull(X));
    }

    @Test
    public void testIsNullUri4() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNull(
                generateURI1(TERM_FACTORY.getDBStringConstant("toto")));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertFalse(optionalExpression.isPresent());
        assertTrue(result.isEffectiveFalse());
    }

    @Test
    public void testIfElseNull1() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(
                TERM_FACTORY.getIfElseNull(
                    TERM_FACTORY.getStrictEquality(TRUE, TRUE), Y));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(TERM_FACTORY.getDBIsNotNull(Y), optionalExpression.get());
    }

    @Test
    public void testIfElseNull2() {
        ImmutableExpression equality = TERM_FACTORY.getStrictEquality(X, TRUE);
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(
                TERM_FACTORY.getIfElseNull(equality, Y));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        Optional<ImmutableExpression> optionalExpression = result.getExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(TERM_FACTORY.getConjunction(
                    equality,
                    TERM_FACTORY.getDBIsNotNull(X),
                    TERM_FACTORY.getDBIsNotNull(Y)),
                optionalExpression.get());
    }

    @Test
    public void testIfElseNull3() {
        ImmutableExpression initialExpression = TERM_FACTORY.getDBIsNotNull(
                TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(TRUE, FALSE), Y));
        ImmutableExpression.Evaluation result = initialExpression.evaluate(
                CORE_UTILS_FACTORY.createSimplifiedVariableNullability(initialExpression));
        assertFalse(result.getExpression().isPresent());
        assertTrue(result.isEffectiveFalse());
    }

    private ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm var1, Variable var2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(var1, var2));
    }

    private ImmutableFunctionalTerm generateLangString(VariableOrGroundTerm argument1, String languageTag) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument1, TYPE_FACTORY.getLangTermType(languageTag));
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.INTEGER);
    }
}
