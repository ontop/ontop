package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;


public class IQValidationTest {

    private final static RelationPredicate TABLE1;
    private final static RelationPredicate TABLE1_1;
    private final static RelationPredicate TABLE1_2;
    private final static RelationPredicate TABLE1_3;
    private final static RelationPredicate TABLE2;
    private final static RelationPredicate TABLE2_2;
    private final static RelationPredicate TABLE2_3;
    private final static RelationPredicate TABLE2_4;
    private final static RelationPredicate TABLE3;
    private final static AtomPredicate ANS1_VAR3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static AtomPredicate ANS1_VAR1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");

    private final static ImmutableExpression EXPRESSION = TERM_FACTORY.getStrictEquality(X, Y);

    static {
        OfflineMetadataProviderBuilder builder = createMetadataBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        RelationDefinition table1Def = builder.createDatabaseRelation("TABLE1",
            "col1", integerDBType, false);
        TABLE1 = table1Def.getAtomPredicate();

        RelationDefinition table11Def = builder.createDatabaseRelation("TABLE11",
            "col1", integerDBType, false);
        TABLE1_1 = table11Def.getAtomPredicate();

        RelationDefinition table12Def = builder.createDatabaseRelation("TABLE12",
            "col1", integerDBType, false);
        TABLE1_2 = table12Def.getAtomPredicate();

        RelationDefinition table13Def = builder.createDatabaseRelation("TABLE13",
            "col1", integerDBType, false);
        TABLE1_3 = table13Def.getAtomPredicate();

        RelationDefinition table2Def = builder.createDatabaseRelation("TABLE2",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        TABLE2 = table2Def.getAtomPredicate();

        RelationDefinition table22Def = builder.createDatabaseRelation("TABLE22",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        TABLE2_2 = table22Def.getAtomPredicate();

        RelationDefinition table23Def = builder.createDatabaseRelation("TABLE22",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        TABLE2_3 = table23Def.getAtomPredicate();

        RelationDefinition table24Def = builder.createDatabaseRelation("TABLE22",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        TABLE2_4 = table24Def.getAtomPredicate();

        RelationDefinition table3Def = builder.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        TABLE3 = table3Def.getAtomPredicate();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testConstructionNodeChild() {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1, A));
        ExtensionalDataNode table2DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_1, A));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, table1DataNode);
        queryBuilder.addChild(constructionNode, table2DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnionNodeChild() {
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_VAR3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, A, B));
        ExtensionalDataNode table4DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_2, A, C));
        ExtensionalDataNode table5DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3, A, B, C));

        queryBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        queryBuilder.addChild(rootConstructionNode, unionNode1);
        queryBuilder.addChild(unionNode1, joinNode);
        queryBuilder.addChild(unionNode1, table5DataNode);
        queryBuilder.addChild(joinNode, unionNode2);
        queryBuilder.addChild(joinNode, table4DataNode);
        queryBuilder.addChild(unionNode2, table1DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnionNodeProjectedVariables() {
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_VAR3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, A, B));
        ExtensionalDataNode table2DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_2, A, B));
        ExtensionalDataNode table3DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_3, A, B));
        ExtensionalDataNode table4DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_4, A, C));
        ExtensionalDataNode table5DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3, A, B, C));

        queryBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        queryBuilder.addChild(rootConstructionNode, unionNode1);
        queryBuilder.addChild(unionNode1, joinNode);
        queryBuilder.addChild(unionNode1, table5DataNode);
        queryBuilder.addChild(joinNode, unionNode2);
        queryBuilder.addChild(joinNode, table4DataNode);
        queryBuilder.addChild(unionNode2, table1DataNode);
        queryBuilder.addChild(unionNode2, table2DataNode);
        queryBuilder.addChild(unionNode2, table3DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testInnerJoinNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testLeftJoinNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, LEFT);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testFilterNodeChild() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, filterNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testExtensionalDataNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_2, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_3, A));
        queryBuilder.addChild(innerJoinNode, dataNode1);
        queryBuilder.addChild(innerJoinNode, dataNode2);
        queryBuilder.addChild(dataNode1, dataNode3);
        IntermediateQuery query = queryBuilder.build();
    }


    @Test(expected = InvalidIntermediateQueryException.class)
    public void testIntensionalDataNodeChildren() {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1, A));
        IntensionalDataNode dataNode2 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_2, A));
        IntensionalDataNode dataNode3 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_3, A));
        queryBuilder.addChild(innerJoinNode, dataNode1);
        queryBuilder.addChild(innerJoinNode, dataNode2);
        queryBuilder.addChild(dataNode1, dataNode3);
        IntermediateQuery query = queryBuilder.build();
    }

//    @Test(expected = InvalidIntermediateQueryException.class)
//    public void testGroupNodeChildren() {
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(Z));
//        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
//        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
//        queryBuilder.init(projectionAtom, constructionNode);
//        queryBuilder.addChild(constructionNode, groupNode);
//        IntermediateQuery query = queryBuilder.build();
//    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testEmptyNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(Z));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, emptyNode);
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, Z));
        queryBuilder.addChild(emptyNode, dataNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnboundVariableInFilter() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION);
        queryBuilder.addChild(rootNode, filterNode);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, Z));
        queryBuilder.addChild(filterNode, dataNode);
        queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnboundVariableInInnerJoin() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, Z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, A));
        queryBuilder.addChild(joinNode, dataNode2);
        queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnboundVariableInLeftJoin() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode joinNode = IQ_FACTORY.createLeftJoinNode(EXPRESSION);
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, Z));
        queryBuilder.addChild(joinNode, dataNode1, LEFT);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2, X, A));
        queryBuilder.addChild(joinNode, dataNode2, RIGHT);
        queryBuilder.build();
    }
}
