package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static org.junit.Assert.assertEquals;

/**
 * Elimination of redundant self-joins using a non unique functional constraint
 */
public class FunctionalDependencyTest {

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;
    private final static NamedRelationDefinition TABLE11;
    private final static NamedRelationDefinition TABLE15;

    private final static AtomPredicate ANS1_PREDICATE_AR_1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS1_PREDICATE_AR_2 = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static AtomPredicate ANS1_PREDICATE_AR_3 = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");
    private final static Variable D = TERM_FACTORY.getVariable("d");
    private final static Variable E = TERM_FACTORY.getVariable("e");
    private final static Variable F = TERM_FACTORY.getVariable("f");
    private final static Variable G = TERM_FACTORY.getVariable("g");
    private final static Variable H = TERM_FACTORY.getVariable("h");
    private final static Variable I = TERM_FACTORY.getVariable("i");
    private final static Variable J = TERM_FACTORY.getVariable("j");
    private final static Variable K = TERM_FACTORY.getVariable("k");
    private final static Variable L = TERM_FACTORY.getVariable("l");
    private final static Variable M = TERM_FACTORY.getVariable("m");
    private final static Variable N = TERM_FACTORY.getVariable("n");
    private final static Variable O = TERM_FACTORY.getVariable("o");
    private final static Variable P = TERM_FACTORY.getVariable("p");
    private final static Variable Q = TERM_FACTORY.getVariable("q");
    private final static Variable R = TERM_FACTORY.getVariable("r");
    private final static Variable S = TERM_FACTORY.getVariable("s");
    private final static Variable T = TERM_FACTORY.getVariable("t");
    private final static Variable U = TERM_FACTORY.getVariable("u");
    private final static Variable V = TERM_FACTORY.getVariable("v");
    private final static Variable W = TERM_FACTORY.getVariable("w");
    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant THREE = TERM_FACTORY.getDBConstant("3", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    static{
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        /*
         * Table 1: PK + non-unique functional constraint + 2 dependent fields + 1 independent
         */
        TABLE1 = builder.createDatabaseRelation("table1",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE1)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        TABLE2 = builder.createDatabaseRelation("table2",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(2));

        /*
         * Table 3: PK + 2 independent non-unique functional constraints + 1 independent
         */
        TABLE3 = builder.createDatabaseRelation("table3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false,
            "col6", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE3)
                .addDeterminant(2)
                .addDependent(3)
                .build();
        FunctionalDependency.defaultBuilder(TABLE3)
                .addDeterminant(4)
                .addDependent(5)
                .build();

        /*
         * Table 4: PK + 2 non-unique functional constraints (one is nested) + 1 independent attribute
         */
        TABLE4 = builder.createDatabaseRelation("table4",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE4)
                .addDeterminant(3)
                .addDependent(4)
                .build();
        FunctionalDependency.defaultBuilder(TABLE4)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        /*
         * Table 11: Like table 1, except that the non-PK columns are nullable
         */
        TABLE11 = builder.createDatabaseRelation("table10",
                "col1", integerDBType, false,
                "col2", integerDBType, true,
                "col3", integerDBType, true,
                "col4", integerDBType, true,
                "col5", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE11.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE11)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        /*
         * Table 15: PK + 2 non-unique functional constraints (one is nested) + 1 independent attribute
         */
        TABLE15 = builder.createDatabaseRelation("table4",
                "col1", integerDBType, false,
                "col2", integerDBType, true,
                "col3", integerDBType, true,
                "col4", integerDBType, true,
                "col5", integerDBType, true,
                "col6", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE15.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE15)
                .addDeterminant(3)
                .addDependent(4)
                .build();
        FunctionalDependency.defaultBuilder(TABLE15)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .addDependent(5)
                .build();

    }

    @Test
    public void testRedundantSelfJoin1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A ));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 2, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin2()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3,
                X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 3, Z));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 2, Y, 3, Z));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3,
                X, Y, Z);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 2, Z));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Z, Y));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 2, Y));
        expectedQueryBuilder.addChild(newRootNode, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 2, Y, 3, X));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 2,Y, 3, X));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 4, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 3, Z));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 3, Z, 4, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin6() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 4, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 3, Z));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3= IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 3, Z, 4, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode4);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    /**
     * Second column non-nullable
     */
    @Test
    public void testRedundantSelfJoin7() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 4, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 3, Z));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, F, 2, G));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, F, 2, M));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 3, Z, 4, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode5);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    /**
     * Second column nullable
     */
    @Test
    public void testRedundantSelfJoin7T11() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11, ImmutableMap.of(0, X, 1, A, 4, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11, ImmutableMap.of(1, A, 3, Z));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11, ImmutableMap.of(1, F, 2, G));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE11, ImmutableMap.of(1, F, 2, M));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBIsNotNull(A), TERM_FACTORY.getDBIsNotNull(F)));
        expectedQueryBuilder.addChild(topConstructionNode, newJoinNode);

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE11, ImmutableMap.of(0, X, 1, A, 3, Z, 4, Y));
        expectedQueryBuilder.addChild(newJoinNode, dataNode5);
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE11, ImmutableMap.of(1, F));
        expectedQueryBuilder.addChild(newJoinNode, dataNode6);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin7_1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 4, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 3, Z));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, F));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, F, 4, Z));
        queryBuilder.addChild(joinNode, dataNode4);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, joinNode);

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 3, Z, 4, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode5);

        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(4, Z));
        expectedQueryBuilder.addChild(joinNode, dataNode6);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin7_3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 4, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 3, Z, 4, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, F));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, F));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 3, Z, 4, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode5);

        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 4, TWO));
        expectedQueryBuilder.addChild(joinNode, dataNode6);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }


    @Test
    public void testRedundantSelfJoin8() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_1, X);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, X, 2, B));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, X, 3, F));
        queryBuilder.addChild(joinNode, dataNode2);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, X));

        expectedQueryBuilder.addChild(distinctNode, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin9() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, X));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, X, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);

        expectedQueryBuilder.addChild(distinctNode, dataNode2);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    /**
     * Y --> from an independent attribute
     */
    @Test
    public void testNonRedundantSelfJoin1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, F, G, Y));
        queryBuilder.addChild(joinNode, dataNode2);


        IntermediateQueryBuilder expectedQueryBuilder = IQ_FACTORY.createIQBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);
        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0,X, 1, A));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, A, 4, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode4);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testNonRedundantSelfJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, Y, 1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = IQ_FACTORY.createIQBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);
        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, Y, 1, A)));

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, ONE, B, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, TWO, E, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IQ query = queryBuilder.buildIQ();

        System.out.println("\nBefore optimization: \n" +  query);

        IQ optimizedQuery = optimize(query);
        System.err.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin2() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictNEquality(B, TWO));
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, TWO, F, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IQ query = queryBuilder.buildIQ();

        System.out.println("\nBefore optimization: \n" +  query);

        IQ optimizedQuery = optimize(query);
        System.err.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin3() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictNEquality(F, TWO));
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, B, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, TWO, F, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IQ query = queryBuilder.buildIQ();

        System.out.println("\nBefore optimization: \n" +  query);

        IQ optimizedQuery = optimize(query);
        System.err.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test
    public void testRedundantSelfJoin1_T3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, X, 1, A));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, X, 2, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin2_T3()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, X, 1, A));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(3, M, 4, O));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(3, M, 4, T));
        queryBuilder.addChild(joinNode, dataNode4);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, X, 2, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode5);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin3_T3()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, X, 1, A, 3, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, Y, 3, C));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, X, 2, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin1_T4()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, X, 1, A));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(1, A, 2, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, X, 2, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin2_T4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, X, 1, A, 2, B));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(1, A, 2, B, 3, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, X, 3, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode3);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Ignore("TODO: optimize the redundant self-lj (no variable on the right is used")
    @Test
    public void testLJRedundantSelfLeftJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(topConstructionNode, leftJoinNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(C, A, D, E, F));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(F, H, I, J, K));
        queryBuilder.addChild(leftJoinNode, dataNode3, RIGHT);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 3, Z, 4, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode4);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testLJRedundantSelfLeftJoin2() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(topConstructionNode, leftJoinNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(C, A, D, E, F));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(G, H, I, J, K));
        queryBuilder.addChild(leftJoinNode, dataNode3, RIGHT);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, X, 3, Z, 4, Y));
        expectedQueryBuilder.init(projectionAtom, dataNode4);

        optimizeAndCompare(queryBuilder.buildIQ(), expectedQueryBuilder.buildIQ());
    }

    @Test
    public void testRedundantSelfJoin10() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, X));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 2, TWO, 4, Y));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, TWO, 1, A, 2, TWO));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 4, Y));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode3, dataNode4));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        newConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createDistinctNode(),
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y)),
                                        newJoinTree))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin11() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, X, 4, Z));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 2, TWO, 4, Y));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, TWO, 1, A, 2, TWO, 4, Z));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 4, Y));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode3, dataNode4));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        newConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createDistinctNode(),
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, Z)),
                                        newJoinTree))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin12() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, X));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 2, TWO));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_1, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, TWO, 1, A, 2, TWO));

        UnaryIQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode3);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(
                newConstructionNode,
                filterTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin13() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, X, 4, Y));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 2, TWO, 4, Y));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, TWO, 1, A, 2, TWO, 4, Y));

        UnaryIQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(
                        TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getDBIsNotNull(A),
                                TERM_FACTORY.getDBIsNotNull(Y))),
                dataNode3);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(
                newConstructionNode,
                filterTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin14() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, Y));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, Y));

        UnaryIQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode3);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                filterTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin15() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of( 1, A, 2, B, 4, Y));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of(0, X, 1, A, 2, B));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of(0, X, 1, A, 2, B, 4, Y));

        UnaryIQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBIsNotNull(A),
                        TERM_FACTORY.getDBIsNotNull(B))),
                dataNode3);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                filterTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin16() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of( 1, A, 2, B, 4, Y, 5, Z));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of(0, X, 1, A, 2, B));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                joinTree)));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of(1, A, 2, B, 4, Y, 5, Z));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE15,
                ImmutableMap.of(0, X, 1, A));

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBIsNotNull(B)),
                ImmutableList.of(dataNode3, dataNode4));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                newJoinTree)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin17() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of( 0, X,1, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, Y, 1, A));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 2, Z));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createDistinctNode(),
                                joinTree)));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, Z));

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        newJoinTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testRedundantSelfJoin18() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of( 0, X,1, A, 2, B));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(1, A, 3, Y));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode1, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createDistinctNode(),
                                joinTree)));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE11,
                ImmutableMap.of(0, X, 1, A, 2, B, 3, Y));

        IQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBIsNotNull(A),
                        TERM_FACTORY.getDBIsNotNull(B))),
                dataNode4);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        filterTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial query: "+ initialIQ);
        System.out.println("Expected query: "+ expectedIQ);
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        System.out.println("Optimized query: "+ optimizedIQ);
        assertEquals(expectedIQ, optimizedIQ);
    }

    private IQ optimize(IQ query) throws EmptyQueryException {
        IQ initialIQ =  query.normalizeForOptimization();

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        if (optimizedIQ.getTree().isDeclaredAsEmpty())
            throw new EmptyQueryException();

        return optimizedIQ;
    }
}
