package it.unibz.inf.ontop.unfolding;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.ExpressionOperation;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.predicate.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FixedPointBindingLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.predicate.ExpressionOperation.*;
import static it.unibz.inf.ontop.model.predicate.Predicate.COL_TYPE.INTEGER;
import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNotEquals;

/**
 * Test {@link ExpressionEvaluator}
 */
public class ExpressionEvaluatorTest {

    private final AtomPredicate TABLE1_PREDICATE = DATA_FACTORY.getAtomPredicate("table1", 2);
    private final AtomPredicate TABLE2_PREDICATE = DATA_FACTORY.getAtomPredicate("table2", 2);

    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = DATA_FACTORY.getAtomPredicate("ans1", 3);
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = DATA_FACTORY.getAtomPredicate("ans1", 2);

    private final Variable X = DATA_FACTORY.getVariable("x");
    private final Variable Y = DATA_FACTORY.getVariable("y");
    private final Variable W = DATA_FACTORY.getVariable("w");
    private final Variable A = DATA_FACTORY.getVariable("a");
    private final Variable B = DATA_FACTORY.getVariable("b");
    private final Variable C = DATA_FACTORY.getVariable("c");
    private final Variable D = DATA_FACTORY.getVariable("d");

    private URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private URITemplatePredicate URI_PREDICATE2 =  new URITemplatePredicateImpl(3);
    private Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/stock/{}");

    private ExtensionalDataNode DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, D));
    private ExtensionalDataNode EXP_DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, C, B));
    private ExtensionalDataNode EXP_DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));

    private final ImmutableExpression EXPR_LANG = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.SPARQL_LANG, W );


    private Constant langValueConstant =  DATA_FACTORY.getConstantLiteral("en.us");
    private ImmutableFunctionalTerm langValue =  generateLiteral(langValueConstant);

    private final ImmutableExpression EXPR_EQ = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, B, langValueConstant );

    private final ImmutableExpression EXPR_LANGMATCHES = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.LANGMATCHES, EXPR_LANG, langValue);

    /**
     * test LangMatches matching a  lang function with a constant value
     * (this case should not happen)
     * @throws EmptyQueryException
     */
    @Test
    public void testLangLeftNodeVariable() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                DATA_FACTORY.getSubstitution(X, generateURI1(A), W, generateLangString(B,B)));
        queryBuilder.addChild(joinNode, leftNode);

        queryBuilder.addChild(leftNode, DATA_NODE_1);

        //construct right side join
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                DATA_FACTORY.getSubstitution(X, generateURI1(C), Y, generateInt(D)));

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_2);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        unOptimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        IntermediateQuery optimizedQuery = JOIN_LIKE_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);


        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution( W, generateLangString(B, B), X, generateURI1(A), Y, generateInt(D)));

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode(EXPR_EQ);
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, DATA_NODE_1);

        expectedQueryBuilder.addChild(expectedJoinNode, EXPECTED_DATA_NODE_2);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    private IntermediateQuery getExpectedQuery() {
        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);;


        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateURI1(A), Y, generateInt(D), W, generateLangString(B, langValueConstant)));

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, DATA_NODE_1);

        expectedQueryBuilder.addChild(expectedJoinNode, EXPECTED_DATA_NODE_2);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);
        return expectedQuery;
    }

    private IntermediateQuery getExpectedQuery2() {
        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);;


        DistinctVariableOnlyDataAtom expectedProjectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(expectedProjectionAtom.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateURI1(C), Y, generateInt(D), W, generateLangString(B, langValueConstant)));

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, EXP_DATA_NODE_2);

        expectedQueryBuilder.addChild(expectedJoinNode, EXP_DATA_NODE_1);

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                DATA_FACTORY.getSubstitution(X, generateURI1(A), W, generateLangString(B,langValueConstant)));
        queryBuilder.addChild(joinNode, leftNode);

        queryBuilder.addChild(leftNode, DATA_NODE_1);

        //construct right side join
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                DATA_FACTORY.getSubstitution(
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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPR_LANGMATCHES);
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                DATA_FACTORY.getSubstitution(
                        X, generateURI1(C),
                        Y, generateInt(D)));

        queryBuilder.addChild(joinNode, leftNode);
        queryBuilder.addChild(leftNode, DATA_NODE_2);

        //construct right side join
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,W),
                DATA_FACTORY.getSubstitution(X, generateURI1(A), W, generateLangString(B,langValueConstant)));


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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom
                (ANS1_ARITY_2_PREDICATE, X ,Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                DATA_FACTORY.getSubstitution(X, generateURI2(A,B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                DATA_FACTORY.getSubstitution(Y, generateURI2(C,D)));

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(DATA_FACTORY.getImmutableExpression(NEQ, X, Y));

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
                DATA_FACTORY.getSubstitution(X, generateURI2(A,B), Y, generateURI2(C,D)));

        ImmutableExpression subExpression1 = DATA_FACTORY.getImmutableExpression(NEQ, A, C);
        ImmutableExpression subExpression2 = DATA_FACTORY.getImmutableExpression(NEQ, B, D);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(
                DATA_FACTORY.getImmutableExpression(OR, subExpression1, subExpression2));

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
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
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, generateURI1(X));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, X));
    }

    @Test
    public void testIsNotNullUri2() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, generateURI2(X, Y));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(),
                ImmutabilityTools.foldBooleanExpressions(
                        DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, X),
                        DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, Y)).get());
    }

    @Test
    public void testIsNotNullUri3() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NOT_NULL,
                generateURI2(DATA_FACTORY.getConstantLiteral("toto"), X));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, X));
    }

    @Ignore("TODO: optimize this case")
    @Test
    public void testIsNotNullUri4() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NOT_NULL,
                generateURI1(DATA_FACTORY.getConstantLiteral("toto")));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertFalse(optionalExpression.isPresent());
        assertTrue(result.isEffectiveTrue());
    }

    @Ignore("TODO: support this tricky case")
    @Test
    public void testIsNotNullUriTrickyCase() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NOT_NULL,
                generateURI1(DATA_FACTORY.getImmutableExpression(IS_NULL, X)));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();

        /*
         * Not necessary -> could be further optimized
         */
        if (optionalExpression.isPresent()) {
            ImmutableExpression optimizedExpression = optionalExpression.get();
            assertNotEquals("Invalid optimization for " + initialExpression,
                    optimizedExpression, DATA_FACTORY.getImmutableExpression(IS_NOT_NULL, X));
            fail("The expression " + initialExpression + " should be evaluated as true");
        }
        else {
            assertTrue(result.isEffectiveTrue());
        }

    }

    @Test
    public void testIsNullUri1() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NULL, generateURI1(X));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), DATA_FACTORY.getImmutableExpression(IS_NULL, X));
    }

    @Test
    public void testIsNullUri2() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NULL, generateURI2(X, Y));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(),
                DATA_FACTORY.getImmutableExpression(OR,
                        DATA_FACTORY.getImmutableExpression(IS_NULL, X),
                        DATA_FACTORY.getImmutableExpression(IS_NULL, Y)));
    }

    @Test
    public void testIsNullUri3() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NULL,
                generateURI2(DATA_FACTORY.getConstantLiteral("toto"), X));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertTrue(optionalExpression.isPresent());
        assertEquals(optionalExpression.get(), DATA_FACTORY.getImmutableExpression(IS_NULL, X));
    }

    @Ignore("TODO: optimize this case")
    @Test
    public void testIsNullUri4() {
        ImmutableExpression initialExpression = DATA_FACTORY.getImmutableExpression(IS_NULL,
                generateURI1(DATA_FACTORY.getConstantLiteral("toto")));
        ExpressionEvaluator.EvaluationResult result = new ExpressionEvaluator().evaluateExpression(initialExpression);
        Optional<ImmutableExpression> optionalExpression = result.getOptionalExpression();
        assertFalse(optionalExpression.isPresent());
        assertTrue(result.isEffectiveFalse());
    }

    private ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm var1, Variable var2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE2, URI_TEMPLATE_STR_1, var1, var2);
    }

    private ImmutableFunctionalTerm generateLangString(VariableOrGroundTerm argument1, Constant argument2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.LITERAL_LANG),
                argument1, argument2);
    }
    private ImmutableFunctionalTerm generateLangString(VariableOrGroundTerm argument1, VariableOrGroundTerm argument2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.LITERAL_LANG),
                argument1, argument2);
    }


    private ImmutableFunctionalTerm generateLiteral(Constant argument1) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.LITERAL),
                argument1);
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER),
                argument);
    }
}
