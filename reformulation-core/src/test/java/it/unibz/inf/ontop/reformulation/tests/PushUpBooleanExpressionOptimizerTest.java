package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.PushUpBooleanExpressionOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.PushUpBooleanExpressionOptimizerImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;

public class PushUpBooleanExpressionOptimizerTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final static AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 3);
    private final static AtomPredicate ANS1_PREDICATE1 = new AtomPredicateImpl("ans1", 3);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable W = DATA_FACTORY.getVariable("W");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Z);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Y, Z);

    private final MetadataForQueryOptimization metadata;

    public PushUpBooleanExpressionOptimizerTest() {
        this.metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        return new EmptyMetadataForQueryOptimization();
    }

    @Test
    public void testPropagationFomInnerJoinProvider() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1,EXPRESSION2));
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, joinNode2);
        queryBuilder1.addChild(joinNode2, dataNode2);
        queryBuilder1.addChild(joinNode2, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);

        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, joinNode1);
        queryBuilder2.addChild(joinNode1, dataNode2);
        queryBuilder2.addChild(joinNode1, dataNode3);
        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testNoPropagationFomInnerJoinProvider() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X,Y,Z));
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(ImmutabilityTools.foldBooleanExpressions(EXPRESSION1,EXPRESSION2));
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Z, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, unionNode);
        queryBuilder1.addChild(unionNode, dataNode1);
        queryBuilder1.addChild(unionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl();
        IntermediateQuery optimizedQuery = pushUpBooleanExpressionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQuery query2 = IntermediateQueryUtils.convertToBuilder(query1).build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
    public void testPropagationFomFilterNodeProvider() throws EmptyQueryException {
    }
    public void testNoPropagationFomFilterNodeProvider() throws EmptyQueryException {
    }
    public void testNoPropagationFomLeftJoinProvider() throws EmptyQueryException {
    }
    public void testPropagationToLeftJoinRecipient() throws EmptyQueryException {
    }
    public void testPropagationToExistingFilterRecipient() throws EmptyQueryException {
    }
    public void testPropagationToExistinginnerJoinRecipient() throws EmptyQueryException {
    }
    public void testPropagationWithIntermediateProjector() throws EmptyQueryException {
    }
    public void testPropagationWithIntermediateProjectors() throws EmptyQueryException {
    }
    public void testComplexCase1() throws EmptyQueryException {
    }
    public void testComplexCase2() throws EmptyQueryException {
    }
}
