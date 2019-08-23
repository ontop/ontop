package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.*;
import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNotEquals;

/**
 * Test {@link ExpressionEvaluator}
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

    private Constant URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/stock/{}");

    private ExtensionalDataNode DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B));
    private ExtensionalDataNode DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, A, D));

    private final ImmutableExpression EXPR_LANG = TERM_FACTORY.getImmutableExpression(
            ExpressionOperation.SPARQL_LANG, W );


    private final String languageTag =  "en-us";
    // TODO: avoid this language tag wrapping approach
    private final ImmutableFunctionalTerm wrappedLanguageTag = TERM_FACTORY.getImmutableTypedTerm(
            TERM_FACTORY.getConstantLiteral(languageTag, XSD.STRING), XSD.STRING);

    private final ImmutableExpression EXPR_LANGMATCHES = TERM_FACTORY.getImmutableExpression(
            ExpressionOperation.LANGMATCHES, EXPR_LANG, wrappedLanguageTag);


    private IntermediateQuery getExpectedQuery() {
        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);


        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateInt(D), W, generateLangString(B, languageTag)));

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B)));

        expectedQueryBuilder.addChild(expectedJoinNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, A, D)));

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);
        return expectedQuery;
    }

    private IntermediateQuery getExpectedQuery2() {
        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);;


        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateInt(D), W, generateLangString(B, languageTag)));

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);


        expectedQueryBuilder.addChild(expectedJoinNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, A, D)));

        expectedQueryBuilder.addChild(expectedJoinNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B)));

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);
        return expectedQuery;
    }

    /**
     * test LangMatches matching a lang function with a  typed literal value
     * @throws EmptyQueryException
     */
    @Test
    public void testLangLeftNodeFunction() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), W, generateLangString(B, languageTag)));
        queryBuilder.addChild(joinNode, leftNode);

        queryBuilder.addChild(leftNode, DATA_NODE_1);

        //construct right side join
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateInt(D)));

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_2);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        unOptimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        IntermediateQuery optimizedQuery = JOIN_LIKE_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQuery expectedQuery = getExpectedQuery();
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    /**
     * test LangMatches matching a lang function with a typed literal value
     * @throws EmptyQueryException
     */

    @Test
    public void testLangRightNode() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateInt(D)));

        queryBuilder.addChild(joinNode, leftNode);
        queryBuilder.addChild(leftNode, DATA_NODE_2);

        //construct right side join
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), W, generateLangString(B, languageTag)));


        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_1);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQuery expectedQuery = getExpectedQuery2();
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    /**
     * Reproduces a bug: NEQ(f(x11, x12), f(x21, x22) was evaluated as AND((x11 != x21),  (x12 != x22)),
     * instead of OR((x11 != x21), (x12 != x22))
     */
    @Test
    public void testNonEqualOperatorDistribution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom
                (ANS1_ARITY_2_PREDICATE, X ,Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A,B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI2(C,D)));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(NEQ, X, Y));

        queryBuilder.init(projectionAtom, rootNode);
        queryBuilder.addChild(rootNode, joinNode1);
        queryBuilder.addChild(joinNode1, constructionNode1);
        queryBuilder.addChild(joinNode1, constructionNode2);
        queryBuilder.addChild(constructionNode1, DATA_NODE_1);
        queryBuilder.addChild(constructionNode2, DATA_NODE_2);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        //----------------------------------------------------------------------
        // Construct expected query

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A,B), Y, generateURI2(C,D)));

        ImmutableExpression subExpression1 = TERM_FACTORY.getImmutableExpression(NEQ, A, C);
        ImmutableExpression subExpression2 = TERM_FACTORY.getImmutableExpression(NEQ, B, D);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getImmutableExpression(OR, subExpression1, subExpression2));

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);
        expectedQueryBuilder.addChild(expectedRootNode, joinNode2);
        expectedQueryBuilder.addChild(joinNode2, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode2, DATA_NODE_2);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testIsNotNullUri1() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, generateURI1(X));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, X));
    }

    @Test
    public void testIsNotNullUri2() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, generateURI2(X, Y));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(),
                IMMUTABILITY_TOOLS.foldBooleanExpressions(
                        TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, X),
                        TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, Y)).get());
    }

    @Test
    public void testIsNotNullUri3() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL,
                generateURI2(TERM_FACTORY.getConstantLiteral("toto"), X));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, X));
    }

    @Ignore("TODO: optimize this case")
    @Test
    public void testIsNotNullUri4() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL,
                generateURI1(TERM_FACTORY.getConstantLiteral("toto")));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertFalse(optionalExpression.isPresent());
        assertTrue(result.isEffectiveTrue());
    }

    @Ignore("TODO: support this tricky case")
    @Test
    public void testIsNotNullUriTrickyCase() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL,
                generateURI1(TERM_FACTORY.getImmutableExpression(IS_NULL, X)));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();

        /*
         * Not necessary -> could be further optimized
         */
        if (optionalExpression.isPresent()) {
            ImmutableExpression optimizedExpression = optionalExpression.get();
            assertNotEquals("Invalid optimization for " + initialExpression,
                    optimizedExpression, TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, X));
            fail("The expression " + initialExpression + " should be evaluated as true");
        }
        else {
            assertTrue(result.isEffectiveTrue());
        }

    }

    @Test
    public void testIsNullUri1() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NULL, generateURI1(X));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getImmutableExpression(IS_NULL, X));
    }

    @Test
    public void testIsNullUri2() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NULL, generateURI2(X, Y));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(),
                TERM_FACTORY.getImmutableExpression(OR,
                        TERM_FACTORY.getImmutableExpression(IS_NULL, X),
                        TERM_FACTORY.getImmutableExpression(IS_NULL, Y)));
    }

    @Test
    public void testIsNullUri3() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NULL,
                generateURI2(TERM_FACTORY.getConstantLiteral("toto"), X));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), TERM_FACTORY.getImmutableExpression(IS_NULL, X));
    }

    @Ignore("TODO: optimize this case")
    @Test
    public void testIsNullUri4() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(IS_NULL,
                generateURI1(TERM_FACTORY.getConstantLiteral("toto")));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertFalse(optionalExpression.isPresent());
        assertTrue(result.isEffectiveFalse());
    }

    @Test
    public void testIfElseNull1() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(
                IS_NOT_NULL,
                TERM_FACTORY.getImmutableExpression(
                    IF_ELSE_NULL,
                    TERM_FACTORY.getImmutableExpression(EQ, TRUE, TRUE), Y));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, Y), optionalExpression.get());
    }

    @Test
    public void testIfElseNull2() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(
                IS_NOT_NULL,
                TERM_FACTORY.getImmutableExpression(
                        IF_ELSE_NULL,
                        TERM_FACTORY.getImmutableExpression(EQ, X, TRUE), Y));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(initialExpression, optionalExpression.get());
    }

    @Test
    public void testIfElseNull3() {
        ImmutableExpression initialExpression = TERM_FACTORY.getImmutableExpression(
                IS_NOT_NULL,
                TERM_FACTORY.getImmutableExpression(
                        IF_ELSE_NULL,
                        TERM_FACTORY.getImmutableExpression(EQ, TRUE, FALSE), Y));
        ExpressionEvaluator.EvaluationResult result = DEFAULT_EXPRESSION_EVALUATOR.clone()
                .evaluateExpression(initialExpression);
        assertFalse(result.getOptionalExpression().isPresent());
        assertTrue(result.isEffectiveFalse());
    }

    private ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm var1, Variable var2) {
        return TERM_FACTORY.getImmutableUriTemplate(URI_TEMPLATE_STR_1, var1, var2);
    }

    private ImmutableFunctionalTerm generateLangString(VariableOrGroundTerm argument1, String languageTag) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(TYPE_FACTORY.getLangTermType(languageTag)),
                argument1);
    }


    private ImmutableFunctionalTerm generateLiteral(Constant argument1) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(XSD.STRING),
                argument1);
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(XSD.INTEGER),
                argument);
    }
}
