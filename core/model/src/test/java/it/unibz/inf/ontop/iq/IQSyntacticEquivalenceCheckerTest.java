package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;


import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

    private final static ImmutableExpression EQ_X_Z = TERM_FACTORY.getStrictEquality(X, Z);

    private final static ExtensionalDataNode DATA_NODE_1;

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
    }


    @Test
    public void testInnerJoinNodeEquivalence() {
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(dataNode, dataNode1))));

        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EQ_X_Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(dataNode2, dataNode3))));

        assertEquals(query1, query);
    }

    @Test
    public void testInnerJoinNodeNotEquivalence() {
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(dataNode, dataNode1))));

        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(dataNode2, dataNode3))));

        assertNotEquals(query1, query);
    }

    @Test
    public void testLeftJoinNodeEquivalence() {
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode, dataNode1)));

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode2, dataNode3)));

        assertEquals(query1, query);
    }

    @Test
    public void testLeftJoinNodeNotEquivalence() {
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode, dataNode1)));

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(EQ_X_Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode2, dataNode3)));

        assertNotEquals(query1, query);
    }

    @Test
    public void testUnionNodeEquivalence() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        ConstructionNode constructionNodeMain = IQ_FACTORY.createConstructionNode(projectedVariables);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectedVariables);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode4 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNodeMain,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode3, dataNode4)))));

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(projectedVariables);
        ConstructionNode constructionNodeMain1 = IQ_FACTORY.createConstructionNode(projectedVariables);
        DistinctVariableOnlyDataAtom projectionAtomMain1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode6 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        LeftJoinNode leftJoinNode3 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode7 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode8 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtomMain1,
                IQ_FACTORY.createUnaryIQTree(constructionNodeMain1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2, dataNode5, dataNode6),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode3, dataNode7, dataNode8)))));

        assertEquals(query1, query);
    }

    @Test
    public void testUnionNodeNotEquivalence() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        ImmutableSet<Variable> projectedVariables = projectionAtom.getVariables();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectedVariables);
        ConstructionNode constructionNodeMain = IQ_FACTORY.createConstructionNode(projectedVariables);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode4 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNodeMain,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode3, dataNode4)))));

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(projectedVariables);
        ConstructionNode constructionNodeMain1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y, Z));
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode6 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode7 =  createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode8 =  createExtensionalDataNode(TABLE2, ImmutableList.of(Y, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNodeMain1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2, dataNode5, dataNode6),
                                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(dataNode7, dataNode8))))));

        assertNotEquals(query1, query);
    }

    @Test
    public void testFilterNodeEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(X, Z));
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, dataNode)));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode1)));

        assertEquals(query1, query);
    }

    @Test
    public void testFilterNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality( X, Z));
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, dataNode)));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictNEquality( X, Z));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode1)));

        assertNotEquals(query1, query);
    }

    @Test
    public void testIntensionalDataNodeEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        assertEquals(query1, query);
    }

    @Test
    public void testIntensionalDataNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2.getAtomPredicate(), X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        assertNotEquals(query1, query);
    }

    @Test
    public void testExtensionalDataNodeEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, DATA_NODE_1));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, DATA_NODE_1));

        assertEquals(query1, query);
    }

    @Test
    public void testExtensionalDataNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, DATA_NODE_1));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, createExtensionalDataNode(TABLE2, ImmutableList.of(Z, X))));

        assertNotEquals(query1, query);
    }

//    @Test
//    public void testGroupNodeEquivalence() {
//        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
//        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
//        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.create(metadata, );
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
//        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.create(metadata, );
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
    public void testConstructionNodeEquivalence(){
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        assertEquals(query1, query);
    }

    @Test
    public void testConstructionNodeDifferentSubstitutions() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, TERM_FACTORY.getNullConstant()));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, TERM_FACTORY.getDBStringConstant("John")));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        assertNotEquals(query1, query);
    }


    @Test
    public void testConstructionNodeNotEquivalence() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, X);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y));
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(TABLE1.getAtomPredicate(), Y);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        assertNotEquals(query1, query);
    }

    @Test
    public void testConstructionNodeDifferentModifiers()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(topConstructionNode, dataNode)));

        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        assertNotEquals(query1, query);
    }



    @Test
    public void testConstructionSameModifiers() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        SliceNode sliceNode = IQ_FACTORY.createSliceNode(1, 1);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        OrderByNode orderByNode = IQ_FACTORY.createOrderByNode(ImmutableList.of(
                IQ_FACTORY.createOrderComparator(X, true)));
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(sliceNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(orderByNode,
                                        IQ_FACTORY.createUnaryIQTree(topConstructionNode, dataNode)))));

        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_VAR3_PREDICATE, X, Y, Z);
        SliceNode newSliceNode = IQ_FACTORY.createSliceNode(1, 1);
        DistinctNode newDistinctNode = IQ_FACTORY.createDistinctNode();
        OrderByNode newOrderByNode = IQ_FACTORY.createOrderByNode(ImmutableList.of(
                IQ_FACTORY.createOrderComparator(X, true)));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, Y, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(newSliceNode,
                        IQ_FACTORY.createUnaryIQTree(newDistinctNode,
                                IQ_FACTORY.createUnaryIQTree(newOrderByNode,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1)))));

        assertEquals(query1, query);
    }

}
