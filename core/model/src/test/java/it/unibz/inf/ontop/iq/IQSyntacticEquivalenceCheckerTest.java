package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;


import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class IQSyntacticEquivalenceCheckerTest {

    private final static RelationDefinition TABLE3;
    private final static RelationDefinition TABLE2;
    private final static RelationDefinition TABLE1;
    private final static AtomPredicate ANS2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static AtomPredicate ANS1_VAR1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS3_VAR3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");

    private final static ImmutableExpression EQ_X_Y = TERM_FACTORY.getStrictEquality(X, Y);
    private final static ImmutableExpression EQ_X_Z = TERM_FACTORY.getStrictEquality(X, Z);

    private final static ExtensionalDataNode DATA_NODE_1;
    private final static ExtensionalDataNode DATA_NODE_2;

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE1 = builder.createDatabaseRelation("TABLE1",
                "col1", integerDBType, false);

        TABLE2 = builder.createDatabaseRelation("TABLE2",
                "col1", integerDBType, false,
                "col2", integerDBType, false);

        TABLE3 = builder.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);

        DATA_NODE_1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        DATA_NODE_2 = createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
    }


    @Test
    public void testInnerJoinNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        queryBuilder.addChild(innerJoinNode, dataNode1);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EQ_X_Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(innerJoinNode1, dataNode2);
        queryBuilder1.addChild(innerJoinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testInnerJoinNodeNotEquivalence() {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        queryBuilder.addChild(innerJoinNode, dataNode1);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, innerJoinNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(innerJoinNode1, dataNode2);
        queryBuilder1.addChild(innerJoinNode1, dataNode3);
        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testLeftJoinNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode1, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(leftJoinNode1, dataNode2, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testLeftJoinNodeNotEquivalence() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode1, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(EQ_X_Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(leftJoinNode1, dataNode2, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testUnionNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        ConstructionNode constructionNodeMain = IQ_FACTORY.createConstructionNode(projectedVariables);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectedVariables);
        queryBuilder.init(projectionAtom, constructionNodeMain);
        queryBuilder.addChild(constructionNodeMain, unionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(unionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder.addChild(leftJoinNode, dataNode1, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(unionNode, leftJoinNode1);
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode4 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder.addChild(leftJoinNode1, dataNode3, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode1, dataNode4, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(projectedVariables);
        ConstructionNode constructionNodeMain1 = IQ_FACTORY.createConstructionNode(projectedVariables);
        DistinctVariableOnlyDataAtom projectionAtomMain1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder1.init(projectionAtomMain1, constructionNodeMain1);
        queryBuilder1.addChild(constructionNodeMain1, unionNode1);

        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder1.addChild(unionNode1, leftJoinNode2);
        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode6 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder1.addChild(leftJoinNode2, dataNode5, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode2, dataNode6, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        LeftJoinNode leftJoinNode3 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder1.addChild(unionNode1, leftJoinNode3);
        ExtensionalDataNode dataNode7 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode8 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder1.addChild(leftJoinNode3, dataNode7, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode3, dataNode8, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testUnionNodeNotEquivalence() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);;
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectedVariables);
        ConstructionNode constructionNodeMain = IQ_FACTORY.createConstructionNode(projectedVariables);
        queryBuilder.init(projectionAtom, constructionNodeMain);
        queryBuilder.addChild(constructionNodeMain, unionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(unionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder.addChild(leftJoinNode, dataNode1, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(unionNode, leftJoinNode1);
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode4 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder.addChild(leftJoinNode1, dataNode3, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder.addChild(leftJoinNode1, dataNode4, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(projectedVariables);
        ConstructionNode constructionNodeMain1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        queryBuilder1.init(projectionAtom, constructionNodeMain1);
        queryBuilder1.addChild(constructionNodeMain1, unionNode1);

        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder1.addChild(unionNode1, leftJoinNode2);
        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode6 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder1.addChild(leftJoinNode2, dataNode5, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        queryBuilder1.addChild(leftJoinNode2, dataNode6, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder1.addChild(unionNode1, innerJoinNode);
        ExtensionalDataNode dataNode7 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode8 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        queryBuilder1.addChild(innerJoinNode, dataNode7);
        queryBuilder1.addChild(innerJoinNode, dataNode8);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testFilterNodeEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(X, Z));
        queryBuilder.addChild(constructionNode, filterNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(filterNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(X, Z));
        queryBuilder1.addChild(constructionNode1, filterNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(filterNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testFilterNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality( X, Z));
        queryBuilder.addChild(constructionNode, filterNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(filterNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictNEquality( X, Z));
        queryBuilder1.addChild(constructionNode1, filterNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(filterNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testIntensionalDataNodeEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testIntensionalDataNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Y));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testExtensionalDataNodeEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, DATA_NODE_1);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, DATA_NODE_1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testExtensionalDataNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, DATA_NODE_1);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, createExtensionalDataNode(TABLE2, ImmutableList.of(Z, X)));

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

//    @Test
//    public void testGroupNodeEquivalence() {
//        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.create(metadata, EXECUTOR_REGISTRY);
//        queryBuilder.init(projectionAtom, constructionNode);
//        ImmutableList.Builder<NonGroundTerm> termBuilder = ImmutableList.builder();
//        termBuilder.add(X);
//        GroupNode groupNode = new GroupNodeImpl(termBuilder.build());
//        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2_PREDICATE, X, Z));
//        queryBuilder.addChild(constructionNode, groupNode);
//        queryBuilder.addChild(groupNode, dataNode);
//
//        IntermediateQuery query = queryBuilder.build();
//
//        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
//        queryBuilder1.init(projectionAtom1, constructionNode1);
//        ImmutableList.Builder<NonGroundTerm> termBuilder1 = ImmutableList.builder();
//        termBuilder1.add(X);
//        GroupNode groupNode1 = new GroupNodeImpl(termBuilder1.build());
//        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2_PREDICATE, X, Z));
//        queryBuilder1.addChild(constructionNode1, groupNode1);
//        queryBuilder1.addChild(groupNode1, dataNode1);
//
//        IntermediateQuery query1 = queryBuilder1.build();
//
//        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
//    }

//    @Test
//    public void testGroupNodeNodeNotEquivalence() {
//        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.create(metadata, EXECUTOR_REGISTRY);
//        queryBuilder.init(projectionAtom, constructionNode);
//        ImmutableList.Builder<NonGroundTerm> termBuilder = ImmutableList.builder();
//        termBuilder.add(X);
//        GroupNode groupNode = new GroupNodeImpl(termBuilder.build());
//        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2_PREDICATE, X, Y));
//        queryBuilder.addChild(constructionNode, groupNode);
//        queryBuilder.addChild(groupNode, dataNode);
//
//        IntermediateQuery query = queryBuilder.build();
//
//        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
//        queryBuilder1.init(projectionAtom1, constructionNode1);
//        ImmutableList.Builder<NonGroundTerm> termBuilder1 = ImmutableList.builder();
//        termBuilder1.add(Y);
//        GroupNode groupNode1 = new GroupNodeImpl(termBuilder1.build());
//        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2_PREDICATE, X, Y));
//        queryBuilder1.addChild(constructionNode1, groupNode1);
//        queryBuilder1.addChild(groupNode1, dataNode1);
//
//        IntermediateQuery query1 = queryBuilder1.build();
//
//        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
//    }

    @Test
    public void testConstructionNodeEquivalence() {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testConstructionNodeDifferentSubstitutions() {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, TERM_FACTORY.getNullConstant())));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, TERM_FACTORY.getDBStringConstant("John")));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }


    @Test
    public void testConstructionNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        queryBuilder.addChild(constructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(TABLE1.getAtomPredicate(), Y);
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

    @Test
    public void testConstructionNodeDifferentModifiers() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));
        queryBuilder.addChild(topConstructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();
        queryBuilder1.init(projectionAtom1, constructionNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }



    @Test
    public void testConstructionSameModifiers() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);

        SliceNode sliceNode = IQ_FACTORY.createSliceNode(1, 1);
        queryBuilder.init(projectionAtom, sliceNode);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        queryBuilder.addChild(sliceNode, distinctNode);

        OrderByNode orderByNode = IQ_FACTORY.createOrderByNode(ImmutableList.of(
                IQ_FACTORY.createOrderComparator(X, true)));
        queryBuilder.addChild(distinctNode, orderByNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        queryBuilder.addChild(orderByNode, topConstructionNode);

        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));
        queryBuilder.addChild(topConstructionNode, dataNode);

        IntermediateQuery query = queryBuilder.build();

        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);

        IntermediateQueryBuilder queryBuilder1 = query.newBuilder();

        SliceNode newSliceNode = IQ_FACTORY.createSliceNode(1, 1);
        queryBuilder1.init(projectionAtom1, newSliceNode);

        DistinctNode newDistinctNode = IQ_FACTORY.createDistinctNode();
        queryBuilder1.addChild(newSliceNode, newDistinctNode);

        OrderByNode newOrderByNode = IQ_FACTORY.createOrderByNode(ImmutableList.of(
                IQ_FACTORY.createOrderComparator(X, true)));
        queryBuilder1.addChild(newDistinctNode, newOrderByNode);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        queryBuilder1.addChild(newOrderByNode, constructionNode1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));
        queryBuilder1.addChild(constructionNode1, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }

}
