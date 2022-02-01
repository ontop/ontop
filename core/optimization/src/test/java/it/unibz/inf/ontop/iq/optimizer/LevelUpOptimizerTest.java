package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.IntStream;


import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class LevelUpOptimizerTest {


    private final static OfflineMetadataProviderBuilder2 BUILDER;

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE3;



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
    private final static Variable X1 = TERM_FACTORY.getVariable("X1");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");

    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    static {


        BUILDER = createMetadataProviderBuilder();
        DBTermType integerDBType = BUILDER.getDBTypeFactory().getDBLargeIntegerType();
        DBTermType arrayDBType = BUILDER.getDBTypeFactory().getArrayDBType();

        TABLE1 = BUILDER.createDatabaseRelation("TABLE1",
                "pk", integerDBType, false,
                "arr", arrayDBType, true,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        TABLE2 = BUILDER.createDatabaseRelation("TABLE2",
                "pk", integerDBType, false,
                "col2", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));

        TABLE3 = BUILDER.createDatabaseRelation("TABLE3",
                "pk", integerDBType, false,
                "arr1", arrayDBType, true,
                "arr2", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));


    }

    static {
//        BasicDBMetadata dbMetadata = createDummyMetadata();
//        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        // has nestedView1 as child, and no parent
//        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "table1"));
//        Attribute col1T1 = table1Def.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
//        table1Def.addAttribute(idFactory.createAttributeID("arr1"), Types.ARRAY, null, true);
//        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T1));
//        TABLE1_PREDICATE = table1Def.getAtomPredicate();
//
//        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "table2"));
//        Attribute col1T2 = table2Def.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
//        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T2));
//        TABLE2_PREDICATE = table2Def.getAtomPredicate();
//
//
//        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "table3"));
//        Attribute col1T3 = table3Def.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        table3Def.addAttribute(idFactory.createAttributeID("arr1"), Types.ARRAY, null, true);
//        table3Def.addAttribute(idFactory.createAttributeID("arr2"), Types.ARRAY, null, true);
//        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T3));
//        TABLE3_PREDICATE = table3Def.getAtomPredicate();
//
//        // has table1 as parent
//        NestedView nestedView1 = dbMetadata.createNestedView(
//                idFactory.createRelationID(null, "nestedView1"),
//                table1Def,
//                FLATTEN_NODE_PRED_AR3.getRelationDefinition(),
//                2
//        );
//        Attribute col1N1 = nestedView1.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        nestedView1.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
//        nestedView1.addAttribute(idFactory.createAttributeID("arr1"), Types.ARRAY, null, false);
//        nestedView1.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N1));
//        NESTED_VIEW1 = nestedView1.getAtomPredicate();
//
//        // has nestedView1 as parent
//        NestedView nestedView2 = dbMetadata.createNestedView(
//                idFactory.createRelationID(null, "nestedView2"),
//                nestedView1,
//                FLATTEN_NODE_PRED_AR3.getRelationDefinition(),
//                2
//        );
//        Attribute col1N2 = nestedView2.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        nestedView2.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
//        nestedView2.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
//        nestedView2.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N2));
//        NESTED_VIEW2 = nestedView2.getAtomPredicate();
//
//        // has table3 as parent
//        NestedView nestedView3 = dbMetadata.createNestedView(
//                idFactory.createRelationID(null, "nestedView3"),
//                table3Def,
//                FLATTEN_NODE_PRED_AR3.getRelationDefinition(),
//                2
//        );
//
//        Attribute col1N3 = nestedView3.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        nestedView3.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
//        nestedView3.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
//        nestedView3.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N3));
//        NESTED_VIEW3 = nestedView3.getAtomPredicate();
//
//        // has table3 as parent
//        NestedView nestedView4 = dbMetadata.createNestedView(
//                idFactory.createRelationID(null, "nestedView4"),
//                table1Def,
//                FLATTEN_NODE_PRED_AR4.getRelationDefinition(),
//                3
//        );
//
//        Attribute col1N4 = nestedView4.addAttribute(idFactory.createAttributeID("pk"), Types.INTEGER, null, false);
//        nestedView4.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, true);
//        nestedView4.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
//        nestedView4.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1N3));
//        NESTED_VIEW4 = nestedView3.getAtomPredicate();
//
//        dbMetadata.freeze();
//        DB_METADATA = dbMetadata;
    }


    @Test
    public void testNoLevelUp() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(2), X, O
        );

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X, B));
        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.empty(), true);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X, N, C));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flattenNode,
                                                rightDataNode
                                        )))));

//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
//        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
//        queryBuilder.init(projectionAtom, rootNode);

//        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
//        queryBuilder.addChild(rootNode, joinNode);

//        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
//                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, B));
//        queryBuilder.addChild(joinNode, leftDataNode);
//
//        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(N, 0,
//                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, Y, D, E));
//        queryBuilder.addChild(joinNode, flattenNode);
//
//        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
//                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, N, C));
//        queryBuilder.addChild(flattenNode, rightDataNode);
//
//        IntermediateQuery query = queryBuilder.build();

        optimizeAndCompare(initialIQ, initialIQ);
    }


    @Test
    public void testLevelUp1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, C1, O
        );

        OntopViewDefinition baseView = BUILDER.createBaseView(TABLE1, ImmutableList.of(X, N, C));
        OntopViewDefinition nestedView = BUILDER.createStrictNestedViewNoDependency(
                baseView,
                N,
                O,
                ImmutableList.of(X, C),
                ImmutableList.of(X1, C1)
        );

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode = createExtensionalDataNodeFromView(nestedView);

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        dataNode
                ));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, C1, O),
                BUILDER.getJSONSubstitution(
                        ImmutableList.of(X, C),
                        ImmutableList.of(X1, C1)
                ));

        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.empty(), true);
        ExtensionalDataNode dataNode2 = createExtensionalDataNodeFromView(baseView);

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        flattenNode,
                                        dataNode2
                        ))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLevelUp2() {

        OntopViewDefinition baseView = BUILDER.createBaseView(TABLE3, ImmutableList.of(X, M, N));
        OntopViewDefinition nestedView1 = BUILDER.createStrictNestedViewNoDependency(
                baseView,
                M,
                O1,
                ImmutableList.of(X, N),
                ImmutableList.of(X1, N1)
        );

        OntopViewDefinition nestedView2 = BUILDER.createStrictNestedViewNoDependency(
                nestedView1,
                M,
                O2,
                ImmutableList.of(X1, O1),
                ImmutableList.of(X11, O11)
        );


        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), X11, O11, O2);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode = createExtensionalDataNodeFromView(nestedView2);

        queryBuilder.init(projectionAtom, rootNode);
        queryBuilder.addChild(rootNode, dataNode);


        FlattenNode flattenNode1 = IQ_FACTORY.createFlattenNode(O2, N1, Optional.empty(), true);
        FlattenNode flattenNode2 = IQ_FACTORY.createFlattenNode(O11, M, Optional.empty(), true);
        ExtensionalDataNode dataNode2 = createExtensionalDataNodeFromView(baseView);

        queryBuilder.init(projectionAtom, rootNode);
        queryBuilder.addChild(rootNode, flattenNode1);
        queryBuilder.addChild(flattenNode1, flattenNode2);
        queryBuilder.addChild(flattenNode2, dataNode2);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLevelUp3() {


        OntopViewDefinition baseView1 = BUILDER.createBaseView(TABLE1, ImmutableList.of(X1, N1, C1));
        OntopViewDefinition baseView2 = BUILDER.createBaseView(TABLE3, ImmutableList.of(X2, N2, N3));
        OntopViewDefinition nestedView1 = BUILDER.createStrictNestedViewNoDependency(
                baseView1,
                N1,
                O1,
                ImmutableList.of(X1, N1),
                ImmutableList.of(X11, N1)
        );

        OntopViewDefinition nestedView2 = BUILDER.createStrictNestedViewNoDependency(
                baseView2,
                M,
                O2,
                ImmutableList.of(X1, O1),
                ImmutableList.of(X11, O11)
        );


        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW3, X, B, D));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, rootNode);
        queryBuilder.addChild(rootNode, joinNode);
        queryBuilder.addChild(joinNode, leftDataNode);
        queryBuilder.addChild(joinNode, rightDataNode);

        StrictFlattenNode flattenNode1 = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, C));
        StrictFlattenNode flattenNode2 = IQ_FACTORY.createStrictFlattenNode(F3, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, D));

        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, flattenNode1);
        expectedQueryBuilder.addChild(joinNode, flattenNode2);
        expectedQueryBuilder.addChild(flattenNode1, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, F1, F2, F0)));
        expectedQueryBuilder.addChild(flattenNode2, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, F4, F5, F3)));


        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLevelUp4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW2, X, B, D));
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, leftDataNode);

        queryBuilder.addChild(joinNode, rightDataNode);


        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, leftDataNode);
        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, D));
        expectedQueryBuilder.addChild(joinNode, flattenNode);
        expectedQueryBuilder.addChild(flattenNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, F1, F2, F0)));


        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLevelUpRecurs1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(EQ, B, ONE)
        );
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(NESTED_VIEW1, X, B, C));
        queryBuilder.init(projectionAtom, rootNode);

        queryBuilder.addChild(rootNode, filterNode);

        queryBuilder.addChild(filterNode, dataNode);


        expectedQueryBuilder.init(projectionAtom, rootNode);
        StrictFlattenNode flattenNode = IQ_FACTORY.createStrictFlattenNode(F0, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, C));

        expectedQueryBuilder.addChild(rootNode, flattenNode);
        expectedQueryBuilder.addChild(flattenNode, filterNode);
        expectedQueryBuilder.addChild(filterNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, F1, F2, F0)));

        optimizeAndCompare(initialIQ, expectedIQ);

    }

    private static void optimizeAndCompare(IQ query, IQ expectedQuery) {
        System.out.println("\nBefore optimization: \n" + query);
        System.out.println("\nExpected: \n" + expectedQuery);

        IQ optimizedIQ = LEVEL_UP_OPTIMIZER.optimize(query);
        System.out.println("\nAfter optimization: \n" + optimizedIQ);

        assertEquals(expectedQuery, optimizedIQ);
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


        private OntopViewDefinition createStrictNestedViewNoDependency( OntopViewDefinition parentView,
                                                     Variable flattenedVariable,
                                                     Variable outputVariable,
                                                     ImmutableList<Variable> retainedVariables,
                                                     ImmutableList<Variable> freshVariables) {

            return createNestedView(parentView, true, flattenedVariable, outputVariable, retainedVariables, freshVariables, ImmutableList.of(), ImmutableSet.of());
        }


        private OntopViewDefinition createNestedView(OntopViewDefinition parentView,
                                                     boolean isStrictFlatten,
                                                     Variable flattenedVariable,
                                                     Variable outputVariable,
                                                     ImmutableList<Variable> retainedVariables,
                                                     ImmutableList<Variable> freshVariables,
                                                     ImmutableList<FunctionalDependency> functionalDependencies,
                                                     ImmutableSet<QuotedID> nonNullAttributes) {

            RelationID id = getQuotedIDFactory().createRelationID("FLATTEN_" + parentView.getID().toString());
            IQ iq = createNestedViewIQ(parentView, isStrictFlatten, flattenedVariable, outputVariable, retainedVariables, freshVariables);
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
                                      Variable outputVariable,
                                      ImmutableList<Variable> retainedVariables,
                                      ImmutableList<Variable> renamedRetainedVariables
        ) {

            ImmutableList projectedVars = ImmutableList.<Variable>builder().addAll(renamedRetainedVariables).add(outputVariable).build();
            DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                    ATOM_FACTORY.getRDFAnswerPredicate(retainedVariables.size() + 1),
                    projectedVars
            );

            ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                    ImmutableSet.copyOf(projectedVars),
                    getJSONSubstitution(retainedVariables, renamedRetainedVariables)
            );

            FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, flattenedVariable, Optional.empty(), isStrictFlatten);
            return IQ_FACTORY.createIQ(
                    projectionAtom,
                    IQ_FACTORY.createUnaryIQTree(
                            constructionNode,
                            IQ_FACTORY.createUnaryIQTree(
                                    flattenNode,
                                    createExtensionalDataNode(
                                            parentView,
                                            parentView.getIQ().getProjectionAtom().getArguments()
                                    ))));
        }

        private ImmutableSubstitution<ImmutableTerm> getJSONSubstitution(ImmutableList<Variable> retainedVariables, ImmutableList<Variable> renamedRetainedVariables) {

            return SUBSTITUTION_FACTORY.getSubstitution(
                    IntStream.range(0, retainedVariables.size()).boxed()
                            .collect(ImmutableCollectors.toMap(
                                    i -> renamedRetainedVariables.get(i),
                                    i -> getJSONEltFunctionalTerm(renamedRetainedVariables.get(i))
                            )));



        }

        private ImmutableTerm getJSONEltFunctionalTerm(Variable variable) {
            return TERM_FACTORY.getImmutableFunctionalTerm(
                    FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory().getDBJsonEltFromJsonPath(),
                    variable
            );
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


    private static ExtensionalDataNode createExtensionalDataNodeFromView(OntopViewDefinition view) {
        return createExtensionalDataNode(view, view.getIQ().getProjectionAtom().getArguments());
    }


    private static ExtensionalDataNode createExtensionalDataNode(RelationDefinition relation, ImmutableList<? extends Variable> arguments) {
        return IQ_FACTORY.createExtensionalDataNode(relation,
                IntStream.range(0, arguments.size())
                        .boxed()
                        .collect(ImmutableCollectors.toMap(
                                i -> i,
                                i -> (VariableOrGroundTerm) arguments.get(i)))
        );
    }
}




