package it.unibz.inf.ontop.reformulation.tests;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class IQSyntacticEquivalenceCheckerTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 3);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table1", 1);
    private final static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private final static AtomPredicate ANS2_PREDICATE = new AtomPredicateImpl("ans2", 2);
    private final static AtomPredicate ANS1_VAR1_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("x");
    private final static Variable Y = DATA_FACTORY.getVariable("y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Constant TWO = DATA_FACTORY.getConstantLiteral("2");

    private final static ImmutableExpression EXPRESSION = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Y);

    private final static ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
    private final static ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));

    private final MetadataForQueryOptimization metadata;

    private static final Injector INJECTOR = QuestCoreConfiguration.defaultBuilder().build().getInjector();

    public IQSyntacticEquivalenceCheckerTest() {
        metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();

        /**
         * Table 1: non-composite key and regular field
         */
        uniqueKeyBuilder.put(TABLE1_PREDICATE, ImmutableList.of(1));

        return new MetadataForQueryOptimizationImpl(
                RDBMetadataExtractionTools.createDummyMetadata(),
                uniqueKeyBuilder.build(),
                new UriTemplateMatcher());
    }

    @Test
    public void testInnerJoinNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        queryBuilder.addChild(innerJoinNode, dataNode1);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(innerJoinNode1, dataNode2);
        queryBuilder1.addChild(innerJoinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testInnerJoinNodeNotEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        queryBuilder.addChild(innerJoinNode, dataNode1);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode1 = new InnerJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(innerJoinNode1, dataNode2);
        queryBuilder1.addChild(innerJoinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testLeftJoinNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode1, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(leftJoinNode1, dataNode2, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testLeftJoinNodeNotEquivalence() {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode1, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(leftJoinNode1, dataNode2, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testUnionNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        ConstructionNode constructionNodeMain = new ConstructionNodeImpl(projectedVariables);
        UnionNode unionNode = new UnionNodeImpl(projectedVariables);
        queryBuilder.init(projectionAtom, constructionNodeMain);
        queryBuilder.addChild(constructionNodeMain, unionNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder.addChild(leftJoinNode, dataNode1, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, leftJoinNode1);
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder.addChild(leftJoinNode1, dataNode3, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode1, dataNode4, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        UnionNode unionNode1 = new UnionNodeImpl(projectedVariables);
        ConstructionNode constructionNodeMain1 = new ConstructionNodeImpl(projectedVariables);
        DistinctVariableOnlyDataAtom projectionAtomMain1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtomMain1, constructionNodeMain1);
        queryBuilder1.addChild(constructionNodeMain1, unionNode1);

        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder1.addChild(unionNode1, leftJoinNode2);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder1.addChild(leftJoinNode2, dataNode5, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode2, dataNode6, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        LeftJoinNode leftJoinNode3 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder1.addChild(unionNode1, leftJoinNode3);
        ExtensionalDataNode dataNode7 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode8 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder1.addChild(leftJoinNode3, dataNode7, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode3, dataNode8, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testUnionNodeNotEquivalence() {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);;
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        UnionNode unionNode = new UnionNodeImpl(projectedVariables);
        ConstructionNode constructionNodeMain = new ConstructionNodeImpl(projectedVariables);
        queryBuilder.init(projectionAtom, constructionNodeMain);
        queryBuilder.addChild(constructionNodeMain, unionNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder.addChild(leftJoinNode, dataNode1, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, leftJoinNode1);
        ExtensionalDataNode dataNode3 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode4 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder.addChild(leftJoinNode1, dataNode3, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode1, dataNode4, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        UnionNode unionNode1 = new UnionNodeImpl(projectedVariables);
        ConstructionNode constructionNodeMain1 = new ConstructionNodeImpl(ImmutableSet.of(Z));
        queryBuilder1.init(projectionAtom, constructionNodeMain1);
        queryBuilder1.addChild(constructionNodeMain1, unionNode1);

        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder1.addChild(unionNode1, leftJoinNode2);
        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode6 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder1.addChild(leftJoinNode2, dataNode5, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode2, dataNode6, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        InnerJoinNode innerJoinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder1.addChild(unionNode1, innerJoinNode);
        ExtensionalDataNode dataNode7 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode8 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
        queryBuilder1.addChild(innerJoinNode, dataNode7);
        queryBuilder1.addChild(innerJoinNode, dataNode8);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testFilterNodeEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        FilterNode filterNode = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, X, Z));
        queryBuilder.addChild(constructionNode, filterNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(filterNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        FilterNode filterNode1 = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, X, Z));
        queryBuilder1.addChild(constructionNode1, filterNode1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(filterNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testFilterNodeNotEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        FilterNode filterNode = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, X, Z));
        queryBuilder.addChild(constructionNode, filterNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(filterNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        FilterNode filterNode1 = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, X, Y));
        queryBuilder1.addChild(constructionNode1, filterNode1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(filterNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testIntensionalDataNodeEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        IntensionalDataNode dataNode1 = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testIntensionalDataNodeNotEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        IntensionalDataNode dataNode1 = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testExtensionalDataNodeEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, DATA_NODE_1);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, DATA_NODE_1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testExtensionalDataNodeNotEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, DATA_NODE_1);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, DATA_NODE_2);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

//    @Test
//    public void testGroupNodeEquivalence() {
//        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
//        queryBuilder.init(projectionAtom, constructionNode);
//        ImmutableList.Builder<NonGroundTerm> termBuilder = ImmutableList.builder();
//        termBuilder.add(X);
//        GroupNode groupNode = new GroupNodeImpl(termBuilder.build());
//        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
//        queryBuilder.addChild(constructionNode, groupNode);
//        queryBuilder.addChild(groupNode, dataNode);
//
//        IntermediateQuery query = queryBuilder.build();
//
//        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
//        queryBuilder1.init(projectionAtom1, constructionNode1);
//        ImmutableList.Builder<NonGroundTerm> termBuilder1 = ImmutableList.builder();
//        termBuilder1.add(X);
//        GroupNode groupNode1 = new GroupNodeImpl(termBuilder1.build());
//        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
//        queryBuilder1.addChild(constructionNode1, groupNode1);
//        queryBuilder1.addChild(groupNode1, dataNode1);
//
//        IntermediateQuery query1 = queryBuilder1.build();
//
//        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
//    }

//    @Test
//    public void testGroupNodeNodeNotEquivalence() {
//        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
//        queryBuilder.init(projectionAtom, constructionNode);
//        ImmutableList.Builder<NonGroundTerm> termBuilder = ImmutableList.builder();
//        termBuilder.add(X);
//        GroupNode groupNode = new GroupNodeImpl(termBuilder.build());
//        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
//        queryBuilder.addChild(constructionNode, groupNode);
//        queryBuilder.addChild(groupNode, dataNode);
//
//        IntermediateQuery query = queryBuilder.build();
//
//        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
//        queryBuilder1.init(projectionAtom1, constructionNode1);
//        ImmutableList.Builder<NonGroundTerm> termBuilder1 = ImmutableList.builder();
//        termBuilder1.add(Y);
//        GroupNode groupNode1 = new GroupNodeImpl(termBuilder1.build());
//        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
//        queryBuilder1.addChild(constructionNode1, groupNode1);
//        queryBuilder1.addChild(groupNode1, dataNode1);
//
//        IntermediateQuery query1 = queryBuilder1.build();
//
//        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
//    }

    @Test
    public void testConstructionNodeEquivalence() {

        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testConstructionNodeDifferentSubstitutions() {

        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, OBDAVocabulary.NULL)),
                Optional.empty());
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, DATA_FACTORY.getConstantLiteral("John"))),
                Optional.empty());
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testConstructionNodeNotEquivalence() {
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(Y));
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(TABLE3_PREDICATE, Y);
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }
}
