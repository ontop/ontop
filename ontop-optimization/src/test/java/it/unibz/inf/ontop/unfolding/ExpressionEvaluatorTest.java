package it.unibz.inf.ontop.unfolding;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FixedPointBindingLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FixedPointJoinLikeOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.JoinLikeOptimizer;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.ConstructionNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static junit.framework.TestCase.assertTrue;

/**
 * Test {@link it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator}
 */
public class ExpressionEvaluatorTest {

    private final AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);

    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = new AtomPredicateImpl("ans1", 3);

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final Variable X = DATA_FACTORY.getVariable("x");
    private final Variable Y = DATA_FACTORY.getVariable("y");
    private final Variable W = DATA_FACTORY.getVariable("w");
    private final Variable A = DATA_FACTORY.getVariable("a");
    private final Variable B = DATA_FACTORY.getVariable("b");
    private final Variable C = DATA_FACTORY.getVariable("c");
    private final Variable D = DATA_FACTORY.getVariable("d");

    private URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/stock/{}");

    private ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, A, D));
    private ExtensionalDataNode EXP_DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, C, B));
    private ExtensionalDataNode EXP_DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));

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
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(EXPR_LANGMATCHES));
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = new ConstructionNodeImpl(ImmutableSet.of(X,W),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), W, generateLangString(B,B))), Optional.empty());
        queryBuilder.addChild(joinNode, leftNode);

        queryBuilder.addChild(leftNode, DATA_NODE_1);

        //construct right side join
        ConstructionNode rightNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))),
                Optional.empty());

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_2);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
        unOptimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        JoinLikeOptimizer joinLikeOptimizer = new FixedPointJoinLikeOptimizer();
        IntermediateQuery optimizedQuery = joinLikeOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);;


        DistinctVariableOnlyDataAtom expectedProjectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(expectedProjectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( W, generateLangString(B, B), X, generateURI1(A), Y, generateInt(D))),
                Optional.empty());

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.of(EXPR_EQ));
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


        DistinctVariableOnlyDataAtom expectedProjectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(expectedProjectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateInt(D), W, generateLangString(B, langValueConstant))),
                Optional.empty());

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.empty());
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


        DistinctVariableOnlyDataAtom expectedProjectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, W);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(expectedProjectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C), Y, generateInt(D), W, generateLangString(B, langValueConstant))),
                Optional.empty());

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin

        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.empty());
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
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(EXPR_LANGMATCHES));
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = new ConstructionNodeImpl(ImmutableSet.of(X,W),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), W, generateLangString(B,langValueConstant))), Optional.empty());
        queryBuilder.addChild(joinNode, leftNode);

        queryBuilder.addChild(leftNode, DATA_NODE_1);

        //construct right side join
        ConstructionNode rightNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))),
                Optional.empty());

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_2);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
        unOptimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        JoinLikeOptimizer joinLikeOptimizer = new FixedPointJoinLikeOptimizer();
        IntermediateQuery optimizedQuery = joinLikeOptimizer.optimize(unOptimizedQuery);

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
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y,W);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(EXPR_LANGMATCHES));
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))),
                Optional.empty());

        queryBuilder.addChild(joinNode, leftNode);
        queryBuilder.addChild(leftNode, DATA_NODE_2);

        //construct right side join
        ConstructionNode rightNode = new ConstructionNodeImpl(ImmutableSet.of(X,W),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), W, generateLangString(B,langValueConstant))), Optional.empty());


        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_1);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQuery expectedQuery = getExpectedQuery2();
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
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
