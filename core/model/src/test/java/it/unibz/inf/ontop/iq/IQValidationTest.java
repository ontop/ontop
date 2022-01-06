package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;


public class IQValidationTest {

    private final static RelationDefinition TABLE2;
    private final static RelationDefinition TABLE2_2;
    private final static RelationDefinition TABLE2_3;
    private final static RelationDefinition TABLE2_4;
    private final static RelationDefinition TABLE3;
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
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE2 = builder.createDatabaseRelation("TABLE2",
            "col1", integerDBType, false,
            "col2", integerDBType, false);

        TABLE2_2 = builder.createDatabaseRelation("TABLE22",
            "col1", integerDBType, false,
            "col2", integerDBType, false);

        TABLE2_3 = builder.createDatabaseRelation("TABLE22",
            "col1", integerDBType, false,
            "col2", integerDBType, false);

        TABLE2_4 = builder.createDatabaseRelation("TABLE22",
            "col1", integerDBType, false,
            "col2", integerDBType, false);

        TABLE3 = builder.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnionNodeChild() {
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_VAR3_PREDICATE, ImmutableList.of(A, B, C));

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE2_2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(A, B, C));

        IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(table1DataNode)),
                                        table4DataNode)),
                                table5DataNode))));
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnionNodeProjectedVariables() {
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_VAR3_PREDICATE, ImmutableList.of(A, B, C));

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE2_3, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE2_4, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(A, B, C));

        IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                        table4DataNode)),
                                table5DataNode))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInnerJoinNodeChildren() {
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(dataNode))));
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
    public void testUnboundVariableInFilter() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION);
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));

        IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, dataNode)));
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnboundVariableInInnerJoin() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, A));

        IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2))));
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnboundVariableInLeftJoin() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode joinNode = IQ_FACTORY.createLeftJoinNode(EXPRESSION);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(X, A));

        IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(joinNode, dataNode1, dataNode2)));
    }
}
