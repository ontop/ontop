package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.sql.Types;
import java.util.Optional;
import java.util.stream.IntStream;


import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertTrue;

public class LevelUpOptimizerTest {


    private static final OntopViewDefinition BASE_VIEW_1;
    private static final OntopViewDefinition BASE_VIEW_2;
    private static final OntopViewDefinition BASE_VIEW_31;

    private static final OntopViewDefinition NESTED_VIEW_1;
    private static final OntopViewDefinition NESTED_VIEW_2;
    private static final OntopViewDefinition NESTED_VIEW_3;
    private static final OntopViewDefinition NESTED_VIEW4;

    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static AtomPredicate ANS4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(4);

    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable A1 = TERM_FACTORY.getVariable("A1");
    private final static Variable A2 = TERM_FACTORY.getVariable("A2");
    private final static Variable B = TERM_FACTORY.getVariable("B");
    private final static Variable B1 = TERM_FACTORY.getVariable("B1");
    private final static Variable B2 = TERM_FACTORY.getVariable("B2");
    private final static Variable C = TERM_FACTORY.getVariable("C");
    private final static Variable C1 = TERM_FACTORY.getVariable("C1");
    private final static Variable C2 = TERM_FACTORY.getVariable("C2");
    private final static Variable C3 = TERM_FACTORY.getVariable("C3");
    private final static Variable C4 = TERM_FACTORY.getVariable("C4");
    private final static Variable D = TERM_FACTORY.getVariable("D");
    private final static Variable D1 = TERM_FACTORY.getVariable("D1");
    private final static Variable D2 = TERM_FACTORY.getVariable("D2");
    private final static Variable E = TERM_FACTORY.getVariable("E");
    private final static Variable F0 = TERM_FACTORY.getVariable("f0");
    private final static Variable F1 = TERM_FACTORY.getVariable("f1");
    private final static Variable F2 = TERM_FACTORY.getVariable("f2");
    private final static Variable F3 = TERM_FACTORY.getVariable("f3");
    private final static Variable F4 = TERM_FACTORY.getVariable("f4");
    private final static Variable F5 = TERM_FACTORY.getVariable("f5");
    private final static Variable G = TERM_FACTORY.getVariable("G");
    private final static Variable N = TERM_FACTORY.getVariable("N");
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");

    private static final Constant ONE = TERM_FACTORY.getConstantLiteral("1");
    private static final Constant TWO = TERM_FACTORY.getConstantLiteral("2");

    static {


        OfflineMetadataProviderBuilder2 builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();
        DBTermType arrayDBType = builder.getDBTypeFactory().getArrayDBType();

        NamedRelationDefinition TABLE1 = builder.createDatabaseRelation("TABLE1",
                "pk", integerDBType, false,
                "arr", arrayDBType, true,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        NamedRelationDefinition TABLE2 = builder.createDatabaseRelation("TABLE2",
                "pk", integerDBType, false,
                "col2", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));

        NamedRelationDefinition TABLE3 = builder.createDatabaseRelation("TABLE3",
                "pk", integerDBType, false,
                "arr1", arrayDBType, true,
                "arr2", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));


    }

    static {
//        BasicDBMetadata dbMetadata = createDummyMetadata();
//        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        // has nestedView1 as child, and no parent
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "table1"));
        Attribute col1T1 = table1Def.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
        table1Def.addAttribute(idFactory.createAttributeID("arr1"), Types.ARRAY, null, true);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T1));
        TABLE1_PREDICATE = table1Def.getAtomPredicate();

        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "table2"));
        Attribute col1T2 = table2Def.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T2));
        TABLE2_PREDICATE = table2Def.getAtomPredicate();


        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "table3"));
        Attribute col1T3 = table3Def.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("arr1"), Types.ARRAY, null, true);
        table3Def.addAttribute(idFactory.createAttributeID("arr2"), Types.ARRAY, null, true);
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T3));
        TABLE3_PREDICATE = table3Def.getAtomPredicate();

        // has table1 as parent
        NestedView nestedView1 = dbMetadata.createNestedView(
                idFactory.createRelationID(null, "nestedView1"),
                table1Def,
                FLATTEN_NODE_PRED_AR3.getRelationDefinition(),
                2
        );
        Attribute col1N1 = nestedView1.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        nestedView1.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
        nestedView1.addAttribute(idFactory.createAttributeID("arr1"), Types.ARRAY, null, false);
        nestedView1.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N1));
        NESTED_VIEW1 = nestedView1.getAtomPredicate();

        // has nestedView1 as parent
        NestedView nestedView2 = dbMetadata.createNestedView(
                idFactory.createRelationID(null, "nestedView2"),
                nestedView1,
                FLATTEN_NODE_PRED_AR3.getRelationDefinition(),
                2
        );
        Attribute col1N2 = nestedView2.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        nestedView2.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
        nestedView2.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        nestedView2.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N2));
        NESTED_VIEW2 = nestedView2.getAtomPredicate();

        // has table3 as parent
        NestedView nestedView3 = dbMetadata.createNestedView(
                idFactory.createRelationID(null, "nestedView3"),
                table3Def,
                FLATTEN_NODE_PRED_AR3.getRelationDefinition(),
                2
        );

        Attribute col1N3 = nestedView3.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        nestedView3.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
        nestedView3.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        nestedView3.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N3));
        NESTED_VIEW3 = nestedView3.getAtomPredicate();

        // has table3 as parent
        NestedView nestedView4 = dbMetadata.createNestedView(
                idFactory.createRelationID(null, "nestedView4"),
                table1Def,
                FLATTEN_NODE_PRED_AR4.getRelationDefinition(),
                3
        );

        Attribute col1N4 = nestedView4.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
        nestedView4.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
        nestedView4.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        nestedView4.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N3));
        NESTED_VIEW4 = nestedView3.getAtomPredicate();

        dbMetadata.freeze();
        DB_METADATA = dbMetadata;
    }


    @Test
    public void testNoLevelUp() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, B));
        queryBuilder.addChild(joinNode, leftDataNode);

        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(N, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, Y, D, E));
        queryBuilder.addChild(joinNode, flattenNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, N, C));
        queryBuilder.addChild(flattenNode, rightDataNode);

        IntermediateQuery query = queryBuilder.build();

        optimizeAndCompare(query, query);
    }


    @Test
    public void testLevelUp1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);


        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        queryBuilder.addChild(rootNode, dataNode);


        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, C));
        expectedQueryBuilder.addChild(rootNode, flattenNode);
        expectedQueryBuilder.addChild(flattenNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, F1, F2, F0)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testLevelUp2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);


        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW2, X, B, C));
        queryBuilder.addChild(rootNode, dataNode);


        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, C));
        expectedQueryBuilder.addChild(rootNode, flattenNode);
        expectedQueryBuilder.addChild(flattenNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, F1, F2, F0)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testLevelUp3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        queryBuilder.addChild(joinNode, leftDataNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW3, X, B, D));
        queryBuilder.addChild(joinNode, rightDataNode);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);
        StrictFlattenNode flattenNode1 = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, C));
        expectedQueryBuilder.addChild(joinNode, flattenNode1);
        StrictFlattenNode flattenNode2 = IQ_FACTORY.createStrictFlattenNode(F3, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, D));
        expectedQueryBuilder.addChild(joinNode, flattenNode2);
        expectedQueryBuilder.addChild(flattenNode1, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, F1, F2, F0)));
        expectedQueryBuilder.addChild(flattenNode2, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, F4, F5, F3)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testLevelUp4() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        queryBuilder.addChild(joinNode, leftDataNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW2, X, B, D));
        queryBuilder.addChild(joinNode, rightDataNode);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, leftDataNode);
        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, D));
        expectedQueryBuilder.addChild(joinNode, flattenNode);
        expectedQueryBuilder.addChild(flattenNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, F1, F2, F0)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testLevelUpRecurs1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, rootNode);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(EQ, B, ONE)
        );
        queryBuilder.addChild(rootNode, filterNode);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        queryBuilder.addChild(filterNode, dataNode);


        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, C));
        expectedQueryBuilder.addChild(rootNode, flattenNode);
        expectedQueryBuilder.addChild(flattenNode, filterNode);

        expectedQueryBuilder.addChild(filterNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, F1, F2, F0)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    private static void optimizeAndCompare(IntermediateQuery query, IntermediateQuery expectedQuery) throws EmptyQueryException {
        System.out.println("\nBefore optimization: \n" + query);
        System.out.println("\nExpected: \n" + expectedQuery);

        IQ optimizedIQ = LEVEL_UP_OPTIMIZER.optimize(IQ_CONVERTER.convert(query));
        IntermediateQuery optimizedQuery = IQ_CONVERTER.convert(
                optimizedIQ,
                query.getExecutorRegistry()
        );
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    public static OfflineMetadataProviderBuilder2 createMetadataProviderBuilder() {
        return new OfflineMetadataProviderBuilder2(CORE_SINGLETONS);
    }

    public static class OfflineMetadataProviderBuilder2 extends OfflineMetadataProviderBuilder {

        public OfflineMetadataProviderBuilder2(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        private OntopViewDefinition createBaseView(NamedRelationDefinition relationDefinition, ImmutableList<Variable> variables) {

            IQ iq = createBaseViewIQ(relationDefinition, variables);
            RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, ImmutableSet.of());
            return new OntopViewDefinitionImpl(
                    ImmutableList.of(relationDefinition.getID()),
                    attributeBuilder,
                    iq,
                    1,
                    CORE_SINGLETONS
            );
        }

        private IQ createBaseViewIQ(NamedRelationDefinition relationDefinition, ImmutableList<Variable> variables) {

            DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                    ATOM_FACTORY.getRDFAnswerPredicate(variables.size()),
                    variables
            );

            return IQ_FACTORY.createIQ(
                    projectionAtom,
                    createExtensionalDataNode(
                            relationDefinition,
                            variables
                    ));
        }

        private OntopViewDefinition createNestedView(OntopViewDefinition parentView,
                                                     boolean isStrictFlatten,
                                                     Variable flattenedVariable,
                                                     ImmutableList<Variable> retainedVariables,
                                                     ImmutableList<Variable> freshVariables,
                                                     ImmutableList<FunctionalDependency> functionalDependencies,
                                                     ImmutableSet<QuotedID> nonNullAttributes) {

            RelationID id = getQuotedIDFactory().createRelationID("FLATTEN_" + parentView.getID().toString());
            IQ iq = createNestedViewIQ(parentView, isStrictFlatten, flattenedVariable, retainedVariables, freshVariables);
            RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, nonNullAttributes);

            OntopViewDefinition view = new OntopViewDefinitionImpl(
                    ImmutableList.of(id),
                    attributeBuilder,
                    iq,
                    parentView.getLevel() + 1,
                    CORE_SINGLETONS
            );
            functionalDependencies.forEach(d -> view.addFunctionalDependency(d));
            return view;
        }

        /**
         * TODO: add projectionNode
         */
        private IQ createNestedViewIQ(OntopViewDefinition parentView,
                                      boolean isStrictFlatten,
                                      Variable flattenedVariable,
                                      ImmutableList<Variable> retainedVariables,
                                      ImmutableList<Variable> freshVariables) {

            DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                    ATOM_FACTORY.getRDFAnswerPredicate(retainedVariables.size() + freshVariables.size()),
                    ImmutableList.<Variable>builder().addAll(retainedVariables).addAll(freshVariables).build()
            );

            FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, flattenedVariable, Optional.empty(), isStrictFlatten);
            return IQ_FACTORY.createIQ(
                    projectionAtom,
                    IQ_FACTORY.createUnaryIQTree(
                            flattenNode,
                            createExtensionalDataNode(
                                    parentView,
                                    parentView.getIQ().getProjectionAtom().getArguments()
                    )));

        }

        private RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, ImmutableSet<QuotedID> nonNullAttributes) {
            QuotedIDFactory quotedIdFactory = getQuotedIDFactory();
            SingleTermTypeExtractor termTypeExtractor = CORE_SINGLETONS.getUniqueTermTypeExtractor();

            RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
            IQTree iqTree = iq.getTree();

            RawQuotedIDFactory rawQuotedIqFactory = new RawQuotedIDFactory(quotedIdFactory);

            for (Variable v : iq.getProjectionAtom().getVariables()) {
                QuotedID attributeId = rawQuotedIqFactory.createAttributeID(v.getName());

                boolean isNullable = (!nonNullAttributes.contains(attributeId))
                        && iqTree.getVariableNullability().isPossiblyNullable(v);

                builder.addAttribute(attributeId,
                        (DBTermType) termTypeExtractor.extractSingleTermType(v, iqTree)
                                .orElseGet(() -> getDBTypeFactory().getAbstractRootDBType()),
                        isNullable);
            }
            return builder;
        }

    }

    public static ExtensionalDataNode createExtensionalDataNode(RelationDefinition relation, ImmutableList<? extends Variable> arguments) {
        return IQ_FACTORY.createExtensionalDataNode(relation,
                IntStream.range(0, arguments.size())
                        .boxed()
                        .collect(ImmutableCollectors.toMap(
                                i -> i,
                                i -> (VariableOrGroundTerm) arguments.get(i)))
        );
    }
}




