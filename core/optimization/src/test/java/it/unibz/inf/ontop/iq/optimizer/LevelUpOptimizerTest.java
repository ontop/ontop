package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import java.sql.Types;


import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;
import static junit.framework.TestCase.assertTrue;

public class LevelUpOptimizerTest {

    private static final RelationPredicate TABLE1_PREDICATE;
    private static final RelationPredicate TABLE2_PREDICATE;
    private static final RelationPredicate TABLE3_PREDICATE;
    private static final RelationPredicate NESTED_VIEW1;
    private static final RelationPredicate NESTED_VIEW2;
    private static final RelationPredicate NESTED_VIEW3;
    private static final RelationPredicate NESTED_VIEW4;

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
        expectedQueryBuilder.addChild(rootNode,flattenNode);
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
        expectedQueryBuilder.addChild(rootNode,flattenNode);
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
        expectedQueryBuilder.addChild(joinNode,flattenNode1);
        StrictFlattenNode flattenNode2 = IQ_FACTORY.createStrictFlattenNode(F3, 0,
                ATOM_FACTORY.getDataAtom(FLATTEN_NODE_PRED_AR3, X, B, D));
        expectedQueryBuilder.addChild(joinNode,flattenNode2);
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
        expectedQueryBuilder.addChild(joinNode,flattenNode);
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
        expectedQueryBuilder.addChild(rootNode,flattenNode);
        expectedQueryBuilder.addChild(flattenNode, filterNode);

        expectedQueryBuilder.addChild(filterNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, F1, F2, F0)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    private static void optimizeAndCompare(IntermediateQuery query, IntermediateQuery expectedQuery) throws EmptyQueryException {
        System.out.println("\nBefore optimization: \n" +  query);
        System.out.println("\nExpected: \n" +  expectedQuery);

        IQ optimizedIQ = LEVEL_UP_OPTIMIZER.optimize(IQ_CONVERTER.convert(query));
        IntermediateQuery optimizedQuery = IQ_CONVERTER.convert(
                optimizedIQ,
                query.getExecutorRegistry()
        );
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }
}

