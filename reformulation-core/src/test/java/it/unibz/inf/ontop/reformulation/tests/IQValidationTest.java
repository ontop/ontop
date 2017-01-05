package it.unibz.inf.ontop.reformulation.tests;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import org.junit.Test;

import java.util.Optional;


public class IQValidationTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 3);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate P3_PREDICATE = new AtomPredicateImpl("p1", 3);
    private final static AtomPredicate ANS1_VAR1_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("x");
    private final static Variable Y = DATA_FACTORY.getVariable("y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Variable A = DATA_FACTORY.getVariable("a");
    private final static Variable B = DATA_FACTORY.getVariable("b");
    private final static Variable C = DATA_FACTORY.getVariable("c");

    private final static ImmutableExpression EXPRESSION = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Y);

    private final MetadataForQueryOptimization metadata;

    private static final Injector INJECTOR = QuestCoreConfiguration.defaultBuilder().build().getInjector();

    public IQValidationTest() {
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

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testConstructionNodeChild() {
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 1);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);

        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(A));
        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, table1DataNode);
        queryBuilder.addChild(constructionNode, table2DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnionNodeChild() {
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(A));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testLeftJoinNodeChildren() {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testFilterNodeChild() {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        FilterNode filterNode = new FilterNodeImpl(EXPRESSION);
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, filterNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testExtensionalDataNodeChildren() {
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 1);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table2", 1);

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(A));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A));
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
        ExtensionalDataNode dataNode3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A));
        queryBuilder.addChild(innerJoinNode, dataNode1);
        queryBuilder.addChild(innerJoinNode, dataNode2);
        queryBuilder.addChild(dataNode1, dataNode3);
        IntermediateQuery query = queryBuilder.build();
    }


    @Test(expected = InvalidIntermediateQueryException.class)
    public void testIntensionalDataNodeChildren() {
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 1);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table2", 1);

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        InnerJoinNode innerJoinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSION));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(A));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        IntensionalDataNode dataNode1 = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A));
        IntensionalDataNode dataNode2 = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
        IntensionalDataNode dataNode3 = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A));
        queryBuilder.addChild(innerJoinNode, dataNode1);
        queryBuilder.addChild(innerJoinNode, dataNode2);
        queryBuilder.addChild(dataNode1, dataNode3);
        IntermediateQuery query = queryBuilder.build();
    }

//    @Test(expected = InvalidIntermediateQueryException.class)
//    public void testGroupNodeChildren() {
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(Z));
//        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
//        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
//        queryBuilder.init(projectionAtom, constructionNode);
//        queryBuilder.addChild(constructionNode, groupNode);
//        IntermediateQuery query = queryBuilder.build();
//    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testEmptyNodeChildren() {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(Z));
        ConstructionNode constructionNode = new ConstructionNodeImpl(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, emptyNode);
        ExtensionalDataNode dataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(emptyNode, dataNode);
        IntermediateQuery query = queryBuilder.build();
    }
}
